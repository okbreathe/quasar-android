package com.okbreathe.quasar.data

import android.util.Log
import io.requery.Persistable
import io.requery.reactivex.KotlinReactiveEntityStore

import com.okbreathe.quasar.data.models.*
import com.okbreathe.quasar.util.utcTimestamp
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.requery.kotlin.*
import io.requery.query.OrderingExpression
import io.requery.reactivex.ReactiveResult
import java.text.ParseException

data class RemoteLink(val remoteId: Int, val localId: Int, val type: String)

class LocalStore(val data: KotlinReactiveEntityStore<Persistable>) {
  val TAG = "QSR:LocalStore"

  fun listPages(order: OrderingExpression<Any> = Page::updatedAt.desc()): ReactiveResult<Page> =
    data.select(Page::class)
      .where(Page::isTrashed eq false)
      .and(Page::isDeleted eq false)
      .orderBy(order)
      .get()

  fun listFavoritePages(order: OrderingExpression<Any> = Page::updatedAt.desc()): ReactiveResult<Page> =
    data.select(Page::class)
      .where(Page::isFavorite eq true)
      .and(Page::isDeleted eq false)
      .orderBy(order)
      .get()

  fun listRecentPages(order: OrderingExpression<Any> = Page::updatedAt.desc()): ReactiveResult<Page> =
    data.select(Page::class)
      .where(Page::isDeleted eq false)
      .orderBy(order)
      .limit(10)
      .get()

  fun listTrashedPages(order: OrderingExpression<Any> = Page::updatedAt.desc()): ReactiveResult<Page> =
    data.select(Page::class)
      .where(Page::isTrashed eq true)
      .and(Page::isDeleted eq false)
      .orderBy(order)
      .get()

  fun listTaggedPages(tag: Tag, order: OrderingExpression<Any> = Page::updatedAt.desc()): ReactiveResult<Page> =
    listTaggedPages(tag.id, order)

  fun listTaggedPages(id: Int, order: OrderingExpression<Any> = Page::updatedAt.desc()): ReactiveResult<Page> =
    data.select(Page::class)
      .join(PageTag::class).on(PageTag::pageId eq Page::id)
      .where(PageTag::tagId eq id)
      .and(Page::isDeleted eq false)
      .orderBy(order)
      .get()

  // TODO Should be a subquery
  fun searchPages(query: String): ReactiveResult<Page> {
    Log.d(TAG, "Returning results for query $query")
    val ids: List<Int> = data.raw(Search::class, "SELECT docid FROM Search WHERE Search MATCH ?", query).toList().map { it.docid }
    return data.select(Page::class).where(Page::id `in` ids).get()
  }

  fun indexFTS() {
    data.raw("INSERT INTO Search (docid, content) SELECT id, content FROM Page WHERE ?", true)
  }

  fun indexFTS(ids: List<Int>) {
    if (ids.isNotEmpty()) {
      data.raw("INSERT INTO Search (docid, content) SELECT id, content FROM Page WHERE remote_id IN ?", ids)
    }
  }

  // TODO
  // data.raw("UPDATE Search SET content = (SELECT content FROM Page WHERE id = ?) WHERE docid = ?", page.id, page.id)
  // causes:
  // io.reactivex.exceptions.UndeliverableException: io.requery.sql.StatementExecutionException:
  // Exception executing statement: UPDATE Search SET content = (SELECT content FROM Page where id = (?, ?)) WHERE docid = ?
  fun updateFTS(page: Page) {
    data.raw("INSERT OR REPLACE INTO Search (docid, content) SELECT id, content FROM Page WHERE id = ?", page.id)
  }

  fun deleteFTS(page: Page) =
    data.raw("DELETE FROM Search WHERE docid = ? ", page.id)

  fun listTags(): ReactiveResult<Tag> = data.select(Tag::class).get()

  fun listPageTags(): ReactiveResult<PageTag> = data.select(PageTag::class).get()

  fun listTagsForPage(page: Page): ReactiveResult<Tag> =
    data.select(Tag::class)
      .join(PageTag::class).on(PageTag::tagId eq Tag::id)
      .where(PageTag::pageId eq page.id)
      .get()

  fun listRevisionsFor(pageId: Int): ReactiveResult<Revision> =
    data.select(Revision::class)
      .where(Revision::page eq pageId and (Revision::isDeleted eq false))
      .orderBy(Revision::insertedAt.desc())
      .get()

  fun getRevision(id: Int): ReactiveResult<Revision> =
    data.select(Revision::class)
      .where(Revision::id eq id)
      .get()

  fun getPage(id: Int): ReactiveResult<Page> =
    data.select(Page::class)
      .where(Page::id eq id)
      .and(Page::isDeleted eq false)
      .get()

  fun getPageByRemoteId(remoteId: Int): ReactiveResult<Page> =
    data.select(Page::class).where(Page::remoteId eq remoteId).get()

  fun createPage(title: String = "", content: String = ""): Single<Page> = with(PageEntity()) {
    val now = utcTimestamp()
    this.title = title
    this.content = content
    insertedAt = now
    updatedAt = now
    data.insert(this)
  }

  fun insertPageAs(page: Page) = data.insert(page)

  fun updatePageAs(page: Page) = data.update(page)

  // If we're being passed a page in the future then
  // we'd be overwriting the updated_at
  fun updatePage(page: Page): Disposable {
    page.updatedAt = utcTimestamp()
    return data.update(page).subscribe()
  }

  fun updatePage(page: Page, title: String, content: String) {
    val titleChanged = title != page.title
    val contentChanged = content != page.content
    if (!titleChanged && !contentChanged) return

    transaction {
      if (contentChanged) createRevision(page)
      page.updatedAt = utcTimestamp()
      page.title = title
      page.content = content
      data.update(page).subscribe(
        { it -> updateFTS(it) },
        { err -> Log.d(TAG, err.message) })
    }
  }

  fun createRevision(page: Page?, isSnapshot: Boolean = false) {
    if (page == null || page.content.isNullOrBlank()) return
    with (RevisionEntity()) {
      val now = utcTimestamp()
      setPage(page)
      event = "update"
      content = page.content
      snapshot = isSnapshot
      insertedAt = now
      data.insert(this).blockingGet()
    }
    deleteStaleAutosaves(page)
  }

  /**
   * Remove older snapshots
   * If it's already been synced, just note it as deleted, otherwise actually delete it
   *
   * TODO raw queries seem o be very buggy with requery:
   * - using a ? for page.id causes it to insert (?,?)
   * - Seems like queries aren't always executed unless they're logged or used in some way
   */
  fun deleteStaleAutosaves(page: Page, offset: Int = 50) {
    val toMark = """
      SELECT id FROM Revision
      WHERE page_id = ${page.id} AND (remote_id IS NOT NULL AND remote_id != 0) AND snapshot = 0
      ORDER BY inserted_at DESC
      LIMIT 1000 OFFSET ?
    """
    val toDelete = """
    SELECT id FROM Revision
      WHERE page_id = ${page.id} AND (remote_id IS NULL OR remote_id = 0) AND snapshot = 0
      ORDER BY inserted_at DESC
      LIMIT 1000 OFFSET ?
    """
    Log.d(TAG, "Marking For Deletion: ${data.raw(toMark, offset).toList()}")
    data.raw("UPDATE Revision SET is_deleted = 1 WHERE id IN ($toMark)", offset)
    Log.d(TAG, "Marked for Deletion: ${data.raw("SELECT id FROM Revision WHERE is_deleted = 1").toList()}")
    // Can safely deleted unsynced revisions
    val deleted = data.raw("DELETE FROM Revision WHERE id IN ($toDelete)", offset)
    Log.d(TAG, "Actually Deleted: ${deleted.toList()}")
  }

  fun insertRevisionsAs(revisions: List<Revision>) = data.insert(revisions)

  /**
   * Append tags to a page's tagset
   */
  fun insertTags(page: Page, tags: List<Tag>?) {
    // Can't we do an upsert here?
    tags?.let {
      for (t in tags) {
        val existingTag = data.select(Tag::class).where(Tag::name eq t.name).get().firstOrNull()
        if (existingTag != null) {
          createPageTag(page, existingTag)
        } else {
          data.insert(t).subscribe(
            { tag -> createPageTag(page, tag) },
            { Log.e(TAG, it.message) }
          )
        }
      }
    }
  }

  /**
   * Replace entire page tagset
   */
  fun setTags(page: Page, tagNames: List<String>) {
    val tagsToCreate = mutableListOf<Tag>()
    for (name in tagNames) {
      val existingTag = data.select(Tag::class).where(Tag::name eq name).get().firstOrNull()
      if (existingTag == null) tagsToCreate.add(buildTag(name))
    }
    transaction {
      delete(PageTag::class).where(PageTag::pageId eq page.id).get().call()
      insert(tagsToCreate)
      insert(data.select(Tag::class).where(Tag::name `in` tagNames ).get().map {
        tag -> PageTagEntity().apply { pageId = page.id; tagId = tag.id }
      })
      // Remove unused tags
      val ret = delete(Tag::class).where(Tag::id notIn select(PageTag::tagId).distinct()).get().value()
      updatePage(page)
      Log.d(TAG, "Deleted $ret unused tags")
    }
  }

  /**
   * Retrieve all pages modified since the given time
   */
  fun pagesModifiedSince(dateString: String?): List<Page> {
    if (dateString.isNullOrBlank()) return listOf()
    try {
      val date = utcTimestamp(dateString)
      return data.select(Page::class).where(Page::updatedAt gt date).get().toList()
    } catch (e: ParseException)  {
      return listOf()
    }
  }

  fun unsyncedPages(): List<Page> =
    // TODO Why is remoteId zero?
    data.select(Page::class).where((Page::remoteId.isNull()) or (Page::remoteId eq 0)).get().toList()

  /**
   * Link up local data with the server
   */
  fun associateRemotePages(assocs: List<RemoteLink>) {
    val updates = assocs.partition { it.type == "Page" }
    transaction {
      data.update(updates.first.map {
        val p = PageEntity()
        p.id = it.localId
        p.remoteId = it.remoteId
        p
      }).subscribe()
      data.update(updates.second.map {
        val r = RevisionEntity()
        r.id = it.localId
        r.remoteId = it.remoteId
        r
      }).subscribe()
    }
  }

  fun deleteEverything(): Unit = with (data) {
    transaction {
      delete(PageTag::class).get().value()
      delete(Page::class).get().value()
      delete(Tag::class).get().value()
      delete(Revision::class).get().value()
      delete(Upload::class).get().value()
    }
  }

  fun deletePage(id: Int) = deletePage(getPage(id).firstOrNull())

  /**
   * If There is no remoteId assigned, it hasn't been
   * synced with the server and can be safely ACTUALLY
   * deleted
   */
  fun deletePage(page: Page) {
    transaction {
      deleteFTS(page)
      if (page.remoteId == null || page.remoteId == 0) {
        data.delete(page).subscribe()
      } else {
        page.isDeleted = true
        data.update(page).subscribe()
      }
    }
  }

  fun deleteRevision(id: Int): Int =
    data.delete(Revision::class).where(Revision::id eq id).get().value()

  fun deletePageRevisions(pageId: Int): Int =
    data.delete(Revision::class).where(Revision::page eq pageId).get().value()

  fun transaction(body: BlockingEntityStore<Persistable>.() -> Unit) = data.toBlocking().withTransaction(body)

  private fun buildTag(tagName: String): Tag = TagEntity().apply {
    val now = utcTimestamp()
    name = tagName
    insertedAt = now
    updatedAt = now
  }

  private fun createPageTag(page: Page, tag: Tag) {
    val pageTag = PageTagEntity()
    pageTag.pageId = page.id
    pageTag.tagId = tag.id
    data.upsert(pageTag).subscribe({}, { Log.e(TAG, it.message)})
  }
}

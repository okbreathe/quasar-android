package com.okbreathe.quasar.data

import android.util.Log
import com.okbreathe.quasar.data.api.*
import com.okbreathe.quasar.data.models.*
import com.okbreathe.quasar.util.utcDateString
import com.okbreathe.quasar.util.utcTimestamp

class SyncStore(val store: LocalStore) {
  val TAG = "QSR:SyncStore"
  /**
   * Apply updates from server
   */
  fun applyRemoteUpdates(pages: List<PageResponse>): Boolean {
    val ids = mutableListOf<Int>()
    for (remotePage in pages) {
      val date = utcTimestamp(remotePage.updatedAt)
      val remoteId =remotePage.id.toInt()
      val localPage = store.getPageByRemoteId(remoteId).firstOrNull()

      if (localPage == null) {
        createPage(remotePage)
      } else if (localPage.updatedAt < date) {
        updatePage(localPage, remotePage)
      }
      ids.add(remoteId)
    }
    store.indexFTS(ids)
    return true
  }

  /**
   * Associate the remote id with the local copy
   * This only happens for pages created on the device
   * TODO return whether or not this query actually succeeds
   */
  fun associateRemoteIds(assocs: List<SyncResponse>): Boolean {
    Log.d(TAG, "Associating IDs: " + assocs.map{ "client_id: ${it.clientId}, item_id: ${it.itemId}, item_type: ${it.itemType}" }.toString())
    store.associateRemotePages(assocs.map {
      RemoteLink(remoteId = it.itemId!!.toInt(), localId = it.clientId!!.toInt(), type = it.itemType!!.toString())
    })
    return true
  }

  /**
   * Generate data to push to the server
   */
  fun generateSyncRequest(pages: List<Page>): SyncRequest =
    SyncRequest(pages.map {
      SyncRequest.Page(
        id = it.id,
        remote_id = it.remoteId,
        is_deleted = it.isDeleted,
        is_trashed = it.isTrashed,
        is_favorite = it.isFavorite,
        title = it.title,
        content = it.content,
        inserted_at = utcDateString(it.insertedAt),
        updated_at = utcDateString(it.updatedAt),
        revisions = it.revisions.toList().map {
          SyncRequest.Revision(
            id = it.id,
            page_id = it.page.id,
            remote_id = it.remoteId,
            event = it.event,
            content = it.content,
            snapshot = it.snapshot,
            inserted_at = utcDateString(it.insertedAt)
          )
        },
        tags = it.tags.toList().map {
          SyncRequest.Tag(
            name = it.name
          )
        }
      )
    })

  fun updateFTS() = store.indexFTS()

  private fun createPage(resp: PageResponse) =
    store.transaction {
      store.insertPageAs(buildLocal(resp))
        .subscribe({
          store.insertTags(it, buildTags(resp))
          createRevisions(resp)
        }, {
          err -> Log.e(TAG, err.message)
        })
    }

  private fun updatePage(page: Page, resp: PageResponse) =
    store.transaction {
      store.updatePageAs(buildLocal(resp, page))
        .subscribe({
          store.insertTags(it, buildTags(resp))
          if (resp.isTrashed) store.deletePageRevisions(resp.id.toInt())
          else createRevisions(resp)
        }, {
          err -> Log.e(TAG, err.message)
        })
    }

  private fun createRevisions(resp: PageResponse) =
    resp.getRevisions()?.let {
      store.insertRevisionsAs(buildRevisions(resp, it))
        .subscribe({}, { err -> Log.e(TAG, err.message)})
    }

  /**
   * Builds a page copy of a resp page
   */
  private fun buildLocal(resp: PageResponse, page: Page? = null): Page =
    (page ?: PageEntity()).apply {
      remoteId = resp.id.toInt()
      title = resp.title ?: ""
      content = resp.content ?: ""
      isFavorite = resp.isFavorite
      isTrashed = resp.isTrashed
      isDeleted = resp.isDeleted
      // TODO Not sure if should prefer server or local time in case of clock differences
      insertedAt = utcTimestamp(resp.insertedAt)
      updatedAt = utcTimestamp(resp.updatedAt)
    }

  private fun buildRevisions(resp: PageResponse, revisions: List<RevisionResponse>): List<RevisionEntity> =
    revisions.fold(arrayListOf<RevisionEntity>()) { acc, rev ->
      acc.add(buildRevision(resp, rev)); acc
    }

  private fun buildRevision(remote: PageResponse, revision: RevisionResponse): RevisionEntity =
    RevisionEntity().apply {
      val page = store.getPageByRemoteId(remote.id.toInt()).firstOrNull()
      event = revision.event
      page?.let { setPage(page)}
      remoteId = revision.id.toInt()
      content = revision.content
      insertedAt = utcTimestamp(revision.insertedAt)
    }

  private fun buildTags(resp: PageResponse): List<Tag> = (resp.getTags() ?: listOf()).map { buildTag(it) }

  private fun buildTag(resp: TagResponse): Tag = TagEntity().apply {
    val now = utcTimestamp()
    name = resp.name
    insertedAt = now
    updatedAt = now
    remoteId = resp.id.toInt()
  }
}

package com.okbreathe.quasar.ui.pages

import com.okbreathe.quasar.Application
import com.okbreathe.quasar.data.LocalStore
import com.okbreathe.quasar.data.models.Page
import com.okbreathe.quasar.data.models.Revision
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.requery.reactivex.ReactiveResult

class PageDetailViewModel(app: Application, private val id: Int): PageViewModel(app) {
  private var store: LocalStore = app.localStore

  fun getPage(): Observable<ReactiveResult<Page>> =
    store.getPage(id)
      .observableResult()
      .subscribeOn(Schedulers.single())
      .observeOn(AndroidSchedulers.mainThread())

  fun updatePage(page: Page, title: String, content: String) =
    store.updatePage(page, title, content)

  fun trashPage() = toggleTrashed(true)

  fun unTrashPage() = toggleTrashed(false)

  fun deletePage(pageId: Int) = store.deletePage(pageId)

  fun favoritePage() = toggleFavorite(true)

  fun unFavoritePage() = toggleFavorite(false)

  fun listRevisions(id: Int): Observable<ReactiveResult<Revision>> =
    store.listRevisionsFor(id).observableResult()

  fun getRevision(id: Int): Observable<ReactiveResult<Revision>> =
    store.getRevision(id).observableResult()

  fun createRevision(pageId: Int) =
    store.createRevision(store.getPage(pageId).firstOrNull(), isSnapshot = true)

  fun deleteRevision(revisionId: Int) = store.deleteRevision(revisionId)

  /**
   * Replace the current version of a page with a previous revision
   */
  fun restoreFromRevision(revisionId: Int) {
    store.getRevision(revisionId).firstOrNull()?.let {
      store.updatePage(it.page, title = it.page.title, content = it.content)
    }
  }

  /**
   * Return list of all tag names
   */
  fun tagNames(): List<String> = store.listTags().toList().map { it.name }

  /**
   * Return list of tag names for a particular page
   */
  fun tagNamesFor(page: Page?): List<String> =
    if (page == null) listOf()
    else store.listTagsForPage(page).toList().map { it.name }

  /**
   * Replace the set of tags on a particular page
   */
  fun setTags(page: Page?, tagNames: List<String>) =
    page?.let { store.setTags(page, tagNames) }

  private fun toggleTrashed(trashed: Boolean) {
    val page = store.getPage(id).maybe().blockingGet()
    page.isTrashed = trashed
    store.updatePage(page)
  }

  private fun toggleFavorite(favorite: Boolean): Page {
    val page = store.getPage(id).maybe().blockingGet()
    page.isFavorite = favorite
    store.updatePage(page)
    return page
  }

  override fun onCreate() {}

  override fun onPause() {}

  override fun onResume() {}

  override fun onDestroy() {}
}

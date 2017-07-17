package com.okbreathe.quasar.ui.pages

import com.okbreathe.quasar.Application
import com.okbreathe.quasar.data.LocalStore
import com.okbreathe.quasar.data.models.Page
import com.okbreathe.quasar.data.models.PageEntity
import com.okbreathe.quasar.data.models.PageTag
import com.okbreathe.quasar.data.models.Tag
import com.okbreathe.quasar.util.titleize
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.requery.kotlin.asc
import io.requery.kotlin.desc
import io.requery.reactivex.ReactiveResult

class PageListViewModel(app: Application) : PageViewModel(app) {
  enum class Filter { TAG, RECENT, FAVORITE, TRASH, ALL }
  enum class Sort { TITLE, UPDATED, CREATED }
  enum class Direction { ASC, DESC }
  private var store: LocalStore = app.localStore

  fun listPages(): Observable<ReactiveResult<Page>> =
    store.listPages()
        .observableResult()
        .subscribeOn(Schedulers.single())
        .observeOn(AndroidSchedulers.mainThread())

  fun searchPages(query: String?): Observable<ReactiveResult<Page>> =
    (if (query.isNullOrBlank()) store.listPages() else store.searchPages(query!!))
      .observableResult()
      .subscribeOn(Schedulers.single())
      .observeOn(AndroidSchedulers.mainThread())

  fun filterPages(filter: Filter, sort: Sort, dir: Direction, tagId: Int = -1): Observable<ReactiveResult<Page>> {
    val order = when (sort) {
      Sort.TITLE -> Page::title
      Sort.CREATED -> Page::insertedAt
      else -> Page::updatedAt
    }.let { if (dir == Direction.ASC) it.asc() else it.desc() }

    return when (filter) {
      Filter.RECENT -> store.listRecentPages(order)
      Filter.FAVORITE -> store.listFavoritePages(order)
      Filter.TRASH -> store.listTrashedPages(order)
      Filter.ALL -> store.listPages(order)
      Filter.TAG -> if (tagId > -1) store.listTaggedPages(tagId, order) else store.listPages(order)
    }
    .observableResult()
    .subscribeOn(Schedulers.single())
    .observeOn(AndroidSchedulers.mainThread())
  }

  fun sortToString(sort: Sort, dir: Direction): String = titleize("$sort $dir")

  fun listPageTags(): Observable<ReactiveResult<PageTag>> =
    store.listPageTags()
        .observableResult()
        .subscribeOn(Schedulers.single())
        .observeOn(AndroidSchedulers.mainThread())

  fun listTags(): Observable<ReactiveResult<Tag>> =
    store.listTags()
      .observableResult()
      .subscribeOn(Schedulers.single())
      .observeOn(AndroidSchedulers.mainThread())

  fun createPage(): Page = with(PageEntity()) {
    store.createPage().blockingGet()
  }

  override fun onCreate() {}

  override fun onPause() {}

  override fun onResume() {}

  override fun onDestroy() {}
}

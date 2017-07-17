package com.okbreathe.quasar.ui.pages

import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.LinearLayout.VERTICAL
import com.okbreathe.quasar.R
import com.okbreathe.quasar.ui.pages.PageListViewModel.Direction
import com.okbreathe.quasar.ui.pages.PageListViewModel.Filter
import com.okbreathe.quasar.ui.pages.PageListViewModel.Sort
import com.okbreathe.quasar.data.models.Page
import com.okbreathe.quasar.util.truncate
import io.reactivex.Observable
import io.requery.reactivex.ReactiveResult
import org.jetbrains.anko.*

class PageListAdapter(val viewModel: PageListViewModel, var excerpt: Boolean = false) : BaseAdapter() {
  val TAG = "QSR:PageListAdapter"
  private var pages = mutableListOf<Page>()
  private var observable: Observable<ReactiveResult<Page>>? = null

  init { dataChanged(viewModel.listPages()) }

  override fun getView(i : Int, v : View?, parent : ViewGroup?) : View {
    return with(parent!!.context) {
      val title = pages[i].title
      val content = pages[i].content
      val ui = viewModel.ui
      linearLayout {
        lparams(width = matchParent, height = wrapContent)
        padding = dip(10)
        orientation = VERTICAL

        textView {
          text = if (title.isNullOrBlank()) getString(R.string.title_no_title) else title
          textColor = if (title.isNullOrBlank()) ui.secondaryColor else ui.primaryColor
          textSize = 16f
          padding = dip(5)
        }
        if (excerpt && !content.isNullOrBlank()) {
          textView {
            text = truncate(content).replace("\n", " ")
            textColor = ui.primaryColor
            textSize = 12f
            padding = dip(5)
          }
        }
      }
    }
  }

  fun search(query: CharSequence?) = dataChanged(viewModel.searchPages(query.toString()))

  fun filter(filter: Filter, sort: Sort, dir: Direction, tagId: Int = -1) =
    dataChanged(viewModel.filterPages(filter, sort, dir, tagId))

  fun addOrUpdate(page: Page) {
    val existing = pages.find { it.id == page.id }
    if (existing != null) {
      val idx = pages.indexOf(existing)
      pages[idx] = page
      pages[pages.indexOf(existing)] = page
    } else {
      pages.add(pages.size, page)
    }
    notifyDataSetChanged()
  }

  fun clear() = pages.clear()

  override fun getItem(position : Int) : Page = pages[position]

  override fun getCount() : Int = pages.size

  override fun getItemId(position : Int) : Long = 0L

  private fun dataChanged(observable: Observable<ReactiveResult<Page>>) {
    this.observable = observable
    observable
      .subscribe({
        clear()
        notifyDataSetChanged()
        it.observable().subscribe {
          addOrUpdate(it)
        }
      }, { Log.e(TAG,  it.message) })
  }
}

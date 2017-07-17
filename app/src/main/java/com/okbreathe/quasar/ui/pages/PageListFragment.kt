package com.okbreathe.quasar.ui.pages

import android.content.res.ColorStateList
import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.FloatingActionButton
import org.jetbrains.anko.*
import android.support.v4.app.Fragment
import android.support.v4.view.GravityCompat
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import co.zsmb.materialdrawerkt.builders.drawer
import co.zsmb.materialdrawerkt.draweritems.badgeable.*
import co.zsmb.materialdrawerkt.draweritems.divider
import co.zsmb.materialdrawerkt.draweritems.expandable.expandableItem
import com.mikepenz.materialdrawer.model.ExpandableDrawerItem
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem
import com.okbreathe.quasar.Application
import com.okbreathe.quasar.R
import com.okbreathe.quasar.data.models.Tag
import org.jetbrains.anko.support.v4.UI
import com.okbreathe.quasar.ui.pages.PageListViewModel.*
import io.reactivex.rxkotlin.subscribeBy
import org.jetbrains.anko.design.coordinatorLayout
import org.jetbrains.anko.design.floatingActionButton
import org.jetbrains.anko.sdk25.listeners.onClick
import org.jetbrains.anko.sdk25.listeners.onItemClick

class PageListFragment : AbstractPageFragment() {
  val TAG = "QSR:PageListFragment"
  private lateinit var viewModel: PageListViewModel
  private lateinit var pageAdapter: PageListAdapter
  private lateinit var sortDescription: String
  private lateinit var layout: CoordinatorLayout
  private lateinit var newButton: FloatingActionButton
  private lateinit var empty: TextView
  private var sortMenuItem: ExpandableDrawerItem? = null
  private var selectedId: Long = 1
  private var sort: Sort = Sort.UPDATED
  private var direction: Direction = Direction.DESC
  private var filter : Filter = Filter.ALL
  private var tagId : Int = -1
  private var query = ""

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    viewModel = PageListViewModel((activity.application as Application))
    sortDescription = viewModel.sortToString(sort, direction)
    pageAdapter = PageListAdapter(viewModel, viewModel.showPageExcerpt)
  }

  override fun onResume() {
    super.onResume()
    setTitle(getString(R.string.app_name))
    layout.backgroundColor = viewModel.ui.backgroundColor
    newButton.backgroundTintList = ColorStateList.valueOf(viewModel.ui.accentColor)
    newButton.drawable.mutate().setTint(viewModel.foreground)
    empty.textColor = viewModel.ui.primaryColor
    pageAdapter.excerpt = viewModel.showPageExcerpt
    pageAdapter.notifyDataSetChanged()
  }

  override fun onCreateView(inf: LayoutInflater?, cont: ViewGroup?, inst: Bundle?): View? {
    drawer = renderDrawer(inst)
    setDrawerTags()
    return render().view
  }

  override fun onSaveInstanceState(outState: Bundle?) {
    super.onSaveInstanceState(outState)
    drawer?.saveInstanceState(outState)
    outState?.let {
      outState.putLong("selectedId", selectedId)
      outState.putSerializable("sort", sort)
      outState.putSerializable("direction", direction)
      outState.putSerializable("filter", filter)
      outState.putInt("tagId", tagId)
      outState.putString("query", query)
    }
  }

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)
    savedInstanceState?.let {
      selectedId = it.getLong("selectedId")
      tagId = it.getInt("tagId")
      query = it.getString("query")
      direction = it.getSerializable("direction") as Direction
      filter = it.getSerializable("filter") as Filter
      sort = it.getSerializable("sort") as Sort
    }
  }

  override fun search(query: CharSequence?): Boolean {
    this.query = query.toString()
    pageAdapter.search(query)
    return true
  }

  private fun sortPages(sort: Sort): Boolean {
    this.sort = sort
    pageAdapter.filter(filter, sort, direction, tagId)
    setSortDescription()
    return true
  }

  private fun orderPages(direction: Direction = Direction.DESC): Boolean {
    this.direction = direction
    pageAdapter.filter(filter, sort, direction, tagId)
    setSortDescription()
    return true
  }

  private fun filterPages(filter: Filter, id: Long): Boolean {
    this.filter = filter
    selectedId = id
    pageAdapter.filter(filter, sort, direction, tagId)
    return false
  }

  private fun filterPages(filter: Filter, tag: Tag): Boolean {
    this.filter = filter
    this.tagId = tag.id
    selectedId = tagIdentifier(tag)
    pageAdapter.filter(filter, sort, direction, tagId)
    return false
  }

  private fun setSortDescription() {
    sortDescription = viewModel.sortToString(sort, direction)
    sortMenuItem?.let {
      drawer?.updateItem(it.withDescription(sortDescription))
    }
  }

  private fun render(): AnkoContext<Fragment> {
    return UI {
      layout = coordinatorLayout {
        lparams(width = matchParent, height = matchParent)
        empty = textView(getString(R.string.title_no_pages)) {
          textSize = 20f
        }.lparams { gravity = Gravity.CENTER }
        verticalLayout {
          listView {
            emptyView = empty
            onItemClick { adapterView, view, i, l -> mListener?.onEditPage(pageAdapter.getItem(i)) }
            adapter = pageAdapter
          }
        }
        newButton = floatingActionButton {
          imageResource = R.drawable.plus
          onClick {
            mListener?.onNewPage(viewModel.createPage())
          }
        }.lparams(width = wrapContent, height = wrapContent) {
          margin = dip(16)
          gravity = Gravity.BOTTOM or GravityCompat.END
        }
      }
    }
  }

  private fun renderDrawer(instance: Bundle?) =
    drawer {
      savedInstance = instance
      primaryItem(getString(R.string.title_all_notes)) {
        icon = R.drawable.archive
        identifier = 1
        onClick { _ -> filterPages(Filter.ALL, 1) }
      }
      primaryItem(getString(R.string.title_recent)) {
        icon = R.drawable.clock
        identifier = 2
        onClick { _ -> filterPages(Filter.RECENT, 2) }
      }
      primaryItem(getString(R.string.title_favorite)) {
        icon = R.drawable.star_dark
        identifier = 3
        onClick { _ -> filterPages(Filter.FAVORITE, 3) }
      }
      primaryItem(getString(R.string.title_trash)) {
        icon = R.drawable.trash
        identifier = 4
        onClick { _ -> filterPages(Filter.TRASH, 4) }
      }
      divider {}
      sortMenuItem =
      expandableItem(getString(R.string.title_sort), sortDescription) {
        icon = R.drawable.sort
        selectable = false
        secondaryItem(getString(R.string.title_title)) {
          selectable = false
          identifier = 5
          onClick { _ -> sortPages(Sort.TITLE) }
        }
        secondaryItem(getString(R.string.title_created)) {
          selectable = false
          identifier = 6
          onClick { _ -> sortPages(Sort.CREATED) }
        }
        secondaryItem(getString(R.string.title_updated)) {
          selectable = false
          identifier = 7
          onClick { _ -> sortPages(Sort.UPDATED) }
        }
        divider {}
        secondaryItem(getString(R.string.title_ascending)) {
          selectable = false
          identifier = 8
          onClick { _ -> orderPages(Direction.ASC) }
        }
        secondaryItem(getString(R.string.title_descending)) {
          selectable = false
          identifier = 9
          onClick { _ -> orderPages(Direction.DESC) }
        }
      }
      divider {}
    }

  private fun setDrawerTags() {
    viewModel.listTags()
      .subscribe({
        it.observable().subscribeBy(
          onNext = { tag: Tag ->
            drawer?.removeItem(tagIdentifier(tag))
            val item = SecondaryDrawerItem()
              .withName(tag.name)
              .withIdentifier(tagIdentifier(tag))
              .withIcon(R.drawable.tag)
              .withOnDrawerItemClickListener { _, _, _ ->  filterPages(Filter.TAG, tag) }
            drawer?.addItem(item)
          },
          onError = { Log.e(TAG, it.message) },
          onComplete = { drawer?.setSelection(selectedId, false) }
        )
      }, {
        Log.e(TAG,  it.message)
      })
  }

  // Tag ids start at 100 to separate them from other menu items
  private fun tagIdentifier(tag: Tag) = tag.id.toLong() + 100

  companion object {
    fun newInstance() = PageListFragment()
  }
}
package com.okbreathe.quasar.ui.pages

import android.app.Activity
import android.support.v4.app.Fragment
import com.mikepenz.materialdrawer.Drawer
import com.okbreathe.quasar.R
import com.okbreathe.quasar.data.models.Page

abstract class AbstractPageFragment : Fragment() {
  protected var mListener: FragmentInteractionListener? = null
  protected var drawer: Drawer? = null
  var isDrawerOpen: Boolean = false
    get() = drawer?.isDrawerOpen ?: false
    protected set

  override fun onAttach(activity: Activity?) {
    super.onAttach(activity)
    try {
      mListener = activity as FragmentInteractionListener?
    } catch (e: ClassCastException) {
      throw ClassCastException(activity!!.toString() + " must implement FragmentInteractionListener")
    }
  }

  override fun onDetach() {
    super.onDetach()
    mListener = null
  }
  open fun favoritePage() {}

  open fun unFavoritePage() {}

  open fun deletePage() {}

  open fun trashPage() {}

  open fun unTrashPage() {}

  open fun openDrawer() = drawer?.openDrawer()

  open fun closeDrawer() = drawer?.closeDrawer()

  open fun search(query: CharSequence?): Boolean = false

  open fun createSnapshot() {}

  fun setTitle(title: String?) {
    activity?.let {
      val act = it as PageActivity
      if (title.isNullOrBlank()) act.setTitle(getString(R.string.title_no_title)) else act.setTitle(title)
    }
  }

  interface FragmentInteractionListener {
    fun onEditPage(page: Page)
    fun onToggleRevision(showing: Boolean)
    fun onPageUpdated(page: Page)
    fun onNewPage(page: Page)
    fun onDeletePage()
    fun onTrashPage()
    fun onUnTrashPage()
  }
}

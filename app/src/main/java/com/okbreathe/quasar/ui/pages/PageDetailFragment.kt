package com.okbreathe.quasar.ui.pages

import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.GravityCompat
import android.support.v4.view.ViewPager
import android.util.Log
import android.view.*
import android.widget.TextView
import co.zsmb.materialdrawerkt.builders.drawer
import co.zsmb.materialdrawerkt.draweritems.badgeable.*
import co.zsmb.materialdrawerkt.draweritems.sectionHeader
import com.mikepenz.materialdrawer.model.ExpandableDrawerItem
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem
import com.okbreathe.quasar.Application
import com.okbreathe.quasar.R
import com.okbreathe.quasar.data.models.Revision
import com.okbreathe.quasar.util.humanizeDate
import org.jetbrains.anko.*
import org.jetbrains.anko.design.coordinatorLayout
import org.jetbrains.anko.design.floatingActionButton
import org.jetbrains.anko.sdk25.listeners.onClick
import org.jetbrains.anko.support.v4.UI
import org.jetbrains.anko.support.v4.viewPager

class PageDetailFragment : AbstractPageFragment() {
  private val TAG = "QSR:PageDetailFragment"
  private lateinit var viewModel: PageDetailViewModel
  private lateinit var pager: ViewPager
  private lateinit var layout: CoordinatorLayout
  private lateinit var revisionInfo: TextView
  private lateinit var restoreButton: FloatingActionButton
  private lateinit var deleteButton: FloatingActionButton
  private lateinit var prefs: SharedPreferences
  private var editorDefault = false
  private var previewFragment: PagePreviewFragment? = null
  private var revisionId: Int = -1
  private var pageId: Int = -1
  private var isNew = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    pageId = arguments?.getInt("id") ?: -1
    isNew = arguments?.getBoolean("new") ?: false
    viewModel = PageDetailViewModel((activity.application as Application), pageId)
    prefs = PreferenceManager.getDefaultSharedPreferences(activity)
  }

  override fun onCreateView(inf: LayoutInflater?, cont: ViewGroup?, inst: Bundle?): View? {
    renderDrawer(inst)
    return render().view
  }

  override fun onResume() {
    super.onResume()
    editorDefault = prefs.getBoolean(getString(R.string.preference_default_to_editor), false)
    layout.backgroundColor = viewModel.ui.backgroundColor
    restoreButton.backgroundTintList = ColorStateList.valueOf(viewModel.ui.accentColor)
    restoreButton.drawable.mutate().setTint(viewModel.foreground)
  }

  override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    pager.adapter = PagerAdapter(childFragmentManager)
    if (isNew) pager.setCurrentItem(if (editorDefault) 1 else 0, false)
  }

  override fun onSaveInstanceState(outState: Bundle?) {
    super.onSaveInstanceState(drawer?.saveInstanceState(outState))
  }

  override fun favoritePage() {
    mListener?.onPageUpdated(viewModel.favoritePage())
    (activity as PageActivity).showMessage(R.string.message_favorite)
  }

  override fun unFavoritePage() {
    mListener?.onPageUpdated(viewModel.unFavoritePage())
    (activity as PageActivity).showMessage(R.string.message_unfavorite)
  }

  override fun trashPage() {
    viewModel.trashPage()
    (activity as PageActivity).showMessage(R.string.message_trashed)
    mListener?.onTrashPage()
  }

  override fun unTrashPage() {
    viewModel.unTrashPage()
    (activity as PageActivity).showMessage(R.string.message_untrashed)
    mListener?.onUnTrashPage()
  }

  override fun deletePage() {
    val act = (activity as PageActivity)
    val title = getString(R.string.confirmation_delete)
    val msg = getString(R.string.message_no_undo)
    act.showConfirmation(title, msg, {
      viewModel.deletePage(pageId)
      act.showMessage(R.string.message_deleted)
      mListener?.onDeletePage()
    })
  }

  override fun createSnapshot() {
    viewModel.createRevision(pageId)
    (activity as PageActivity).showMessage(R.string.message_snapshot_created)
  }

  private fun render(): AnkoContext<Fragment> {
    return UI {
      layout = coordinatorLayout {
        lparams(width = matchParent, height = matchParent)
        verticalLayout {
          pager = viewPager {
            id = R.id.pageDetailViewPager
          }
        }
        revisionInfo = textView {
          backgroundColor = Color.parseColor("#33FFFFFF")
          textColor = Color.BLACK
          visibility = View.GONE
          padding = dip(5)
          textAlignment = View.TEXT_ALIGNMENT_CENTER
        }.lparams(width = matchParent) {
          gravity = Gravity.BOTTOM or GravityCompat.START
        }
        deleteButton = floatingActionButton {
          imageResource = android.R.drawable.ic_menu_delete
          visibility = View.GONE
          onClick { this@PageDetailFragment.deleteRevision() }
        }.lparams(width = wrapContent, height = wrapContent) {
          margin = dip(16)
          gravity = Gravity.BOTTOM or GravityCompat.START
        }
        restoreButton = floatingActionButton {
          imageResource = android.R.drawable.ic_menu_revert
          visibility = View.GONE
          onClick { this@PageDetailFragment.restoreRevision() }
        }.lparams(width = wrapContent, height = wrapContent) {
          margin = dip(16)
          gravity = Gravity.BOTTOM or GravityCompat.END
        }
      }
    }
  }

  // Need better way of enabling / disabling
  private fun renderDrawer(instance: Bundle?) {
    drawer = drawer {
      savedInstance = instance
      primaryItem(getString(R.string.title_current_version)) {
        onClick { _ -> setPage() }
      }
      sectionHeader(getString(R.string.title_revisions))
    }
    renderExpandableItem(R.string.title_snapshots, { it.snapshot })
    renderExpandableItem(R.string.title_autosaves, { !it.snapshot })
  }

  private fun renderExpandableItem(resourceId: Int, pred: (Revision) -> Boolean){
    viewModel.listRevisions(pageId)
    .subscribe({
      drawer?.removeItem(resourceId.toLong())

      val item = ExpandableDrawerItem()
        .withIdentifier(resourceId.toLong())
        .withName(getString(resourceId))
        .withSelectable(false)
        .withEnabled(false)

      val revs: List<SecondaryDrawerItem> = it.filter(pred).map { revision ->
        SecondaryDrawerItem()
          .withName(humanizeDate(revision.insertedAt))
          .withOnDrawerItemClickListener { v, pos, drawerItem -> this.setRevision(revision.id) }
          .withLevel(2)
      }

      if (revs.isNotEmpty()) item.withEnabled(true).withSubItems(revs)

      drawer?.addItem(item)
    }, {
       err -> Log.e(TAG, err.message)
    })
  }

  private fun setRevision(revisionId: Int): Boolean {
    this.revisionId = revisionId
    pager.currentItem = 0
    pager.setOnTouchListener { v, event -> true }
    viewModel.getRevision(revisionId)
      .subscribe({
        mListener?.onToggleRevision(true)
        it.maybe().subscribe({
          previewFragment?.setRevision(it)
          val msg = getString(if(it.snapshot) R.string.title_snapshot else R.string.title_autosave)
          revisionInfo.text = "$msg from ${humanizeDate(it.insertedAt)}"
        })
      })
    restoreButton.visibility = View.VISIBLE
    revisionInfo.visibility = View.VISIBLE
    deleteButton.visibility = View.VISIBLE
    return false
  }

  private fun setPage(): Boolean {
    previewFragment?.removeRevision()
    onPageRestore()
    return false
  }

  private fun deleteRevision(): Boolean {
    viewModel.deleteRevision(revisionId)
    onPageRestore()
    (activity as PageActivity).showMessage(R.string.message_revision_deleted)
    return true
  }

  private fun restoreRevision(): Boolean {
    viewModel.restoreFromRevision(revisionId)
    onPageRestore()
    (activity as PageActivity).showMessage(R.string.message_revision_restored)
    return false
  }

  private fun onPageRestore() {
    pager.setOnTouchListener(null)
    drawer?.setSelectionAtPosition(0, false)
    revisionInfo.visibility = View.GONE
    deleteButton.visibility = View.GONE
    restoreButton.visibility = View.GONE
  }

  inner class PagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
    override fun getItem(pos: Int): Fragment {
      return (if (pos == 0 && !editorDefault || pos == 1 && editorDefault)
        PagePreviewFragment.newInstance() else PageEditFragment.newInstance()).apply {
        arguments = Bundle().apply { putInt("id", pageId) }
      }
    }

    override fun instantiateItem(container: ViewGroup?, position: Int): Any {
      val editorDefault = prefs.getBoolean(getString(R.string.preference_default_to_editor), false)
      val f = super.instantiateItem(container, position)
      if (position == 0 && !editorDefault || position == 1 && editorDefault)
        previewFragment = f as PagePreviewFragment
      return f
    }

    override fun getCount(): Int = 2
  }

  companion object {
    fun newInstance(): PageDetailFragment = PageDetailFragment()
  }
}

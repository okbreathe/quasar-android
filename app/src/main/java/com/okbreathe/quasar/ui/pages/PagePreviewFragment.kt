package com.okbreathe.quasar.ui.pages

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.okbreathe.quasar.Application
import com.okbreathe.quasar.R
import com.okbreathe.quasar.data.models.Page
import com.okbreathe.quasar.data.models.Revision
import com.okbreathe.quasar.lib.commonmark_ext_media.MediaExtension
import im.ene.toro.Toro
import org.jetbrains.anko.*
import org.jetbrains.anko.support.v4.UI
import org.commonmark.parser.Parser
import org.commonmark.ext.gfm.tables.TablesExtension
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension
import org.commonmark.ext.autolink.AutolinkExtension
import org.commonmark.ext.ins.InsExtension
import org.commonmark.node.*
import org.jetbrains.anko.recyclerview.v7.recyclerView

class PagePreviewFragment : AbstractPageFragment() {
  val TAG = "QSR:PagePreviewFragment"
  private lateinit var viewModel: PageDetailViewModel
  private lateinit var listAdapter: PageDetailAdapter
  private lateinit var content: RecyclerView
  private var page: Page? = null
  private var revision: Revision? = null
  private val extensions = listOf(
    MediaExtension.create(),
    TablesExtension.create(),
    StrikethroughExtension.create(),
    AutolinkExtension.create(),
    InsExtension.create()
  )
  private val parser = Parser.builder().extensions(extensions).build()
  private var pageId: Int = -1

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    pageId = arguments?.getInt("id") ?: -1
    viewModel = PageDetailViewModel((activity.application as Application), pageId)
    listAdapter = PageDetailAdapter(activity, viewModel, extensions)
  }

  override fun onResume() {
    super.onResume()
    listAdapter.notifyDataSetChanged()
    Toro.register(content)
  }

  override fun onPause() {
    Toro.unregister(content)
    super.onPause()
  }

  override fun onCreateView(inf: LayoutInflater?, cont: ViewGroup?, inst: Bundle?): View? = render().view

  fun setRevision(revision: Revision) = updateUI(null, revision.content)

  fun removeRevision() {
    mListener?.onToggleRevision(false)
    updateUI(null, page?.content)
  }

  private fun render(): AnkoContext<Fragment> {
    return UI {
      relativeLayout {
        padding = dip(10)

        content = recyclerView {
          layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
          adapter = listAdapter
        }.lparams(width = matchParent, height = wrapContent) {
          below(R.id.pageTitle)
        }

        viewModel.getPage()
          .subscribe({
            it.observable().subscribe({
              page = it
              (parentFragment as PageDetailFragment).setTitle(page?.title)
              updateUI(page?.title, page?.content)
            }, { Log.e(TAG, Log.getStackTraceString(it)) })
          })
      }
    }
  }

  private fun updateUI(pageTitle: String?, content: String? = null) {
    val document = parser.parse(if (content.isNullOrBlank()) getString(R.string.title_no_content) else content)
    val nodes = mutableListOf<Node>()
    var node = document.firstChild

    while(node != null) {
      nodes.add(node)
      node = node.next
    }

    listAdapter.replace(nodes)
    (parentFragment as PageDetailFragment).setTitle(pageTitle)
  }

  companion object {
    fun newInstance() = PagePreviewFragment()
  }
}

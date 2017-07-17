package com.okbreathe.quasar.ui.pages

import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.graphics.ColorUtils
import android.text.InputType
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.okbreathe.quasar.Application
import com.okbreathe.quasar.R
import com.okbreathe.quasar.data.models.Page
import com.okbreathe.quasar.components.tagging.TagTextView
import com.okbreathe.quasar.components.highlighter.EditTextHighlighter
import com.okbreathe.quasar.components.highlighter.grammars.MarkdownGrammar
import com.okbreathe.quasar.util.setCursorColors
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.*
import org.jetbrains.anko.custom.ankoView
import org.jetbrains.anko.support.v4.UI

class PageEditFragment : AbstractPageFragment() {
  lateinit var viewModel: PageDetailViewModel
  lateinit var title: EditText
  lateinit var content: EditText
  lateinit var tags: TagTextView
  lateinit var highlighter: EditTextHighlighter
  val TAG = "QSR:PageEditFragment"
  var page: Page? = null
  var pageId: Int = -1

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    pageId = arguments?.getInt("id") ?: -1
    viewModel = PageDetailViewModel((activity.application as Application), pageId)
  }

  override fun onResume() {
    super.onResume()
    val primary = viewModel.ui.primaryColor
    val hint = ColorUtils.setAlphaComponent(primary, 150)
    val contentHint = ColorUtils.setAlphaComponent(viewModel.editorTheme.foregroundColor, 150)
    title.textColor = primary
    tags.textColor = primary
    title.hintTextColor = hint
    tags.hintTextColor = hint
    content.hintTextColor = contentHint
    setCursorColors(listOf(title,tags,content), viewModel.ui.accentColor)
  }

  override fun onCreateView(inf: LayoutInflater?, cont: ViewGroup?, inst: Bundle?): View? = render().view

  override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    highlighter = EditTextHighlighter(content, MarkdownGrammar(), viewModel.editorTheme)
    viewModel.getPage()
      .subscribe({
        it.observable().subscribe({
          page = it
          (parentFragment as PageDetailFragment).setTitle(it.title)
          title.setText(it.title)
          content.setText(it.content)
          tags.selected = viewModel.tagNamesFor(it)
        })
      }, { err -> Log.e(TAG,  err.message) })
  }

  private fun render(): AnkoContext<Fragment> {
    val trans = Color.parseColor("#00000000")
    return UI {
      relativeLayout {
        title = editText {
          id = R.id.pageTitle
          padding = dip(10)
          hint = getString(R.string.hint_title)
          inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
          backgroundColor = trans
          setOnFocusChangeListener { _, hasFocus -> if (!hasFocus) save() }
        }.lparams(width = matchParent, height = dip(50)) { alignParentTop() }
        tags = ankoView({ TagTextView(it, available = viewModel.tagNames()) }, 0) {
          id = R.id.pageTags
          padding = dip(10)
          hint = getString(R.string.hint_tags)
          backgroundColor = trans
          onLostFocus = { viewModel.setTags(page, it) }
        }.lparams(width = matchParent, height = dip(50)) { below(R.id.pageTitle) }
        content = editText {
          id = R.id.pageContent
          padding = dip(10)
          inputType = InputType.TYPE_CLASS_TEXT
          singleLine = false
          hint = getString(R.string.hint_content)
          gravity = Gravity.TOP
          setTextSize(TypedValue.COMPLEX_UNIT_SP, viewModel.editorFontSize)
          setOnFocusChangeListener { _, hasFocus -> if (!hasFocus) save() }
        }.lparams(width = matchParent, height = matchParent) { below(R.id.pageTags) }
      }
    }
  }

  private fun save() {
    page?.let {
      viewModel.updatePage(it, title.text.toString(), content.text.toString())
    }
  }

  companion object {
    fun newInstance() = PageEditFragment()
  }
}

package com.okbreathe.quasar.components.highlighter

import android.os.Handler
import android.text.*
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.widget.EditText
import com.okbreathe.quasar.components.highlighter.grammars.Grammar
import com.okbreathe.quasar.components.highlighter.themes.Theme
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.textColor

class EditTextHighlighter(editText: EditText, grammar: Grammar, theme: Theme) {
  private var initialLoad = true
  private var delay = 0
  private var modified = true
  private val updateHandler = Handler()
  private val updateRunnable = Runnable { highlightWithoutChange(editText.editableText, grammar, theme) }

  init {
    editText.backgroundColor = theme.backgroundColor
    editText.textColor = theme.foregroundColor
    editText.addTextChangedListener(object : TextWatcher {
      override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
      override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
      override fun afterTextChanged(e: Editable) {
        cancelUpdate()
        if (!modified) return
        modified = true
        updateHandler.postDelayed(updateRunnable, if(initialLoad) 0 else delay.toLong())
        initialLoad = false
      }
    })
  }

  private fun cancelUpdate() = updateHandler.removeCallbacks(updateRunnable)

  private fun highlightWithoutChange(e: Editable, g: Grammar, t: Theme) {
    modified = false
    highlight(e, g, t)
    modified = true
  }

  private fun highlight(e: Editable, g: Grammar, t: Theme): Editable {
    clearSpans(e)
    if (e.isEmpty()) return e

    val exclusive = Spannable.SPAN_EXCLUSIVE_EXCLUSIVE

    // Todo only use enabled matchers (i.e. provide null for other matchers) and just bind
    // these up in a loop into lambdas rather than explicitly writing it out

    with (g.numberPattern().matcher(e)) {
      while (find()) e.setSpan(ForegroundColorSpan(t.numberColor), start(), end(), exclusive)
    }

    with(g.preprocessorPattern().matcher(e)) {
      while (find()) e.setSpan(ForegroundColorSpan(t.preprocessorColor), start(), end(), exclusive)
    }

    with(g.keywordPattern().matcher(e)) {
      while (find()) e.setSpan(ForegroundColorSpan(t.keywordColor), start(), end(), exclusive)
    }

    with(g.builtinPattern().matcher(e)) {
      while (find()) e.setSpan(ForegroundColorSpan(t.builtinColor), start(), end(), exclusive)
    }

    with(g.commentPattern().matcher(e)) {
      while (find()) e.setSpan(ForegroundColorSpan(t.commentColor), start(), end(), exclusive)
    }

    with(g.stringPattern().matcher(e)) {
      while (find()) e.setSpan(ForegroundColorSpan(t.stringColor), start(), end(), exclusive)
    }

    with(g.specialPattern().matcher(e)) {
      while (find()) e.setSpan(ForegroundColorSpan(t.specialColor), start(), end(), exclusive)
    }

    with(g.identifierPattern().matcher(e)) {
      while (find()) e.setSpan(ForegroundColorSpan(t.identifierColor), start(), end(), exclusive)
    }

    return e
  }

  // Remove foreground and background color spans
  private fun clearSpans(e: Editable) {
    let {
      val spans = e.getSpans(0, e.length, ForegroundColorSpan::class.java)
      var n = spans.size
      while (n-- > 0) e.removeSpan(spans[n])
    }

    let {
      val spans = e.getSpans(0, e.length, BackgroundColorSpan::class.java)
      var n = spans.size
      while (n-- > 0) e.removeSpan(spans[n])
    }
  }
}

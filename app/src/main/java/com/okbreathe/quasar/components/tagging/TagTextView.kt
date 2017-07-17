package com.okbreathe.quasar.components.tagging

import android.content.Context
import android.graphics.*
import android.widget.TextView
import android.text.*
import android.util.Log
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputConnectionWrapper
import android.widget.ArrayAdapter
import android.widget.Filter
import com.okbreathe.quasar.R

class TagTextView(context: Context, available: List<String>, selected: List<String>? = null)
  : android.support.v7.widget.AppCompatMultiAutoCompleteTextView(context) {
  var onTagChanged: ((List<String>) -> Unit)? = null
  var onLostFocus: (List<String>) -> Unit = { _ -> Unit }
  val adapter = TagAdapter(context, android.R.layout.simple_dropdown_item_1line, available, selected)
  var selected: List<String>? = selected
    set(value) {
      field = value
      setText(this.tagString(value))
      setTags()
    }

  init {
//    movementMethod = ClickableMovementMethod.Companion.instance
    setAdapter(adapter)
    setTokenizer(CommaTokenizer())
    setOnFocusChangeListener { _, hasFocus -> if (!hasFocus) { onLostFocus(setTags()) } }
    addTextChangedListener(object : TextWatcher {
      var textChanged = false
      var lastString = ""

      override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

      override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        textChanged = false
        if (shouldUpdateTags(s, start, count)) {
          lastString = s.toString()
          textChanged = true
        }
      }

      override fun afterTextChanged(s: Editable?) {
        if (textChanged) setTags()
        // TODO Figure out a way to perform this without doing it every key stroke
        adapter.setSelected(tagList(s)) // Remove selected tags from available options
        textChanged = false
      }

      // This behaves wonky
      fun shouldUpdateTags(s: CharSequence?, start: Int, count: Int): Boolean {
        return count >= 1 &&
          s.toString() != lastString &&
          (s.toString()[start] == ',' || Regex("[^,]+,\\s*$").matches(s.toString().substring(start, start + count)))
      }
    })
  }

  private fun setTags(): List<String> {
    val chunks = tagList(text)
    val str = this.tagString(chunks)
    val ssb = SpannableStringBuilder(str)
    var pos = 0

    for (c in chunks) {
      // Replaces existing text with spans
      ssb.setSpan(getSpanned(c), pos, pos + c.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
      pos += c.length + 2
    }

    text = ssb // set tags span
    setSelection(str.length) // move cursor to last
    onTagChanged?.invoke(chunks) // Perform callback
    return chunks
  }

  private fun tagList(obj: Any?): List<String> =
    obj.toString().split(",").map { it.trim() }.filter { it.isNotBlank() }

  private fun tagString(tags: List<String>?): String =
    when {
      tags == null -> ""
      tags.isEmpty() -> ""
      else -> tags.filter { it.isNotBlank() }.joinToString(separator = ", ", postfix = ", ")
    }

  private fun getSpanned(str: String): TextReplacementSpan {
    val tv = TextView(context).apply {
      setBackgroundResource(R.drawable.oval)
      setTextColor(Color.parseColor("#000000"))
      text = str
    }
    return TextReplacementSpan(context, tv)
  }
}

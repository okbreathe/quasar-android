package com.okbreathe.quasar.components.tagging

// http://www.programering.com/a/MTMxgjMwATg.html
class ClickableMovementMethod : android.text.method.LinkMovementMethod() {
  override fun onTouchEvent(widget: android.widget.TextView, buffer: android.text.Spannable, event: android.view.MotionEvent): Boolean {
    val action = event.action

    if (action == android.view.MotionEvent.ACTION_UP || action == android.view.MotionEvent.ACTION_DOWN) {
      var x = event.x.toInt()
      var y = event.y.toInt()

      x -= widget.totalPaddingLeft
      y -= widget.totalPaddingTop

      x += widget.scrollX
      y += widget.scrollY

      val layout = widget.layout
      val line = layout.getLineForVertical(y)
      val off = layout.getOffsetForHorizontal(line, x.toFloat())

      val link = buffer.getSpans(off, off, android.text.style.ClickableSpan::class.java)
      val imageSpans = buffer.getSpans(off, off, TextReplacementSpan::class.java)

      if (link.isNotEmpty()) {
        if (action == android.view.MotionEvent.ACTION_UP) {
          link[0].onClick(widget)
        } else if (action == android.view.MotionEvent.ACTION_DOWN) {
          android.text.Selection.setSelection(buffer,
            buffer.getSpanStart(link[0]),
            buffer.getSpanEnd(link[0]))
        }

        return true
      } else if (imageSpans.isNotEmpty()) {
        if (action == android.view.MotionEvent.ACTION_UP) {
          imageSpans[0].onClick(widget)
        } else if (action == android.view.MotionEvent.ACTION_DOWN) {
          android.text.Selection.setSelection(buffer,
            buffer.getSpanStart(imageSpans[0]),
            buffer.getSpanEnd(imageSpans[0]))
        }

        return true
      } else {
        android.text.Selection.removeSelection(buffer)
      }
    }

    return false
  }

  companion object {
    private var sInstance: com.okbreathe.quasar.components.tagging.ClickableMovementMethod? = null

    val instance: com.okbreathe.quasar.components.tagging.ClickableMovementMethod
      get() {
        if (com.okbreathe.quasar.components.tagging.ClickableMovementMethod.Companion.sInstance == null) {
          com.okbreathe.quasar.components.tagging.ClickableMovementMethod.Companion.sInstance = com.okbreathe.quasar.components.tagging.ClickableMovementMethod()
        }
        return com.okbreathe.quasar.components.tagging.ClickableMovementMethod.Companion.sInstance as com.okbreathe.quasar.components.tagging.ClickableMovementMethod
      }
  }
}

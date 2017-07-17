package com.okbreathe.quasar.lib.commonmark_ext_media

import android.webkit.MimeTypeMap
import org.commonmark.node.CustomBlock

class MediaBlock(val title: String?, val url: String?) : CustomBlock() {
  var contentType: String? = mimeType(url)

  companion object {
    fun mimeType(url: String?): String? {
      return MimeTypeMap.getFileExtensionFromUrl(url).let {
        MimeTypeMap.getSingleton().getMimeTypeFromExtension(it)
      }
    }
  }
}

package com.okbreathe.quasar.lib.commonmark_ext_media

import com.okbreathe.quasar.lib.commonmark_ext_media.internal.MediaBlockParser
import com.okbreathe.quasar.lib.commonmark_ext_media.internal.MediaNodeRenderer
import org.commonmark.Extension
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer

class MediaExtension : Parser.ParserExtension, HtmlRenderer.HtmlRendererExtension {
  override fun extend(parserBuilder: Parser.Builder?) {
     parserBuilder?.customBlockParserFactory(MediaBlockParser.Factory())
  }

  override fun extend(rendererBuilder: HtmlRenderer.Builder?) {
    rendererBuilder?.nodeRendererFactory({ context -> MediaNodeRenderer(context) })
  }

  companion object {
    fun create(): Extension = MediaExtension()
  }
}

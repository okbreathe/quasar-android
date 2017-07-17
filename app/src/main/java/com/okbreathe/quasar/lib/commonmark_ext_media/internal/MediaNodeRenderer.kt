package com.okbreathe.quasar.lib.commonmark_ext_media.internal

import com.okbreathe.quasar.lib.commonmark_ext_media.MediaBlock
import org.commonmark.node.Node
import org.commonmark.renderer.NodeRenderer
import org.commonmark.renderer.html.HtmlNodeRendererContext
import org.commonmark.renderer.html.HtmlWriter

class MediaNodeRenderer(val ctx: HtmlNodeRendererContext) : NodeRenderer {
  val writer: HtmlWriter = ctx.writer

  override fun render(node: Node) {
    val block = node as MediaBlock
    with(writer) {
      line()
      tag("p", getAttributes(node, "p"))
      text("MEDIA(url=${block.url}, title=${block.title}, contentType=${block.contentType})")
      tag("/p")
      line()
    }
  }

  override fun getNodeTypes(): Set<Class<out Node>> {
    return setOf<Class<out Node>>(MediaBlock::class.java)
  }

  private fun getAttributes(node: Node, tagName: String ): Map<String, String> {
    return ctx.extendAttributes(node, tagName, emptyMap<String, String>())
  }
}

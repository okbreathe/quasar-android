package com.okbreathe.quasar.ui.pages

import android.content.Context
import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.text.Html
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.drawable.ScalingUtils
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder
import com.facebook.drawee.view.SimpleDraweeView
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.okbreathe.quasar.R
import com.okbreathe.quasar.lib.codeview_android.CodeHighlighter
import com.okbreathe.quasar.lib.codeview_android.color
import com.okbreathe.quasar.lib.commonmark_ext_media.MediaBlock
import im.ene.toro.BaseAdapter
import im.ene.toro.ToroAdapter
import im.ene.toro.exoplayer2.ExoPlayerHelper
import im.ene.toro.exoplayer2.ExoPlayerView
import im.ene.toro.extended.ExtPlayerViewHolder
import im.ene.toro.PlayerManager
import io.github.kbiakov.codeview.classifier.CodeClassifier
import io.github.kbiakov.codeview.extractLines
import io.github.kbiakov.codeview.highlight.MonoFontCache
import io.github.kbiakov.codeview.html
import org.commonmark.Extension
import org.commonmark.node.FencedCodeBlock
import org.commonmark.node.IndentedCodeBlock
import org.commonmark.renderer.html.HtmlRenderer
import org.commonmark.node.Node
import org.commonmark.renderer.spannable.SpannableRenderer
import org.jetbrains.anko.*

class PageDetailAdapter(val context: Context, val viewModel: PageDetailViewModel, extensions: List<Extension>) :
  BaseAdapter<ToroAdapter.ViewHolder>(), PlayerManager {
  val TAG = "QSR:PageDetailAdapter"
  private val htmlRenderer = HtmlRenderer.builder().extensions(extensions).build()
  private val spanRenderer = SpannableRenderer.builder(context).build()
  private var list: List<Node> = listOf()

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
      when (viewType) {
        ViewType.VIDEO.ordinal ->
          VideoHolder(LayoutInflater.from(parent.context).inflate(R.layout.video, parent, false))
        ViewType.IMAGE.ordinal ->
          ImageHolder(SimpleDraweeView(parent.context).apply {
            verticalPadding = context.dip(0)
            layoutParams = ViewGroup.LayoutParams(matchParent, wrapContent)
            adjustViewBounds = true
            hierarchy = draweeHierarchy(parent)
          })
        ViewType.CODE.ordinal -> {
          val params = LinearLayout.LayoutParams(matchParent, wrapContent)
          params.setMargins(0,0,0,50)
          CodeHolder(TextView(parent.context).apply {
            verticalPadding = context.dip(0)
            setPadding(0, context.dip(10), 0, 0)
            layoutParams = params
            typeface = monoTypeface()
          })
        }
        else ->
          TextHolder(TextView(parent.context).apply {
            verticalPadding = context.dip(0)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, viewModel.previewFontSize)
            layoutParams = ViewGroup.LayoutParams(matchParent, wrapContent)
          })
      }

  override fun getItem(position: Int): Any? = list[position]

  override fun getItemViewType(position: Int): Int {
    val block = list[position]
    return when(block) {
      is MediaBlock  -> if (mediaIsVideo(block)) ViewType.VIDEO.ordinal else ViewType.IMAGE.ordinal
      is FencedCodeBlock, is IndentedCodeBlock -> ViewType.CODE.ordinal
      else -> ViewType.TEXT.ordinal
    }
  }

  override fun getItemCount(): Int  = list.size

  fun replace(list: List<Node>) {
    this.list = list
    notifyDataSetChanged()
  }

  private fun monoTypeface() = MonoFontCache.getInstance(context).typeface

  private fun draweeHierarchy(parent: ViewGroup) =
    GenericDraweeHierarchyBuilder(parent.resources)
      .setFadeDuration(300)
      .setActualImageScaleType(ScalingUtils.ScaleType.FIT_CENTER)
      .build()

  private fun mediaIsVideo(blk: MediaBlock): Boolean = blk.contentType?.split("/")?.first() == "video"

  enum class ViewType { TEXT, IMAGE, VIDEO, CODE }

  /**
   * Holder for Code Blocks
   */
  inner class CodeHolder(val view: TextView) : ToroAdapter.ViewHolder(view) {
    override fun onAttachedToWindow(){}

    override fun onDetachedFromWindow(){}

    override fun bind(adapter: RecyclerView.Adapter<*>?, node: Any?) {
      view.text = generateCode(node as Node)
      view.setBackgroundColor(viewModel.codeTheme.bgContent.color())
    }

    private fun generateCode(node: Node): Spanned =
      when (node) {
        is FencedCodeBlock -> generateCode(node)
        is IndentedCodeBlock -> generateCode(node)
        else -> SpannableString("No such block renderer")
      }

    private fun generateCode(cb: FencedCodeBlock): Spanned {
      val lang = if (cb.info.isEmpty()) CodeClassifier.DEFAULT_LANGUAGE else cb.info
      return generateCode(cb.literal, lang)
    }

    private fun generateCode(cb: IndentedCodeBlock): Spanned =
      generateCode(cb.literal, CodeClassifier.DEFAULT_LANGUAGE)

    private fun generateCode(code: String, lang: String): Spanned =
      html(extractLines(code).map {
        CodeHighlighter.highlight(lang, it, viewModel.codeTheme)
      }.joinToString("<br>"))
  }

  /**
   * Holder for Text Blocks
   */
  inner class TextHolder(val textView: TextView) : ToroAdapter.ViewHolder(textView) {
    override fun bind(adapter: RecyclerView.Adapter<*>?, `object`: Any?) {
      val node = `object` as Node
      textView.apply {
        val ui = viewModel.ui
        text = renderHtml(node)
        textColor = ui.primaryColor
        linkTextColor = ui.accentColor
        movementMethod = LinkMovementMethod.getInstance()
      }
    }

    override fun onDetachedFromWindow() {}

    override fun onAttachedToWindow() {}

    private fun renderHtml(node: Node): Spanned = Html.fromHtml( htmlRenderer.render(node) )

    private fun renderSpan(node: Node) = SpannableStringBuilder().apply {
      spanRenderer.render(node, this)
    }
  }

  /**
   * Holder for Image Blocks
   */
  class ImageHolder(val view: SimpleDraweeView) : ToroAdapter.ViewHolder(view) {
    override fun onAttachedToWindow() {}
    override fun onDetachedFromWindow() {}

    override fun bind(adapter: RecyclerView.Adapter<*>?, `object`: Any?) {
      view.controller = draweeController((`object` as MediaBlock).url)
    }

    private fun draweeController(url: String?) =
      Fresco.newDraweeControllerBuilder()
        .setUri(url)
        .setAutoPlayAnimations(true)
        .build()
  }

  /**
   * Holder for Video Blocks
   */
  inner class VideoHolder(val view: View) : ExtPlayerViewHolder(view) {
    var videoItem: MediaBlock? = null
    var source: MediaSource? = null
      private set

    override fun onBind(adapter: RecyclerView.Adapter<*>?, item: Any?) {
      this.videoItem = item as MediaBlock
      this.source = ExoPlayerHelper.buildMediaSource(
        view.context,
        Uri.parse(item.url),
        DefaultDataSourceFactory(view.context, Util.getUserAgent(view.context, "Quasar")),
        view.handler,
        null
      )
    }

    override fun setOnItemClickListener(listener: View.OnClickListener?) {
      super.setOnItemClickListener(listener)
      itemView.setOnClickListener {
        player?.let { if (it.isPlaying) it.pause() else it.start() }
      }
    }

    override fun onVideoPrepared() {
      super.onVideoPrepared()
      pause()
    }

    override fun getMediaSource(): MediaSource = source!!

    override fun getMediaId(): String? = videoItem?.url + "@$adapterPosition"

    override fun findVideoView(itemView: View?): ExoPlayerView =
      (itemView?.findViewById(com.okbreathe.quasar.R.id.video)) as ExoPlayerView
  }
}

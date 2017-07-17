package com.okbreathe.quasar.lib.codeview_android

import android.graphics.Color
import io.github.kbiakov.codeview.highlight.parser.ParseResult
import io.github.kbiakov.codeview.highlight.prettify.PrettifyParser
import io.github.kbiakov.codeview.highlight.ColorThemeData
import java.util.*

/**
 * Code highlighter is parses content & inserts necessary font tags
 * accordingly to specified programming language & color theme.
 *
 * @author Kirill Biakov
 */
object CodeHighlighter {

  private val LT_BRACE = "<".toRegex()
  private val LT_REGULAR = "&lt;"
  private val LT_TMP = "^"

  private val parser = PrettifyParser()

  /**
   * Highlight code content.
   *
   * @param codeLanguage Programming language
   * @param rawSource Code source by one string
   * @param colorTheme Color theme (see below)
   * @return Highlighted code, string with necessary inserted color tags
   */
  fun highlight(codeLanguage: String, rawSource: String, colorTheme: ColorThemeData): String {
    val source = rawSource.escapeLT()
    val results = parser.parse(codeLanguage, source)
    val colorsMap = buildColorsMap(colorTheme)
    val highlighted = StringBuilder()

    for (result in results) {
      val color = colorsMap.getColor(result)
      val content = parseContent(source, result).replace(Regex("""^\s+""")) {
        it.value.fold(""){ acc, char -> acc + "\u00A0"}
      }
      highlighted.append(content.withFontParams(color))
    }

    return highlighted.toString()
  }

  // - Helpers

  /**
   * Parse user input by extracting highlighted content.
   *
   * @param codeContent Code content
   * @param result Syntax unit
   * @return Parsed content to highlight
   */
  private fun parseContent(codeContent: String, result: ParseResult): String {
    val length = result.offset + result.length
    val content = codeContent.substring(result.offset, length)
    return content.expandLT()
  }

  /**
   * Color accessor from built color map for selected color theme.
   *
   * @param result Syntax unit
   * @return Color for syntax unit
   */
  private fun HashMap<String, String>.getColor(result: ParseResult) =
    this[result.styleKeys[0]] ?: this["pln"]

  /**
   * Build fast accessor (as map) for selected color theme.
   *
   * @param colorTheme Color theme
   * @return Colors map built from color theme
   */
  private fun buildColorsMap(colorTheme: ColorThemeData) =
    object : HashMap<String, String>() {
      init {
        val syntaxColors = colorTheme.syntaxColors

        put("typ", syntaxColors.type.hex())
        put("kwd", syntaxColors.keyword.hex())
        put("lit", syntaxColors.literal.hex())
        put("com", syntaxColors.comment.hex())
        put("str", syntaxColors.string.hex())
        put("pun", syntaxColors.punctuation.hex())
        put("pln", syntaxColors.plain.hex())
        put("tag", syntaxColors.tag.hex())
        put("dec", syntaxColors.declaration.hex())
        put("src", syntaxColors.plain.hex())
        put("atn", syntaxColors.attrName.hex())
        put("atv", syntaxColors.attrValue.hex())
        put("nocode", syntaxColors.plain.hex())
      }
    }

  // - Escaping/extracting "lower then" symbol

  private fun String.escapeLT() = replace(LT_BRACE, LT_TMP)
  private fun String.expandLT() = replace(LT_TMP, LT_REGULAR)
}

/**
 * @return Converted hex int to color by adding alpha-channel
 */
fun Int.color() = try {
  Color.parseColor("#FF${Integer.toHexString(this)}")
} catch (e: IllegalArgumentException) {
  this
}

/**
 * @return Converted hex int to hex string
 */
fun Int.hex() = "#${Integer.toHexString(this)}"

/**
 * @return Is value equals to found or not condition
 */
fun Int.isFound() = this >= 0

fun Int.notFound() = this == -1

/**
 * Apply font params to string.
 *
 * @param color Color as formatter string
 * @return Formatted string
 */
fun String.withFontParams(color: String?): String {
  val parametrizedString = StringBuilder()

  var idx = 0
  var newIdx = indexOf("\n")

  if (newIdx.notFound()) // covers expected tag coverage (within only one line)
    parametrizedString.append(inFontTag(color))
  else { // may contain multiple lines with line breaks

    // put tag on the borders (end & start of line, ..., end of tag)
    do { // until closing tag is reached
      val part = substring(idx..newIdx - 1).inFontTag(color).plus("\n")
      parametrizedString.append(part)

//      val indented = content.replace(Regex("^\\s+")) { it.value.fold(""){ acc, char -> acc + "*"} }
//      Log.d(TAG, "INDENTED: '${indented}'")
      idx = newIdx + 1
      newIdx = indexOf("\n", idx)
    } while (newIdx.isFound())

    if (idx != indexOf("\n")) // if not replaced only once (for multiline tag coverage)
      parametrizedString.append(substring(idx).inFontTag(color))
  }

  return parametrizedString.toString()
}

/**
 * @return String with escaped line break at start
 */
fun String.escLineBreakAtStart() =
  if (startsWith("\n") && length >= 1)
    substring(1)
  else this

/**
 * @return String surrounded by font tag
 */
fun String.inFontTag(color: String?) =
  "<font color=\"$color\">${escLineBreakAtStart()}</font>"

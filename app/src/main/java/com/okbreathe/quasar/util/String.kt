package com.okbreathe.quasar.util

fun titleize(str: String): String =
  str.split(" ").map { s -> s[0].toUpperCase() + s.substring(1).toLowerCase() }.joinToString(" ")

fun truncate(str: String?, length: Int = 50, separator: Char = ' ', more: String = "..."): String =
  str?.let {
    if (str.length < length) str else str.slice(0..length+more.length).dropLastWhile { it != separator  }
  } + more ?: ""
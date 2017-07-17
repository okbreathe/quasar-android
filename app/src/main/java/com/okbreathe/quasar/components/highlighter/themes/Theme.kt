package com.okbreathe.quasar.components.highlighter.themes

interface Theme {
  val name: String
    get() = this.javaClass.name

  val foregroundColor: Int

  val backgroundColor: Int

  val reservedColor: Int

  val keywordColor: Int

  val builtinColor: Int

  val typeColor: Int

  val constantColor: Int

  val stringColor: Int

  val numberColor: Int

  val floatColor: Int

  val booleanColor: Int

  val specialColor: Int

  val identifierColor: Int

  val preprocessorColor: Int

  val commentColor: Int

  val underlineColor: Int

  val todoColor: Int
}

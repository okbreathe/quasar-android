package com.okbreathe.quasar.data.models

import io.requery.*

@io.requery.Entity
interface PageTag : android.os.Parcelable, io.requery.Persistable {
  @get:ForeignKey(references = Page::class)
  @get:Key
  @get:Column(name = "page_id")
  var pageId: Int

  @get:ForeignKey(references = Tag::class)
  @get:Column(name = "tag_id")
  @get:Key
  var tagId: Int
}

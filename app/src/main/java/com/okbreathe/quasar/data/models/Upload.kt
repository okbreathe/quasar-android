package com.okbreathe.quasar.data.models

import android.os.Parcelable
import io.requery.*

@io.requery.Entity
interface Upload : android.os.Parcelable, io.requery.Persistable {
  @get: io.requery.Key
  @get: io.requery.Generated
  var id: Int
  @get:Column(name = "content_type")
  var contentType: String
  @get:Column(name = "uploaded_as")
  var uploadedAs: String
  var file: String
}

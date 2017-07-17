package com.okbreathe.quasar.data.models

import io.requery.*

@Entity
interface Search : android.os.Parcelable, io.requery.Persistable {
  var docid: Int
  var content: String
}

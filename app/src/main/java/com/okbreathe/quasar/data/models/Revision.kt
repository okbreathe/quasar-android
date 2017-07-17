package com.okbreathe.quasar.data.models

import io.requery.*
import java.sql.Timestamp
import java.util.*

@Entity
interface Revision : android.os.Parcelable, io.requery.Persistable {
  @get:Key
  @get:Generated
  var id: Int

  @get:ManyToOne
  @get:Index("revisions_page_id_idx")
  @get:Column(name = "page_id", nullable = false)
  val page: Page

  @get:Index("revisions_remote_id_idx")
  @get:Column(name = "remote_id", nullable = true)
  var remoteId: Int

  var event: String

  var content: String

  @get:Column(value = "f", nullable = false)
  var snapshot: Boolean

  @get:Column(name = "is_deleted", value = "f", nullable = false)
  var isDeleted: Boolean

  @get:Column(name = "inserted_at", nullable = false, value = "(DATETIME('now'))")
  var insertedAt: Timestamp
}

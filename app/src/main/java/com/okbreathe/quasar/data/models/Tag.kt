package com.okbreathe.quasar.data.models

import io.requery.*
import java.sql.Timestamp
import java.util.*

@Entity
interface Tag : android.os.Parcelable, io.requery.Persistable {
  @get:Key
  @get:Generated
  var id: Int

  @get:Index("tags_remote_id_idx")
  @get:Column(name = "remote_id", nullable = true)
  var remoteId: Int

  @get:Column(nullable = false, unique = true)
  @get:Index("tag_name_idx")
  var name: String

  @get:Column(name = "inserted_at", nullable = false, value = "(DATETIME('now'))")
  var insertedAt: Timestamp

  @get:Column(name = "updated_at", nullable = false, value = "(DATETIME('now'))")
  var updatedAt: Timestamp

  @get:ManyToMany
  val pages: List<Page>
}

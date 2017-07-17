package com.okbreathe.quasar.data.models

import io.requery.*
import io.requery.sql.type.TimeStampType
import java.sql.Timestamp
import java.util.*

@Entity
interface Page : android.os.Parcelable, io.requery.Persistable {
  @get:Key
  @get:Generated
  var id: Int

  @get:Index("pages_remote_id_idx")
  @get:Column(name = "remote_id", nullable = true)
  var remoteId: Int

  var title: String

  var content: String

  @get:Column(name = "is_deleted", value = "f", nullable = false)
  var isDeleted: Boolean

  @get:Column(name = "is_trashed", value = "f", nullable = false)
  var isTrashed: Boolean

  @get:Column(name = "is_favorite", value = "f", nullable = false)
  var isFavorite: Boolean

  @get:Column(name = "inserted_at", nullable = false, value = "(DATETIME('now'))")
  var insertedAt: Timestamp

  @get:Column(name = "updated_at", nullable = false, value = "(DATETIME('now'))")
  var updatedAt: Timestamp

  @get:ManyToMany(cascade = arrayOf(CascadeAction.DELETE))
  @get:JunctionTable(type = PageTag::class)
  var tags: List<Tag>

  @get:OneToMany(cascade = arrayOf(CascadeAction.DELETE))
  var revisions: List<Revision>
}

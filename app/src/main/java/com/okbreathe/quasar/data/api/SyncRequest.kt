package com.okbreathe.quasar.data.api

data class SyncRequest(val pages: List<Page>) {
  data class Page(
    val id: Int,
    val remote_id: Int,
    val is_deleted: Boolean,
    val is_trashed: Boolean,
    val is_favorite: Boolean,
    val title: String,
    val content: String,
    val inserted_at: String,
    val updated_at: String,
    val revisions: List<Revision>,
    val tags: List<Tag>
  )

  data class Revision(
    val id: Int,
    val page_id: Int,
    val remote_id: Int,
    val event: String,
    val content: String,
    val snapshot: Boolean,
    val inserted_at: String
  )

  data class Tag( val name: String )
}


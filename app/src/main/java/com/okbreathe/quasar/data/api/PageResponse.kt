package com.okbreathe.quasar.data.api

@moe.banana.jsonapi2.JsonApi(type = "page")
class PageResponse : moe.banana.jsonapi2.Resource() {
  var title: String? = null

  var content: String? = null

  @com.squareup.moshi.Json(name="inserted_at")
  var insertedAt: String? = null

  @com.squareup.moshi.Json(name="updated_at")
  var updatedAt: String? = null

  @com.squareup.moshi.Json(name="is_trashed")
  var isTrashed: Boolean = false

  @com.squareup.moshi.Json(name="is_favorite")
  var isFavorite: Boolean = false

  @com.squareup.moshi.Json(name="is_deleted")
  var isDeleted: Boolean = false

  var tags: moe.banana.jsonapi2.HasMany<TagResponse>? = null

  var revisions: moe.banana.jsonapi2.HasMany<RevisionResponse>? = null

  fun getTags(): List<TagResponse>? = tags?.get(context)

  fun getRevisions(): List<RevisionResponse>? = revisions?.get(context)
}

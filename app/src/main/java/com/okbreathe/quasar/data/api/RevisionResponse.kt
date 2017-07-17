package com.okbreathe.quasar.data.api

import com.squareup.moshi.Json

@moe.banana.jsonapi2.JsonApi(type = "revision")
class RevisionResponse : moe.banana.jsonapi2.Resource() {
  var name: String? = null

  var remoteId: Int? = null

  var event: String? = null

  var content: String? = null

  @com.squareup.moshi.Json(name="inserted_at")
  var insertedAt: String? = null
}

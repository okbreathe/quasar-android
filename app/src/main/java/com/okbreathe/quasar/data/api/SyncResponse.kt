package com.okbreathe.quasar.data.api

@moe.banana.jsonapi2.JsonApi(type = "device_item")
class SyncResponse : moe.banana.jsonapi2.Resource() {
  @com.squareup.moshi.Json(name="client_id")
  var clientId: Int? = null

  @com.squareup.moshi.Json(name="item_id")
  var itemId: Int? = null

  @com.squareup.moshi.Json(name="item_type")
  var itemType: String? = null
}

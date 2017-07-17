package com.okbreathe.quasar.data.api

import com.squareup.moshi.Json
import moe.banana.jsonapi2.JsonApi
import moe.banana.jsonapi2.Resource

@moe.banana.jsonapi2.JsonApi(type = "tag")
class TagResponse : moe.banana.jsonapi2.Resource() {
  var name: String? = null
}

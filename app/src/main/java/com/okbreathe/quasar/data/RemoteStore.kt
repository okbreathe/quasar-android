package com.okbreathe.quasar.data

import android.preference.PreferenceManager
import android.util.Log
import com.okbreathe.quasar.data.api.*
import com.okbreathe.quasar.lib.jsonapi.JsonApiConverterFactory
import com.squareup.moshi.Moshi
import io.reactivex.Observable
import moe.banana.jsonapi2.ResourceAdapterFactory
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*

interface RemoteStore {
  @Headers("Content-Type: application/json")
  @POST("sessions")
  fun login(@Body request: AccountRequest): Observable<AccountResponse>

  @Headers("Content-Type: application/json")
  @DELETE("sessions")
  fun logout(): Observable<AccountResponse>

  @POST("sessions")
  fun signUp(@Body request: AccountRequest): Observable<AccountResponse>

  @GET("pages")
  fun list(@Header("Authorization") authorization: String):
    Observable<List<PageResponse>>

  @GET("sync/{device_id}")
  fun syncPull(@Header("Authorization") authorization: String, @Path("device_id") device_id: String, @Query("since") since: String):
    Observable<List<PageResponse>>

  @GET("sync/{device_id}")
  fun syncPull(@Header("Authorization") authorization: String, @Path("device_id") device_id: String):
    Observable<List<PageResponse>>

  @Headers("Content-Type: application/vnd.api+json", "Accept: application/vnd.api+json")
  @POST("sync/{device_id}")
  fun syncPush(@Header("Authorization") authorization: String, @Path("device_id") device_id: String, @Body request: SyncRequest):
    Observable<List<SyncResponse>>

  companion object {
    // Standard moshi instance for regular JSON Requests
    val json by lazy { Moshi.Builder() .build() }
    val jsonApi by lazy {
      // JSON API factory
      val jsonApiAdapterFactory = ResourceAdapterFactory.builder()
        .add(PageResponse::class.java)
        .add(TagResponse::class.java)
        .add(RevisionResponse::class.java)
        .build()

      Moshi.Builder()
        .add(jsonApiAdapterFactory)
        .build()
    }

    fun create(url: String): RemoteStore {
      val retrofit = Retrofit.Builder()
          .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
          .addConverterFactory(JsonApiConverterFactory.create(jsonApi))
          .addConverterFactory(MoshiConverterFactory.create(json))
          .baseUrl(url.replace(Regex("\\/+$"), "") + "/api/")
          .build()
      val ret = retrofit.create(RemoteStore::class.java)
      return ret
    }
  }
}

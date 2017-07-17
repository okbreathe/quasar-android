package com.okbreathe.quasar.sync

import android.accounts.Account
import android.accounts.AccountManager
import android.content.*
import android.net.ConnectivityManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.Settings.Secure
import android.util.Log
import com.okbreathe.quasar.Application
import com.okbreathe.quasar.R
import com.okbreathe.quasar.data.ACCOUNT_TYPE
import com.okbreathe.quasar.data.AUTH_TYPE
import com.okbreathe.quasar.data.RemoteStore
import com.okbreathe.quasar.data.SyncStore
import com.okbreathe.quasar.data.api.*
import com.okbreathe.quasar.util.utcDateString
import com.squareup.moshi.JsonAdapter
import moe.banana.jsonapi2.JsonBuffer

class SyncAdapter : AbstractThreadedSyncAdapter {
  val TAG = "QSR:SyncAdapter"
  private var mContentResolver: ContentResolver
  private var preferences: SharedPreferences

  constructor(context: Context, autoInitialize: Boolean) : super(context, autoInitialize) {
    mContentResolver = context.contentResolver
    preferences = PreferenceManager.getDefaultSharedPreferences(context)
  }

  constructor( context: Context, autoInitialize: Boolean, allowParallelSyncs: Boolean) :
    super(context, autoInitialize, allowParallelSyncs) {
    mContentResolver = context.contentResolver
    preferences = PreferenceManager.getDefaultSharedPreferences(context)
  }

/**
 * Sync Flow:
 * - Device Requests SyncStore passing device_id, optionally last_sync_at
 * - Server returns pages updated/inserted since last_sync_pull_at or ALL if last_sync_pull_at is null + new last_sync_pull_at time
 * - Create/update new/stale pages
 * - Find all pages where inserted_at or updated_at is newer than DEVICE last_sync_push_at
 * - If success, update DEVICE last_sync_pull_at time, otherwise do nothing
 * - Push Changes to Server
 * - Server returns syncRemote json { syncRemote: [{ client_id: client_id, page_id: page_id}]
 * - Associate pages with remote ids
 * - If success, update DEVICE last_sync_push_at time, otherwise do nothing
 */
  override fun onPerformSync(account: Account, bundle: Bundle, auth: String, provider: ContentProviderClient, result: SyncResult) {
    Log.d(TAG, "Performing Sync")

    val deviceId = Secure.getString(context.contentResolver, Secure.ANDROID_ID)
    val lastSyncPull = preferences.getString(context.getString(R.string.last_sync_push_at), null)
    val syncStarted = utcDateString()
    val authToken = getToken()
    val app = context.applicationContext as Application
    val syncStore = SyncStore(app.localStore)
    val manual = bundle.getBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, false)

    if (authToken == null) {
      Log.d(TAG, "No AuthToken. Not syncing now.")
      return
    }

    if (!manual && !canSync()) {
      Log.d(TAG, "Not on Wifi. Not syncing now.")
      return
    }

    Log.d(TAG, "Pulling remote updates")

    pullRemoteUpdates(app, authToken, deviceId, lastSyncPull)
      .subscribe(
        { pages ->
          if (applyRemoteUpdates(pages, syncStore) ) {
            pushLocalUpdates(app, authToken, deviceId, syncStore, syncStarted)
            syncStore.updateFTS()
          } else {
            Log.e(TAG, "Apply remote changes failed")
          }
        },
        { err -> Log.e(TAG, Log.getStackTraceString(err))}
      )
  }

  private fun getToken(): String? {
    val accountManager = AccountManager.get(context)
    val accounts = accountManager.getAccountsByType(ACCOUNT_TYPE)
    return if (accounts.isNotEmpty()) accountManager.peekAuthToken(accounts[0], AUTH_TYPE) else null
  }

  private fun pullRemoteUpdates(app: Application, authToken: String, deviceId: String, lastSyncPull: String?) =
    if (lastSyncPull != null)
      app.remoteStore.syncPull(authToken, deviceId, lastSyncPull)
    else
      app.remoteStore.syncPull(authToken, deviceId)

  private fun applyRemoteUpdates(pages: List<PageResponse>, syncStore: SyncStore): Boolean {
    if (pages.isEmpty()) return true

    val adapter:JsonAdapter<MetaResponse> = RemoteStore.json.adapter(MetaResponse::class.java)
    val meta: JsonBuffer<Any?> = pages[0].context.meta
    val metaData = meta.get(adapter)
    val lastSyncAt = metaData.last_sync_at

    if (syncStore.applyRemoteUpdates(pages)) return syncPullSuccessful(lastSyncAt)

    return false
  }

  /**
   * Pushes local changes to the server and associates the local copy with canonical server id
   * This responds with a list of newly created pages and their remote ids
   * We need to update the local store to ensure that they are seen as the
   * same page. We'll also get the data during subsequent data pulls if
   * the connection is interrupted
   */
  private fun pushLocalUpdates(app: Application, authToken: String, deviceId: String, syncStore: SyncStore, syncStarted: String): Boolean {
    val lastSyncPush = preferences.getString(context.getString(R.string.last_sync_push_at), null)
    val pagesToSync = if (lastSyncPush == null) app.localStore.unsyncedPages() else app.localStore.pagesModifiedSince(lastSyncPush)
    if (pagesToSync.isNotEmpty()) {
      Log.d(TAG, "Pushing ${pagesToSync.size} local pages modified since $lastSyncPush")
      app.remoteStore.syncPush(authToken, deviceId, syncStore.generateSyncRequest(pagesToSync))
        .subscribe(
          { resp -> if (syncStore.associateRemoteIds(resp)) syncPushSuccessful(syncStarted) },
          { e -> syncFailure(e) }
        )
    } else {
      syncPushSuccessful(syncStarted)
      Log.d(TAG, "Not pushing changes to remote. No pages updated on device since $lastSyncPush")
    }
    return true
  }

  private fun canSync(): Boolean {
    val wifiOnly = preferences.getBoolean(context.getString(R.string.preference_sync_wifi), false)
    return !wifiOnly || (wifiOnly && onWifi())
  }

  private fun onWifi(): Boolean =
    with(context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager) {
      when (this.activeNetworkInfo.type) {
        ConnectivityManager.TYPE_WIFI -> true
        else -> false
      }
    }

  private fun syncPullSuccessful(lastSyncAt: String): Boolean {
    val editor = preferences.edit()
    editor.putString(context.getString(R.string.last_sync_pull_at), lastSyncAt)
    editor.apply()
    return true
  }

  private fun syncPushSuccessful(lastSyncAt: String): Boolean {
    val editor = preferences.edit()
    editor.putString(context.getString(R.string.last_sync_push_at), lastSyncAt)
    editor.apply()
    return true
  }

  private fun syncFailure(e: Throwable) = Log.e(TAG, "SyncStore Failed: ${e.message}")
}

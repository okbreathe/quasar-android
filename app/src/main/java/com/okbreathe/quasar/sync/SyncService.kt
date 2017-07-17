package com.okbreathe.quasar.sync

import android.app.Service
import android.content.Intent
import android.os.IBinder

class SyncService : Service() {
  override fun onCreate() {
    synchronized(sSyncAdapterLock) {
      if (sSyncAdapter == null) sSyncAdapter = SyncAdapter(applicationContext, true)
    }
  }

  override fun onBind(intent: Intent): IBinder? = sSyncAdapter!!.syncAdapterBinder

  companion object {
    // Storage for an instance of the syncRemote adapter
    private var sSyncAdapter: SyncAdapter? = null
    // Object to use as a thread-safe lock
    private val sSyncAdapterLock = Any()
  }
}

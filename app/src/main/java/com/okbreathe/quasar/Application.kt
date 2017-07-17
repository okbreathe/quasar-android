package com.okbreathe.quasar

import android.preference.PreferenceManager
import com.facebook.drawee.backends.pipeline.Fresco
import com.okbreathe.quasar.data.models.Models
import com.okbreathe.quasar.data.RemoteStore
import com.okbreathe.quasar.data.LocalStore
import com.okbreathe.quasar.data.requery.QuasarSchemaModifier
import com.squareup.leakcanary.LeakCanary
import im.ene.toro.Toro
import io.requery.Persistable
import io.requery.android.sqlite.DatabaseSource
import io.requery.reactivex.KotlinReactiveEntityStore
import io.requery.sql.KotlinEntityDataStore
import io.requery.sql.TableCreationMode

class Application : android.app.Application() {
  val data: KotlinReactiveEntityStore<Persistable> by lazy {
    val source = DatabaseSource(this, Models.DEFAULT, 1).apply {
      setTableCreationMode(TableCreationMode.CREATE_NOT_EXISTS)
      setLoggingEnabled(true)
    }
    QuasarSchemaModifier(source.configuration).createTables(
      if (getDatabasePath("default").exists()) TableCreationMode.CREATE_NOT_EXISTS else TableCreationMode.DROP_CREATE
    )
    KotlinReactiveEntityStore<Persistable>(KotlinEntityDataStore(source.configuration))
  }
  val remoteStore: RemoteStore by lazy {
    val prefs = PreferenceManager.getDefaultSharedPreferences(this)
    RemoteStore.create(prefs.getString(getString(R.string.preference_sync_url), ""))
  }
  val localStore: LocalStore by lazy { LocalStore(data) }

  override fun onCreate() {
    super.onCreate()
    Fresco.initialize(this)
    Toro.init(this)
    if (!LeakCanary.isInAnalyzerProcess(this)) LeakCanary.install(this)
  }
}

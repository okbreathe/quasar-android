package com.okbreathe.quasar.data.requery

import android.util.Log
import io.requery.meta.Type
import io.requery.sql.Configuration
import io.requery.sql.SchemaModifier
import io.requery.sql.TableCreationMode

class QuasarSchemaModifier(configuration: Configuration?) : SchemaModifier(configuration) {
  override fun <T : Any?> tableCreateStatement(type: Type<T>?, mode: TableCreationMode?) =
    if (type?.name == "Search") {
      "CREATE VIRTUAL TABLE IF NOT EXISTS Search USING fts4 (content='Page', content)"
    } else {
      super.tableCreateStatement(type, mode)
    }
}

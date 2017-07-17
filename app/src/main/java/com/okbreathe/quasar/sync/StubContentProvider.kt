package com.okbreathe.quasar.sync

import android.content.ContentValues
import android.database.Cursor
import android.net.Uri

class StubContentProvider : android.content.ContentProvider() {
  override fun onCreate() = true

  override fun getType(uri: Uri): String? {
    // TODO: Implement this to handle requests for the MIME type of the data at the given URI.
    throw UnsupportedOperationException("Not yet implemented")
  }

  override fun query(uri: Uri,
                     projection: Array<String>?,
                     selection: String?,
                     selectionArgs: Array<String>?,
                     sortOrder: String?): Cursor? {
    // TODO: Implement this to handle query requests from clients.
    throw UnsupportedOperationException("Not yet implemented")
  }

  override fun insert(uri: Uri, values: ContentValues?): Uri? {
    // TODO: Implement this to handle requests to insertPage a new row.
    throw UnsupportedOperationException("Not yet implemented")
  }

  override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
    // TODO: Implement this to handle requests to update one or more rows.
    throw UnsupportedOperationException("Not yet implemented")
  }

  override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
    // Implement this to handle requests to trashPage one or more rows.
    throw UnsupportedOperationException("Not yet implemented")
  }
}

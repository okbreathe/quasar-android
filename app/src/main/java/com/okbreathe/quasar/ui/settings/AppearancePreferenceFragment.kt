package com.okbreathe.quasar.ui.settings

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceFragment
import android.view.MenuItem
import com.okbreathe.quasar.R
import com.okbreathe.quasar.ui.settings.SettingsActivity.Companion.bindPreferenceSummaryToValue

class AppearancePreferenceFragment : PreferenceFragment() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    addPreferencesFromResource(R.xml.pref_appearance)
    setHasOptionsMenu(true)
    bindPreferenceSummaryToValue(findPreference("ui"))
    bindPreferenceSummaryToValue(findPreference("theme"))
    bindPreferenceSummaryToValue(findPreference("editor_font_size"))
    bindPreferenceSummaryToValue(findPreference("preview_font_size"))
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    if (item.itemId == android.R.id.home) {
      startActivity(Intent(activity, SettingsActivity::class.java))
      return true
    }
    return super.onOptionsItemSelected(item)
  }
}


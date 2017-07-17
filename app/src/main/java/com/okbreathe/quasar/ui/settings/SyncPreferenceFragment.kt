package com.okbreathe.quasar.ui.settings

import android.accounts.AccountManager
import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceFragment
import android.preference.PreferenceManager
import android.view.MenuItem
import com.okbreathe.quasar.R
import com.okbreathe.quasar.data.ACCOUNT_TYPE
import com.okbreathe.quasar.ui.settings.SettingsActivity.Companion.bindPreferenceSummaryToValue

class SyncPreferenceFragment : PreferenceFragment() {
  lateinit private var prefs: SharedPreferences
  var showSync = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setHasOptionsMenu(true)
    prefs = PreferenceManager.getDefaultSharedPreferences(activity)
    showSync = hasAccount()
    render()
  }

  override fun onResume() {
    super.onResume()
    val acnt = hasAccount()
    // Swap the settings if account status changes
    if (showSync != acnt) {
      showSync = acnt
      render()
    }
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    if (item.itemId == android.R.id.home) {
      startActivity(Intent(activity, SettingsActivity::class.java))
      return true
    }
    return super.onOptionsItemSelected(item)
  }

  private fun render() {
    preferenceScreen = null
    if (showSync) {
      addPreferencesFromResource(R.xml.pref_sync)
      bindPreferenceSummaryToValue(findPreference("sync_url"))
      bindPreferenceSummaryToValue(findPreference("sync_frequency"))
      findPreference("remove_account")?.apply {
        summary = prefs.getString(getString(R.string.preference_email), "user@example.com")
        onPreferenceClickListener = null
        setOnPreferenceClickListener {
          showConfirmation {
            removeAccount()
            render()
          }
          true
        }
      }
    } else {
      addPreferencesFromResource(R.xml.pref_account)
    }
  }

  private fun removeAccount(){
    val am = AccountManager.get(activity)
    if (am.accounts.isNotEmpty()) {
      val handler = Handler()
      if (android.os.Build.VERSION.SDK_INT >= 22) {
        am.removeAccount(am.accounts[0], activity, { showSync = false; render() }, handler)
      } else {
        am.removeAccount(am.accounts[0], { showSync = false; render() }, handler)
      }
    }
  }

  private fun showConfirmation(fn: () -> Unit) {
    AlertDialog.Builder(activity)
      .setTitle(R.string.title_remove_account)
      .setMessage(R.string.message_remove_account)
      .setIcon(android.R.drawable.ic_dialog_alert)
      .setPositiveButton(android.R.string.yes, { dialog, which -> fn() })
      .setNegativeButton(android.R.string.no,null)
      .show()
  }

  private fun hasAccount(): Boolean {
    val availableAccounts = AccountManager.get(activity).getAccountsByType(ACCOUNT_TYPE)
    return availableAccounts.isNotEmpty()
  }
}


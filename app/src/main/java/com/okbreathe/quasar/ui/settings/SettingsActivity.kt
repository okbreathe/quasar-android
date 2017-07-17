package com.okbreathe.quasar.ui.settings

import android.content.Context
import android.content.res.Configuration
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.preference.ListPreference
import android.preference.Preference
import android.preference.PreferenceActivity
import android.preference.PreferenceFragment
import android.preference.PreferenceManager
import android.preference.RingtonePreference
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.okbreathe.quasar.R

class SettingsActivity : AppCompatPreferenceActivity() {
  val TAG = "QSR:SettingsActivity"

  // https://stackoverflow.com/questions/26509180/no-actionbar-in-preferenceactivity-after-upgrade-to-support-library-v21
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val root = findViewById(android.R.id.list).parent.parent.parent as LinearLayout
    val bar = LayoutInflater.from(this).inflate(R.layout.settings_toolbar, root, false) as Toolbar
    root.addView(bar, 0)
    setSupportActionBar(bar)
    bar.setNavigationOnClickListener { finish() }
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
  }

  /**
   * {@inheritDoc}
   */
  override fun onIsMultiPane(): Boolean = isXLargeTablet(this)

  /**
   * {@inheritDoc}
   */
  override fun onBuildHeaders(target: List<PreferenceActivity.Header>) {
    loadHeadersFromResource(R.xml.pref_headers, target)
  }

  /**
   * This method stops fragment injection in malicious applications.
   * Make sure to deny any unknown fragments here.
   */
  override fun isValidFragment(fragmentName: String): Boolean {
    return PreferenceFragment::class.java.name == fragmentName
      || GeneralPreferenceFragment::class.java.name == fragmentName
      || SyncPreferenceFragment::class.java.name == fragmentName
      || AppearancePreferenceFragment::class.java.name == fragmentName
  }

  companion object {
    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     * @see .sBindPreferenceSummaryToValueListener
     */
    fun bindPreferenceSummaryToValue(preference: Preference) {
      preference.onPreferenceChangeListener = sBindPreferenceSummaryToValueListener
      sBindPreferenceSummaryToValueListener.onPreferenceChange(
        preference,
        PreferenceManager
          .getDefaultSharedPreferences(preference.context)
          .getString(preference.key, ""))
    }
    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private val sBindPreferenceSummaryToValueListener = Preference.OnPreferenceChangeListener { preference, value ->
      val stringValue = value.toString()

      if (preference is ListPreference) {
        // For list preferences, look up the correct display value in
        // the preference's 'entries' list.
        val listPreference = preference
        val index = listPreference.findIndexOfValue(stringValue)

        // Set the summary to reflect the new value.
        preference.setSummary(if (index >= 0) listPreference.entries[index] else null)
      } else {
        // For all other preferences, set the summary to the value's
        // simple string representation.
        preference.summary = stringValue
      }
      true
    }

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private fun isXLargeTablet(context: Context): Boolean =
      context.resources.configuration.screenLayout and
        Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_XLARGE
  }
}

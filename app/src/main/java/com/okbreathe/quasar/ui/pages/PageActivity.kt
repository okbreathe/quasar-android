package com.okbreathe.quasar.ui.pages

import android.accounts.AccountManager
import android.content.*
import android.graphics.Color
import android.graphics.PorterDuff
import android.support.v7.app.AppCompatActivity
import org.jetbrains.anko.*
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.support.v7.app.AlertDialog
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import com.okbreathe.quasar.Application
import com.okbreathe.quasar.R
import com.okbreathe.quasar.data.ACCOUNT_TYPE
import com.okbreathe.quasar.data.AUTHORITY
import com.okbreathe.quasar.data.AUTH_TYPE
import com.okbreathe.quasar.data.models.Page
import com.okbreathe.quasar.ui.settings.SettingsActivity
import org.jetbrains.anko.sdk25.listeners.textChangedListener
import java.util.*
import kotlin.concurrent.*

class PageActivity : AppCompatActivity(), AbstractPageFragment.FragmentInteractionListener {
  val TAG = "QSR:PageActivity"
  private lateinit var accountManager: AccountManager
  private lateinit var toolbar: Toolbar
  private lateinit var viewModel: PageViewModel
  private lateinit var prefs: SharedPreferences
  private var showingDetail = false
  private var showingRevision = false
  private var searching = false
  private var isTrashed = false
  private var isFavorite = false
  private val delay: Long = 500
  private var timer = Timer()
  private var searchInput: EditText? = null
  private var menu: Menu? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_page)
    toolbar = findViewById(R.id.toolbar) as Toolbar
    setSupportActionBar(toolbar)
    accountManager = AccountManager.get(this)
    viewModel = PageViewModel(this)
    prefs = PreferenceManager.getDefaultSharedPreferences(this)

    val availableAccounts = accountManager.getAccountsByType(ACCOUNT_TYPE)

    when (availableAccounts.size) {
      0 -> if (!prefs.getBoolean(getString(R.string.preference_seen_welcome), false)) {
        accountManager.addAccount(ACCOUNT_TYPE, AUTH_TYPE, null, null, this, null, null)
      }
      else -> {
        accountManager.getAuthToken(availableAccounts[0], AUTH_TYPE, null, this, null, null)
        setupPeriodicSync()
      }
    }

    supportFragmentManager
      .beginTransaction()
      .replace(R.id.fragmentContainer, PageListFragment.newInstance())
      .commit()
  }

  override fun onResume() {
    super.onResume()
    setTheme()
  }

  override fun onCreateOptionsMenu(menu: Menu?): Boolean {
    this.menu = menu
    menuInflater.inflate(R.menu.menu_main, menu)
    setTheme()
    return true
  }

  override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
    menu?.clear()
    when (showingDetail) {
      true -> {
        menuInflater.inflate(R.menu.menu_page, menu)
        if (showingRevision) {
          menu?.findItem(R.id.action_trash)?.setVisible(false)
          menu?.findItem(R.id.action_untrash)?.setVisible(false)
          menu?.findItem(R.id.action_snapshot)?.setVisible(false)
          menu?.findItem(R.id.action_favorite)?.setVisible(false)
          menu?.findItem(R.id.action_unfavorite)?.setVisible(false)
        } else {
          menu?.findItem(R.id.action_delete)?.setVisible(isTrashed)
          menu?.findItem(R.id.action_trash)?.setVisible(!isTrashed)
          menu?.findItem(R.id.action_untrash)?.setVisible(isTrashed)
          menu?.findItem(R.id.action_snapshot)?.setVisible(!isTrashed)
          menu?.findItem(R.id.action_favorite)?.setVisible(!isFavorite && !isTrashed)
          menu?.findItem(R.id.action_unfavorite)?.setVisible(isFavorite)
        }
      }
      false -> {
        menuInflater.inflate(R.menu.menu_main, menu)
        menu?.findItem(R.id.action_page_sync)?.setVisible(
          !prefs.getString(getString(R.string.preference_sync_frequency), null).isNullOrBlank()
            && accountManager.accounts.isNotEmpty()
        )
        menu?.findItem(R.id.action_search)?.setIcon(if(searching) R.drawable.close else R.drawable.search)
      }
    }
    setTheme()
    return super.onPrepareOptionsMenu(menu)
  }

  override fun onOptionsItemSelected(item: MenuItem?): Boolean {
    when (item?.itemId) {
      R.id.action_clear -> reset()
      R.id.action_trash -> currentFragment().trashPage()
      R.id.action_delete -> currentFragment().deletePage()
      R.id.action_untrash -> currentFragment().unTrashPage()
      R.id.action_favorite -> currentFragment().favoritePage()
      R.id.action_unfavorite -> currentFragment().unFavoritePage()
      R.id.action_snapshot -> currentFragment().createSnapshot()
      R.id.action_search -> onToggleSearch()
      R.id.action_settings -> startActivity(Intent(this, SettingsActivity::class.java))
      R.id.action_page_sync -> sync()
      android.R.id.home -> currentFragment().openDrawer()
    }
    return super.onOptionsItemSelected(item)
  }

  override fun onNewPage(page: Page) {
    if (searching) closeSearch()
    onFragmentInteraction(PageDetailFragment.newInstance().apply {
      showingDetail = true
      invalidateOptionsMenu()
      arguments = Bundle().apply {
        putBoolean("new", true)
        putInt("id", page.id)
      }
    })
  }

  override fun onEditPage(page: Page) {
    onPageUpdated(page)
    onFragmentInteraction(PageDetailFragment.newInstance().apply {
      arguments = Bundle().apply { putInt("id", page.id) }
    })
  }

  override fun onPageUpdated(page: Page) {
    showingDetail = true
    isTrashed = page.isTrashed
    isFavorite = page.isFavorite
    invalidateOptionsMenu()
  }

  override fun onTrashPage() = onBackPressed()

  override fun onDeletePage() = onBackPressed()

  override fun onUnTrashPage() {
    isTrashed = false
    invalidateOptionsMenu()
  }

  override fun onToggleRevision(showing: Boolean) {
    showingRevision = showing
    invalidateOptionsMenu()
  }

  override fun onBackPressed() {
    if (currentFragment().isDrawerOpen) {
      currentFragment().closeDrawer()
    } else {
      if (supportFragmentManager.backStackEntryCount > 0 ) {
        showingDetail = false
        isTrashed = false
        invalidateOptionsMenu()
        supportFragmentManager.popBackStack()
        setTitle(getString(R.string.app_name))
      } else {
        super.onBackPressed()
      }
    }
  }

  fun setTitle(msg: String?) {
    toolbar.title = msg
  }

  fun showConfirmation(title: String, msg: String, fn: () -> Unit) {
    AlertDialog.Builder(this)
      .setTitle(title)
      .setMessage(msg)
      .setIcon(android.R.drawable.ic_dialog_alert)
      .setPositiveButton(android.R.string.yes, { dialog, which -> fn() })
      .setNegativeButton(android.R.string.no,null)
      .show()
  }

  fun showMessage(msg: String?) {
    if (msg.isNullOrBlank()) return
    runOnUiThread { Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show() }
  }

  fun showMessage(resourceId: Int) = showMessage(getString(resourceId))

  private fun setTheme(){
    val fg = viewModel.foreground
    val setBg: (item: MenuItem?) -> Unit = { it?.icon?.let { it.mutate(); it.setColorFilter(fg, PorterDuff.Mode.SRC_ATOP) } }
    menu?.let {
      val menu = it
      arrayOf(
        R.id.action_search,
        R.id.action_trash,
        R.id.action_untrash,
        R.id.action_favorite,
        R.id.action_unfavorite
      ).forEach { setBg(menu.findItem(it)) }
    }
    toolbar.navigationIcon?.mutate()?.setColorFilter(fg, PorterDuff.Mode.SRC_ATOP)
    toolbar.overflowIcon?.mutate()?.setColorFilter(fg, PorterDuff.Mode.SRC_ATOP)
    toolbar.setTitleTextColor(fg)
    toolbar.setSubtitleTextColor(fg)
    toolbar.backgroundColor = viewModel.ui.accentColor
    (findViewById(R.id.content) as View).backgroundColor = viewModel.ui.backgroundColor
  }

  private fun onToggleSearch() {
    if (searching) closeSearch() else openSearch()
    setTheme()
  }

  private fun closeSearch() {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    val searchIcon = menu?.findItem(R.id.action_search)
    supportActionBar?.apply {
      searchInput?.let { imm.hideSoftInputFromWindow(it.windowToken, 0) }
      setDisplayShowCustomEnabled(false)
      setDisplayShowTitleEnabled(true)
      toolbar.navigationIcon = getDrawable(R.drawable.logo)
      search("")
      searchIcon?.setIcon(R.drawable.search)
      searching = false
    }
  }

  private fun openSearch() {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    val searchIcon = menu?.findItem(R.id.action_search)
    supportActionBar?.apply {
      customView = renderSearch().view
      setDisplayShowCustomEnabled(true)
      setDisplayShowTitleEnabled(false)
      toolbar.navigationIcon = null
      searchIcon?.setIcon(R.drawable.close)
      searchInput?.let {
        it.requestFocus()
        imm.showSoftInput(searchInput, InputMethodManager.SHOW_IMPLICIT)
      }
      searching = true
    }
  }

  private fun renderSearch(): AnkoContext<Context> =
    UI {
      relativeLayout {
        lparams(width = matchParent) {
          gravity = Gravity.FILL_HORIZONTAL
        }
        searchInput = editText {
          padding = dip(10)
          hint = getString(R.string.hint_search)
          backgroundColor = Color.WHITE
          textChangedListener {
            afterTextChanged {
              editable ->
              timer.cancel()
              timer = Timer("search", true)
              timer.schedule(delay) {
                search(editable)
              }
            }
          }
        }.lparams(width = matchParent)
      }
    }

  private fun onFragmentInteraction(f: Fragment) =
    supportFragmentManager
      .beginTransaction()
      .replace(R.id.fragmentContainer, f)
      .addToBackStack(f::class.java.name)
      .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
      .commit()

  fun search(query: CharSequence?) = currentFragment().search(query)

  private fun reset() {
    (application as Application).localStore.deleteEverything()
    val editor = prefs.edit()
    editor.remove(getString(R.string.last_sync_push_at))
    editor.remove(getString(R.string.last_sync_pull_at))
    editor.apply()
  }

  private fun currentFragment(): AbstractPageFragment =
    supportFragmentManager.findFragmentById(R.id.fragmentContainer) as AbstractPageFragment

  private fun setupPeriodicSync(){
    val accounts = accountManager.getAccountsByType(ACCOUNT_TYPE)
    val interval = prefs.getString(getString(R.string.preference_sync_frequency), "-1").toLong()
    if (accounts.isNotEmpty() && interval > -1) {
      Log.d(TAG, "Setting periodic sync to $interval minutes")
      ContentResolver.addPeriodicSync( accounts[0], AUTHORITY, Bundle.EMPTY, interval * 60L )
    }
  }

  private fun sync() {
    val accounts = accountManager.getAccountsByType(ACCOUNT_TYPE)
    if (accounts.isNotEmpty()) {
      val bnd = Bundle()
      bnd.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true)
      bnd.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true)
      ContentResolver.requestSync(accounts[0], AUTHORITY, bnd)
      showMessage("Syncing")
    }
  }
}


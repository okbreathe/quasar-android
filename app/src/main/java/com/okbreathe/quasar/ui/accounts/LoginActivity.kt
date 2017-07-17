package com.okbreathe.quasar.ui.accounts

import android.accounts.Account
import android.accounts.AccountManager
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity

import android.os.Bundle
import android.preference.PreferenceManager
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*

import com.okbreathe.quasar.R
import com.okbreathe.quasar.data.ACCOUNT_NAME
import com.okbreathe.quasar.data.ACCOUNT_TYPE
import com.okbreathe.quasar.data.AUTH_TYPE
import com.okbreathe.quasar.data.RemoteStore
import com.okbreathe.quasar.data.api.AccountRequest
import com.okbreathe.quasar.data.api.AccountResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class LoginActivity : AppCompatActivity() {
  val TAG = "QSR:LoginActivity"
  private var authenticating = false
  lateinit private var urlView: EditText
  lateinit private var emailView: AutoCompleteTextView
  lateinit private var passwordView: EditText
  lateinit private var progressView: View
  lateinit private var loginFormView: View
  lateinit private var signInButton: Button
  lateinit private var skipButton: Button
  lateinit private var prefs: SharedPreferences

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_login)
    prefs = PreferenceManager.getDefaultSharedPreferences(this)
    // Set up the login form.
    loginFormView = findViewById(R.id.login_form)
    signInButton = (findViewById(R.id.email_sign_in_button) as Button).apply {
      setOnClickListener { attemptLogin() } }
    skipButton = (findViewById(R.id.skip_sign_in) as Button).apply {
      setOnClickListener { skipLogin() } }
    progressView = findViewById(R.id.login_progress)
    urlView = findViewById(R.id.url) as EditText
    emailView = findViewById(R.id.email) as AutoCompleteTextView
    passwordView = findViewById(R.id.password) as EditText
    passwordView.setOnEditorActionListener(TextView.OnEditorActionListener {
      textView, id, keyEvent ->
      if (id == R.id.login || id == EditorInfo.IME_NULL) {
        attemptLogin()
        return@OnEditorActionListener true
      }
      false
    })

    if (prefs.getBoolean(getString(R.string.preference_seen_welcome), false)) {
      skipButton.visibility = View.GONE
    }
  }

  private fun skipLogin() {
    prefs.edit().apply{
      putBoolean(getString(R.string.preference_seen_welcome), true)
      commit()
    }
    finish()
  }

  /**
   * Attempts to sign in or register the account specified by the login form.
   * If there are form errors (invalid email, missing fields, etc.), the
   * errors are presented and no actual login attempt is made.
   */
  private fun attemptLogin() {
    if (authenticating) return

    // Reset errors.
    emailView.error = null
    passwordView.error = null

    // Store values at the time of the login attempt.
    val email = emailView.text.toString()
    val password = passwordView.text.toString()
    val url = urlView.text.toString()

    var cancel = false
    var focusView: View = passwordView

    // Check for a valid password, if the user entered one.
    if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
      passwordView.error = getString(R.string.error_invalid_password)
      focusView = passwordView
      cancel = true
    }

    // Check for a valid email address.
    if (TextUtils.isEmpty(email)) {
      emailView.error = getString(R.string.error_field_required)
      focusView = emailView
      cancel = true
    } else if (!isEmailValid(email)) {
      emailView.error = getString(R.string.error_invalid_email)
      focusView = emailView
      cancel = true
    }

    // Check for a valid url
    if (TextUtils.isEmpty(url)) {
      urlView.error = getString(R.string.error_field_required)
      focusView = urlView
      cancel = true
    } else if (!isUrlValid(url)) {
      urlView.error = getString(R.string.error_invalid_url)
      focusView = urlView
      cancel = true
    }

    if (cancel) {
      // There was an error; don't attempt login and focus the first
      // form field with an error.
      focusView.requestFocus()
    } else {
      // Show a progress spinner, and kick off a background task to
      // perform the user login attempt.
      authenticating = true
      showProgress(authenticating)
      RemoteStore.create(url)
        .login(AccountRequest(email = email, password = password))
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
          { resp ->
            authenticating = false
            showProgress(authenticating)
            setAuthToken(resp)
            saveURL(url, email)
            finish()
          },
          { e ->
            Log.e(TAG, e.message)
            showMessage(e.message)
            authenticating = false
            showProgress(authenticating)
          }
        )
    }
  }

  private fun setAuthToken(resp: AccountResponse) {
    if (resp.error != null) {
      passwordView.error = getString(R.string.error_incorrect_password)
      passwordView.requestFocus()
    } else {
      try {
        val account = Account(ACCOUNT_NAME, ACCOUNT_TYPE)
        val accountManager = AccountManager.get(this)
        accountManager.addAccountExplicitly(account, null, null)
        accountManager.setAuthToken(account, AUTH_TYPE, resp.token)
      } catch (e: Exception) {
        Log.e(TAG, e.message)
      }
    }
  }

  private fun saveURL(url: String, email: String) {
    PreferenceManager.getDefaultSharedPreferences(this).edit().apply {
      putString(getString(R.string.preference_sync_url), url)
      putString(getString(R.string.preference_email), email)
      commit()
    }
  }

  private fun isUrlValid(url: String) = url.matches(Regex("^https?:\\/\\/.+"))

  private fun isEmailValid(email: String) = email.contains("@")

  private fun isPasswordValid(password: String) = password.length > 4

  private fun showMessage(msg: String?) {
    if (msg.isNullOrBlank()) return
    runOnUiThread { Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show() }
  }

  private fun showProgress(show: Boolean) {
    val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime)

    loginFormView.visibility = if (show) View.GONE else View.VISIBLE
    loginFormView.animate().setDuration(shortAnimTime.toLong()).alpha(
      (if (show) 0 else 1).toFloat()).setListener(object : AnimatorListenerAdapter() {
      override fun onAnimationEnd(animation: Animator) {
        loginFormView.visibility = if (show) View.GONE else View.VISIBLE
      }
    })

    progressView.visibility = if (show) View.VISIBLE else View.GONE
    progressView.animate().setDuration(shortAnimTime.toLong()).alpha(
      (if (show) 1 else 0).toFloat()).setListener(object : AnimatorListenerAdapter() {
      override fun onAnimationEnd(animation: Animator) {
        progressView.visibility = if (show) View.VISIBLE else View.GONE
      }
    })
  }
}

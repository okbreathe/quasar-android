package com.okbreathe.quasar.sync

import android.accounts.*
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils

import android.accounts.AccountManager.KEY_BOOLEAN_RESULT
import com.okbreathe.quasar.data.*
import com.okbreathe.quasar.ui.accounts.LoginActivity

class Authenticator(private val context: Context) : AbstractAccountAuthenticator(context) {
  val TAG = "QSR:Authenticator"

  override fun addAccount(response: AccountAuthenticatorResponse,
                          accountType: String,
                          authTokenType: String,
                          requiredFeatures: Array<String>?,
                          options: Bundle?): Bundle {
    val intent = Intent(context, LoginActivity::class.java)
    intent.putExtra(ACCOUNT_TYPE, accountType)
    intent.putExtra(AUTH_TYPE, authTokenType)
    intent.putExtra(IS_ADDING_ACCOUNT, true)
    intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)
    return Bundle().apply { putParcelable(AccountManager.KEY_INTENT, intent) }
  }

  override fun getAuthToken(response: AccountAuthenticatorResponse,
                            account: Account,
                            authTokenType: String,
                            options: Bundle): Bundle {
    val am = AccountManager.get(context)
    val authToken = am.peekAuthToken(account, authTokenType)

    // If we get an authToken return it
    if (!TextUtils.isEmpty(authToken)) {
      return Bundle().apply {
        putString(AccountManager.KEY_ACCOUNT_NAME, account.name)
        putString(AccountManager.KEY_ACCOUNT_TYPE, account.type)
        putString(AccountManager.KEY_AUTHTOKEN, authToken)
      }
    }

    // If we get here, then we the user has no token so we need
    // need to prompt them for their credentials.
    val intent = Intent(context, LoginActivity::class.java)
    intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)
    intent.putExtra(ACCOUNT_TYPE, account.type)
    intent.putExtra(AUTH_TYPE, authTokenType)
    intent.putExtra(ACCOUNT_NAME, account.name)
    val bundle = Bundle()
    bundle.putParcelable(AccountManager.KEY_INTENT, intent)
    return bundle
  }

  override fun hasFeatures(response: AccountAuthenticatorResponse, account: Account, features: Array<String>): Bundle =
    Bundle().apply { putBoolean(KEY_BOOLEAN_RESULT, false) }

  override fun getAuthTokenLabel(authTokenType: String) = "$authTokenType (Label)"

  override fun editProperties(response: AccountAuthenticatorResponse, accountType: String) = null

  override fun confirmCredentials(response: AccountAuthenticatorResponse, account: Account, options: Bundle) = null

  override fun updateCredentials(response: AccountAuthenticatorResponse, account: Account, authTokenType: String, options: Bundle) = null
}

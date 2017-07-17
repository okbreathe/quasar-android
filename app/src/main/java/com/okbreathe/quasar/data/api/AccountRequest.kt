package com.okbreathe.quasar.data.api

data class AccountRequest(val email: String, val password: String, val password_confirmation: String? = null)

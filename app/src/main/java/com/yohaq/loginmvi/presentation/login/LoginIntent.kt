package com.yohaq.loginmvi.presentation.login

sealed class LoginIntent {
  data class UpdateCredentials(
    val username: String,
    val password: String
  ) : LoginIntent()

  data class SubmitLoginIntent(
    val username: String,
    val password: String
  ): LoginIntent()
}
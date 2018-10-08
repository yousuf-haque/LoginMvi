package com.yohaq.loginmvi.presentation.login

import arrow.core.Option
import arrow.core.none
import arrow.core.some
import com.yohaq.loginmvi.presentation.login.LoginIntent.SubmitLoginIntent
import com.yohaq.loginmvi.presentation.login.LoginState.Entering
import com.yohaq.loginmvi.presentation.login.LoginState.Error
import com.yohaq.loginmvi.presentation.login.LoginState.LoginError.IncorrectCredentials
import com.yohaq.loginmvi.presentation.login.LoginState.LoginError.NetworkError
import com.yohaq.loginmvi.presentation.login.LoginState.Submitting
import java.util.Date

data class LoginViewState(
  val username: String,
  val password: String,
  val isUsernameFieldEnabled: Boolean,
  val isPasswordFieldEnabled: Boolean,
  val isProgressSpinnerVisible: Boolean,
  val isSubmitButtonEnabled: Boolean,
  val submitButtonCopy: String,
  val errorMessageOption: Option<String>,
  val submitButtonIntentOption: Option<LoginIntent>,
  val currentTimeStringOption: Option<String>
)

fun LoginState.render(): LoginViewState {
  return LoginViewState(
      username = getUserName(),
      isPasswordFieldEnabled = isPasswordFieldEnabled(),
      isUsernameFieldEnabled = isUsernameFieldEnabled(),
      password = getPassword(),
      submitButtonCopy = getSubmitButtonCopy(),
      isSubmitButtonEnabled = isSubmitButtonEnabled(),
      errorMessageOption = getErrorMessageOpton(),
      isProgressSpinnerVisible = isProgressSpinnerVisible(),
      submitButtonIntentOption = getSubmitButtonIntent(),
      currentTimeStringOption = getCurrentTimeStringOption()
  )
}

private fun LoginState.getCurrentTimeStringOption(): Option<String> {
  return when(this){
    is LoginState.Entering -> currentTime.map(Date::toLocaleString)
    is LoginState.Submitting -> currentTime.map(Date::toLocaleString)
    is LoginState.Error -> currentTime.map(Date::toLocaleString)
  }
}



fun LoginState.getUserName(): String {
  return when (this) {
    is Entering -> username
    is Submitting -> username
    is Error -> username
  }
}

fun LoginState.isPasswordFieldEnabled(): Boolean {
  return when (this) {
    is Entering -> true
    is Submitting -> false
    is Error -> true
  }
}

fun LoginState.isUsernameFieldEnabled(): Boolean {
  return when (this) {
    is Entering -> true
    is Submitting -> false
    is Error -> true
  }

}

fun LoginState.isProgressSpinnerVisible(): Boolean {
  return when (this) {
    is Entering -> false
    is Submitting -> true
    is Error -> false
  }
}

fun LoginState.getErrorMessageOpton(): Option<String> {
  return when (this) {
    is Entering -> none()
    is Submitting -> none()
    is Error -> when (error) {

      IncorrectCredentials -> "Incorrect credentials".some()
      NetworkError -> "Network error".some()
    }
  }
}

fun LoginState.isSubmitButtonEnabled(): Boolean {
  return when (this) {
    is Entering -> username.isNotBlank() && password.isNotBlank()
    is Submitting -> false
    is Error -> username.isNotBlank() && password.isNotBlank()
  }
}

fun LoginState.getPassword(): String {
  return when (this) {
    is Entering -> password
    is Submitting -> password
    is Error -> password
  }
}

fun LoginState.getSubmitButtonCopy(): String {
  return when (this) {
    is Entering -> "Submit"
    is Submitting -> "Submitting"
    is Error -> "Submit"
  }
}

fun LoginState.getSubmitButtonIntent(): Option<LoginIntent> {
  return when (this) {
    is Entering ->
      if (username.isNotBlank() && password.isNotBlank()) {
        SubmitLoginIntent(
            username = username,
            password = password
        )
            .some()
      } else {
        none()
      }
    is Submitting -> none()
    is Error ->
      if (username.isNotBlank() && password.isNotBlank()) {
        SubmitLoginIntent(
            username = username,
            password = password
        )
            .some()
      } else {
        none()
      }
  }
}
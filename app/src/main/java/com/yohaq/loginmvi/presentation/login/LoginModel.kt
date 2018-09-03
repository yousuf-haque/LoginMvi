package com.yohaq.loginmvi.presentation.login

import arrow.core.Try
import com.yohaq.loginmvi.api.HttpException
import com.yohaq.loginmvi.api.LoginRequest
import com.yohaq.loginmvi.api.UserInfo
import com.yohaq.loginmvi.common.just
import com.yohaq.loginmvi.presentation.login.LoginIntent.SubmitLoginIntent
import com.yohaq.loginmvi.presentation.login.LoginIntent.UpdateCredentials
import com.yohaq.loginmvi.presentation.login.LoginState.Entering
import com.yohaq.loginmvi.presentation.login.LoginState.Error
import com.yohaq.loginmvi.presentation.login.LoginState.LoginError.IncorrectCredentials
import com.yohaq.loginmvi.presentation.login.LoginState.LoginError.NetworkError
import com.yohaq.loginmvi.presentation.login.LoginState.Submitting
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

sealed class LoginState {

  data class Entering(
    val username: String,
    val password: String
  ) : LoginState()

  data class Submitting(
    val username: String,
    val password: String
  ) : LoginState()

  data class Error(
    val username: String,
    val password: String,
    val error: LoginError
  ) : LoginState()

  enum class LoginError {
    IncorrectCredentials,
    NetworkError
  }

}

fun buildLoginStateStream(
  intentStream: Observable<LoginIntent>,
  loginRequestBuilder: (LoginRequest) -> Single<Try<UserInfo>>,
  buildGoToLoggedInCompletable: (userId: String) -> Completable
): Observable<LoginState> {
  val initialState = Entering(
      username = "",
      password = ""
  )

  val reducerStream: Observable<LoginExampleStateReducer> = intentStream.flatMap {
    getIntentReducerStream(
        it,
        loginRequestBuilder,
        buildGoToLoggedInCompletable
    )
  }

  return reducerStream.scan(
      initialState
  ) { oldState: LoginState, reducer: LoginExampleStateReducer ->
    val newState = reducer(oldState)
    newState
  }
}

fun getIntentReducerStream(
  intent: LoginIntent,
  loginRequestBuilder: (LoginRequest) -> Single<Try<UserInfo>>,
  buildGoToLoggedInCompletable: (userId: String) -> Completable
): Observable<LoginExampleStateReducer> {

  return when (intent) {
    is UpdateCredentials -> getUpdateCredentialsReducerStream(
        intent
    )
    is SubmitLoginIntent -> buildSubmitLoginRequestReducerStream(
        intent,
        loginRequestBuilder,
        buildGoToLoggedInCompletable
    )
  }

}

fun buildSubmitLoginRequestReducerStream(
  intent: SubmitLoginIntent,
  loginRequestBuilder: (LoginRequest) -> Single<Try<UserInfo>>,
  buildGoToLoggedInCompletable: (userId: String) -> Completable
): Observable<LoginExampleStateReducer> {
  fun getOnErrorStateReducer(throwable: Throwable): LoginExampleStateReducer {
    val onErrorStateReducer: LoginExampleStateReducer = { oldState ->
      when (oldState) {
        is Entering -> Error(
            username = intent.username,
            password = intent.password,
            error = if (throwable is HttpException && throwable.code == 400) IncorrectCredentials else NetworkError
        )
        is Submitting -> Error(
            username = intent.username,
            password = intent.password,
            error = if (throwable is HttpException && throwable.code == 400) IncorrectCredentials else NetworkError
        )
        is Error -> Error(
            username = intent.username,
            password = intent.password,
            error = if (throwable is HttpException && throwable.code == 400) IncorrectCredentials else NetworkError
        )
      }
    }
    return onErrorStateReducer
  }

  val onSubmitStateReducer: LoginExampleStateReducer = { oldState ->
    when (oldState) {
      is Entering ->
        Submitting(
            username = intent.username,
            password = intent.password
        )
      is Submitting -> oldState
      is Error -> Submitting(
          username = intent.username,
          password = intent.password
      )
    }
  }

  return loginRequestBuilder(
      LoginRequest(
          username = intent.username,
          password = intent.password
      )
  )
      .flatMapObservable {
        it.fold(
            ifFailure = { getOnErrorStateReducer(it).just() },
            ifSuccess = {
              buildGoToLoggedInCompletable(
                  it.userId
              ).toObservable<LoginExampleStateReducer>()
            }
        )
      }
      .startWith(onSubmitStateReducer)

}

fun getUpdateCredentialsReducerStream(updateCredentialsIntent: UpdateCredentials): Observable<LoginExampleStateReducer> {

  val reducer: LoginExampleStateReducer = { oldState ->
    when (oldState) {

      is Entering ->
        oldState.copy(
            username = updateCredentialsIntent.username,
            password = updateCredentialsIntent.password
        )
      is Submitting -> oldState
      is Error ->
        Entering(
            username = updateCredentialsIntent.username,
            password = updateCredentialsIntent.password
        )
    }
  }

  return reducer.just()
}
typealias LoginExampleStateReducer = (LoginState) -> LoginState

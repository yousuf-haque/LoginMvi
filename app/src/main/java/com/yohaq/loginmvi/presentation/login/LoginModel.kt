package com.yohaq.loginmvi.presentation.login

import arrow.core.Option
import arrow.core.Try
import arrow.core.none
import arrow.core.some
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
import java.util.Date

sealed class LoginState {

  data class Entering(
    val username: String,
    val password: String,
    val currentTime: Option<Date>
  ) : LoginState()

  data class Submitting(
    val username: String,
    val password: String,
    val currentTime: Option<Date>
  ) : LoginState()

  data class Error(
    val username: String,
    val password: String,
    val error: LoginError,
    val currentTime: Option<Date>
  ) : LoginState()

  enum class LoginError {
    IncorrectCredentials,
    NetworkError
  }

}

fun buildLoginStateStream(
  intentStream: Observable<LoginIntent>,
  loginRequestBuilder: (LoginRequest) -> Single<Try<UserInfo>>,
  currentTimeStream: Observable<Date>,
  buildGoToLoggedInCompletable: (userId: String) -> Completable
): Observable<LoginState> {
  val initialState = Entering(
      username = "",
      password = "",
      currentTime = none()
  )

  val updateTimeReducerStream = getCurrentTimeReducerStream(currentTimeStream)

  val intentReducerStream = intentStream.flatMap {
    getIntentReducerStream(
        it,
        loginRequestBuilder,
        buildGoToLoggedInCompletable
    )
  }

  val reducerStream: Observable<LoginExampleStateReducer> = Observable.merge(intentReducerStream, updateTimeReducerStream)

  return reducerStream.scan(
      initialState
  ) { oldState: LoginState, reducer: LoginExampleStateReducer ->
    val newState = reducer(oldState)
    newState
  }
}

fun getCurrentTimeReducerStream(currentTimeStream: Observable<Date>): Observable<LoginExampleStateReducer> {
  return currentTimeStream.map { date ->
    { oldState: LoginState ->
      when(oldState){
        is LoginState.Entering -> oldState.copy(currentTime = date.some())
        is LoginState.Submitting -> oldState.copy(currentTime = date.some())
        is LoginState.Error -> oldState.copy(currentTime = date.some())
      }
    }
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
            error = if (throwable is HttpException && throwable.code == 400) IncorrectCredentials else NetworkError,
            currentTime = oldState.currentTime
        )
        is Submitting -> Error(
            username = intent.username,
            password = intent.password,
            error = if (throwable is HttpException && throwable.code == 400) IncorrectCredentials else NetworkError,
            currentTime = oldState.currentTime
        )
        is Error -> Error(
            username = intent.username,
            password = intent.password,
            error = if (throwable is HttpException && throwable.code == 400) IncorrectCredentials else NetworkError,
            currentTime = oldState.currentTime
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
            password = intent.password,
            currentTime = oldState.currentTime
        )
      is Submitting -> oldState
      is Error -> Submitting(
          username = intent.username,
          password = intent.password,
          currentTime = oldState.currentTime
      )
    }
  }

  return loginRequestBuilder(
      LoginRequest(
          username = intent.username,
          password = intent.password
      )
  )
      .flatMapObservable { loginResult ->
        loginResult.fold(
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
            password = updateCredentialsIntent.password,
            currentTime = oldState.currentTime
        )
    }
  }

  return reducer.just()
}
typealias LoginExampleStateReducer = (LoginState) -> LoginState

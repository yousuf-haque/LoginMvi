package com.yohaq.loginmvi.api

import io.reactivex.Single

data class UserInfo(
  val userId: String
)

data class LoginRequest(
  val username: String,
  val password: String
)

class HttpException(val code: Int) :  RuntimeException() // I made my own exception to emulate a retrofit error

interface LoginService {
  fun authenticate(loginRequest: LoginRequest): Single<UserInfo>
}
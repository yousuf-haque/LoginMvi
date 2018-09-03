package com.yohaq.loginmvi.data

import arrow.core.Try
import com.yohaq.loginmvi.api.LoginRequest
import com.yohaq.loginmvi.api.LoginService
import com.yohaq.loginmvi.api.UserInfo
import com.yohaq.loginmvi.common.di.scopes.ApplicationScope
import com.yohaq.loginmvi.common.wrapInTry
import io.reactivex.Single
import javax.inject.Inject

@ApplicationScope
class AuthenticationRepository @Inject constructor(private val loginService: LoginService) {
  fun buildLoginRequest(loginRequest: LoginRequest): Single<Try<UserInfo>> {
    return loginService.authenticate(loginRequest).wrapInTry()
  }
}
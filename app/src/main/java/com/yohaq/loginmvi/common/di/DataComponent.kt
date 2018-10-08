package com.yohaq.loginmvi.common.di

import com.yohaq.loginmvi.api.HttpException
import com.yohaq.loginmvi.api.LoginRequest
import com.yohaq.loginmvi.api.LoginService
import com.yohaq.loginmvi.api.UserInfo
import com.yohaq.loginmvi.common.di.scopes.ApplicationScope
import com.yohaq.loginmvi.common.toSingle
import com.yohaq.loginmvi.data.AuthenticationRepository
import com.yohaq.loginmvi.data.DateTimeRepository
import dagger.Component
import dagger.Module
import dagger.Provides
import io.reactivex.Single
import java.util.concurrent.TimeUnit

@Component(
    dependencies = [ApplicationComponent::class],
    modules = [ServiceModule::class]
)
@ApplicationScope
interface DataComponent {
  val authenticationRepository: AuthenticationRepository
  val dateTimeRepository: DateTimeRepository
}

@Module
class ServiceModule {

  private val fakeLoginService: LoginService = object : LoginService {
    override fun authenticate(loginRequest: LoginRequest): Single<UserInfo> {
      return if (loginRequest.username == "foo" && loginRequest.password == "bar") {
        UserInfo("SOME_USER_ID").toSingle()
            .delay(4, TimeUnit.SECONDS)
      } else {
        Unit.toSingle()
            .delay(4, TimeUnit.SECONDS)
            .flatMap { Single.error<UserInfo>(HttpException(400)) }
      }
    }
  }


  @Provides
  fun provideLoginService(): LoginService = fakeLoginService

}
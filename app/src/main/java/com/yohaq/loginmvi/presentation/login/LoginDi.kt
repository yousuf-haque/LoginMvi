package com.yohaq.loginmvi.presentation.login

import com.bluelinelabs.conductor.Router
import com.yohaq.loginmvi.common.di.DataComponent
import com.yohaq.loginmvi.common.di.scopes.ControllerScope
import com.yohaq.loginmvi.data.AuthenticationRepository
import com.yohaq.loginmvi.data.DateTimeRepository
import dagger.Component
import dagger.Module
import dagger.Provides
import io.reactivex.Observable

@Component(
    dependencies = [DataComponent::class],
    modules = [LoginModule::class]
)
@ControllerScope
interface LoginComponent {
  val viewStateStream: Observable<LoginViewState>
}

@Module
class LoginModule(
  private val intentStream: Observable<LoginIntent>,
  private val router: Router
) {

  @Provides
  fun provideStateStream(
    authenticationRepository: AuthenticationRepository,
    dateTimeRepository: DateTimeRepository
  ): Observable<LoginState> {
    return buildLoginStateStream(
        intentStream,
        authenticationRepository::buildLoginRequest,
        dateTimeRepository.currentTimeStream,
        router::buildGoToLoggedInScreenCompletable
    )
  }

  @Provides
  fun provideViewStateStream(stateStream: Observable<LoginState>): Observable<LoginViewState> {
    return stateStream.map(LoginState::render)
  }

}


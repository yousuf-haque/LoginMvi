package com.yohaq.loginmvi.presentation.login

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bluelinelabs.conductor.Controller
import com.jakewharton.rxbinding2.widget.textChanges
import com.jakewharton.rxrelay2.PublishRelay
import com.yohaq.loginmvi.R.layout
import com.yohaq.loginmvi.common.forSome
import com.yohaq.loginmvi.presentation.common.dataComponent
import com.yohaq.loginmvi.presentation.common.isVisible
import com.yohaq.loginmvi.presentation.common.setTextOrHide
import com.yohaq.loginmvi.presentation.login.LoginIntent.UpdateCredentials
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.android.synthetic.main.controller_login.view.current_time_tv
import kotlinx.android.synthetic.main.controller_login.view.error_message_tv
import kotlinx.android.synthetic.main.controller_login.view.password_et
import kotlinx.android.synthetic.main.controller_login.view.progress_pb
import kotlinx.android.synthetic.main.controller_login.view.submit_btn
import kotlinx.android.synthetic.main.controller_login.view.username_et

class LoginController : Controller() {

  private val component: LoginComponent by lazy {
    val act = activity ?: throw IllegalStateException(
        "Cannot access component on controller before controller has been tied to activity"
    )
    DaggerLoginComponent.builder()
        .loginModule(LoginModule(intentRelay, router))
        .dataComponent(act.dataComponent)
        .build()
  }

  // Controllers survive configuration changes. We turn this into a connected observable
  // so we can maintain the state of the stream during configuration changes
  private val viewStateStream: Observable<LoginViewState> by lazy {
    component.viewStateStream.replay(1)
        .autoConnect()
  }

  private val intentRelay: PublishRelay<LoginIntent> by lazy {
    PublishRelay.create<LoginIntent>()
  }

  private val compositeDisposable = CompositeDisposable()

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup
  ): View {
    return inflater.inflate(layout.controller_login, container, false)
  }

  override fun onAttach(view: View) {
    super.onAttach(view)

    compositeDisposable += view.getUpdateCredentialsIntentStream()
        .subscribeBy(
            onNext = { intentRelay.accept(it) },
            onError = { Log.e(TAG, "Error in login example username/password text changes", it) }
        )
    compositeDisposable += viewStateStream
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeBy(
            onNext = { view.update(it) },
            onError = {
              Log.e(
                  TAG, "Error in login example view state stream", it
              )
            }
        )
  }

  private fun View.getUpdateCredentialsIntentStream(): Observable<UpdateCredentials> {
    return Observable.combineLatest(
        username_et.textChanges().map(CharSequence::toString).distinctUntilChanged(),
        password_et.textChanges().map(CharSequence::toString).distinctUntilChanged(),
        BiFunction(::UpdateCredentials) // The constructor of UpdateCredentials will act as the joining function
    )
        .subscribeOn(AndroidSchedulers.mainThread())
  }

  private fun View.update(viewState: LoginViewState) {

    // This can be further optimized to only update the properties that have been changed.
    // This would be done by splitting the view state stream into the individual properties, and
    // appending a .distinctUntilChanged on it.
    progress_pb.isVisible = viewState.isProgressSpinnerVisible

    submit_btn.isEnabled = viewState.isSubmitButtonEnabled
    submit_btn.text = viewState.submitButtonCopy

    username_et.isEnabled = viewState.isUsernameFieldEnabled
    password_et.isEnabled = viewState.isPasswordFieldEnabled

    error_message_tv.setTextOrHide(viewState.errorMessageOption)

    current_time_tv.setTextOrHide(viewState.currentTimeStringOption)

    submit_btn.setOnClickListener {
      viewState.submitButtonIntentOption.forSome { intentRelay.accept(it) }
    }
  }

  override fun onDetach(view: View) {
    super.onDetach(view)
    compositeDisposable.clear() // Unsubscribe from view state stream so no updates while detached
  }

  override fun onDestroy() {
    super.onDestroy()
    compositeDisposable.dispose() // Dispose the stream to terminate view state stream's connected obs
  }

  companion object {
    private val TAG = LoginController::class.java.simpleName!!
  }
}

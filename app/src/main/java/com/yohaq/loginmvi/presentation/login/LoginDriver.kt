package com.yohaq.loginmvi.presentation.login

import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import com.yohaq.loginmvi.presentation.loggedIn.LoggedInController
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.toCompletable

fun Router.buildGoToLoggedInScreenCompletable(userId: String): Completable {

  return { replaceTopController(RouterTransaction.with(LoggedInController(userId))) }
      .toCompletable()
      .subscribeOn(AndroidSchedulers.mainThread())
}
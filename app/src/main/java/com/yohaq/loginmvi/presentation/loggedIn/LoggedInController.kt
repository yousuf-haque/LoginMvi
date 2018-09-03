package com.yohaq.loginmvi.presentation.loggedIn

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.RouterTransaction
import com.yohaq.loginmvi.R
import com.yohaq.loginmvi.presentation.login.LoginController
import kotlinx.android.synthetic.main.controller_logged_in.view.user_id_tv

class LoggedInController(bundle: Bundle) : Controller(bundle) {

  constructor(userId: String): this(Bundle().apply {
    putString(KEY_USER_ID, userId)
  })

  private val userId: String = bundle.getString(KEY_USER_ID)

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
    return inflater.inflate(R.layout.controller_logged_in, container, false)
  }

  override fun handleBack(): Boolean {
    router.replaceTopController(RouterTransaction.with(LoginController()))
    return true
  }


  override fun onAttach(view: View) {
    super.onAttach(view)
    view.user_id_tv.text = userId
  }

  companion object {
    private val TAG = LoggedInController::class.java.simpleName!!
    private const val KEY_USER_ID = "KEY_USER_ID"
  }
}
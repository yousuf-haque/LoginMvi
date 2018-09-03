package com.yohaq.loginmvi

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.bluelinelabs.conductor.Conductor
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import com.yohaq.loginmvi.presentation.login.LoginController
import kotlinx.android.synthetic.main.activity_main.root_chfl

class MainActivity : AppCompatActivity() {

  private lateinit var router: Router
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    router = Conductor.attachRouter(this, root_chfl, savedInstanceState)

    if (!router.hasRootController()) {
      router.pushController(RouterTransaction.with(LoginController()))
    }
  }

  override fun onBackPressed() {
    if (!router.handleBack()) {
      super.onBackPressed()
    }
  }
}



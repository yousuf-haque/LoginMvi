package com.yohaq.loginmvi

import android.app.Application
import com.yohaq.loginmvi.common.di.AppModule
import com.yohaq.loginmvi.common.di.ApplicationComponent
import com.yohaq.loginmvi.common.di.DaggerApplicationComponent
import com.yohaq.loginmvi.common.di.DaggerDataComponent
import com.yohaq.loginmvi.common.di.DataComponent

class LoginExampleApplication : Application() {

  private val appComponent: ApplicationComponent by lazy {
    DaggerApplicationComponent.builder()
        .appModule(AppModule(applicationContext))
        .build()
  }

  val dataComponent: DataComponent by lazy {
    DaggerDataComponent.builder()
        .applicationComponent(appComponent)
        .build()
  }

}
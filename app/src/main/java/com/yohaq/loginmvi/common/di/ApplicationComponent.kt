package com.yohaq.loginmvi.common.di

import android.content.Context
import dagger.Component
import dagger.Module
import dagger.Provides

@Component(modules = [AppModule::class])

interface ApplicationComponent {
  val applicationContext: Context
}

@Module
class AppModule(private val context: Context){

  @Provides
  fun provideContext() = context
}
package com.yohaq.loginmvi.presentation.common

import android.app.Activity
import com.yohaq.loginmvi.LoginExampleApplication
import com.yohaq.loginmvi.common.di.DataComponent

val Activity.dataComponent: DataComponent get() = let { application as LoginExampleApplication }.dataComponent
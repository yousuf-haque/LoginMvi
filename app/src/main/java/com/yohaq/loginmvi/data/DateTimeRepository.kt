package com.yohaq.loginmvi.data

import com.yohaq.loginmvi.common.di.scopes.ApplicationScope
import io.reactivex.Observable
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit.SECONDS
import javax.inject.Inject

@ApplicationScope
class DateTimeRepository @Inject constructor() {

  val currentTimeStream: Observable<Date> by lazy {
    Observable.interval(1, SECONDS)
        .map {
          Calendar.getInstance()
              .time
        }
  }
}
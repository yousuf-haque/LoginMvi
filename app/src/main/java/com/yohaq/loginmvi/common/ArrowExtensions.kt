package com.yohaq.loginmvi.common

import arrow.core.Option
import arrow.core.Some

inline fun <reified T> Option<T>.forSome(doFunc: (T) -> Unit): Option<T> =
  also {
    when (this) {
      is Some -> doFunc(t)
    }
  }

inline fun <reified T, reified U> Option<T>.forSome(otherOption: Option<U>, doFunc: (T, U) -> Unit) {
  forSome { t -> otherOption.forSome { u -> doFunc(t, u) } }

}
package com.yohaq.loginmvi.presentation.common

import android.view.View
import android.widget.TextView
import arrow.core.None
import arrow.core.Option
import arrow.core.Some

fun TextView.setTextOrHide(text: Option<String>) {
  when (text) {
    None -> {
      visibility = View.GONE
    }
    is Some -> {
      this.text = text.t
      visibility = View.VISIBLE
    }
  }
}
var View.isVisible: Boolean
  get() = visibility == View.VISIBLE
  set(value) {
    visibility = if (value) View.VISIBLE else View.GONE
  }
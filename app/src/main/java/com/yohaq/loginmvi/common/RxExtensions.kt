package com.yohaq.loginmvi.common

import arrow.core.Try
import io.reactivex.Observable
import io.reactivex.Single

fun <T> T.just(): Observable<T> = Observable.just(this)!!

fun <T> Observable<T>.wrapInTry(): Observable<Try<T>> = map { Try.just(it) }.onErrorReturn { Try.raise(it) }
fun <T> Single<T>.wrapInTry(): Single<Try<T>> = map { Try.just(it) }.onErrorReturn { Try.raise(it) }

fun <T> T.toSingle(): Single<T> = Single.just(this)
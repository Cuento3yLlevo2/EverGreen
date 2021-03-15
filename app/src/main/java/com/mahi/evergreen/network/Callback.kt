package com.mahi.evergreen.network

import java.lang.Exception

interface Callback<T> {
    fun onSuccess(result: T?)
    fun onFailure(exception: Exception)
}
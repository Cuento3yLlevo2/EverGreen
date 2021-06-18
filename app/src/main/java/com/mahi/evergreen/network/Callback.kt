package com.mahi.evergreen.network

import java.lang.Exception

/**
 * Retrieve DatabaseService results from Firebase to do some post-action on a activity or fragment
 */
interface Callback<T> {
    fun onSuccess(result: T?)
    fun onFailure(exception: Exception)
}
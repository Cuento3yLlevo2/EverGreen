package com.mahi.evergreen.view.adapter

import com.mahi.evergreen.model.User

interface UsersListener {
    fun onUserClicked(user: User, position: Int)
    fun observeViewModel()
}
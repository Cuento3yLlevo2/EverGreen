package com.mahi.evergreen.view.adapter

import com.mahi.evergreen.model.Post

interface PostListener {
    fun onPostItemClicked(postItem: Post, position: Int)
    fun onPostFavCheckClicked(postItem: Post, position: Int)
    fun onPostFavUncheckClicked(postItem: Post, position: Int)
    fun observeViewModel()
}
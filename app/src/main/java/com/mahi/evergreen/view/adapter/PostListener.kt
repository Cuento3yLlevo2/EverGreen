package com.mahi.evergreen.view.adapter

import com.mahi.evergreen.model.Post

/**
 * Provides a listener to control what happens when:
 * - a Post populated in the RecyclerView is clicked
 * - a Post populated in the RecyclerView is being add or remove as favorite by the user.
 * - The RecyclerView's post list has been updated
 */
interface PostListener {
    fun onPostItemClicked(postItem: Post, position: Int)
    fun onPostFavCheckClicked(postItem: Post, position: Int)
    fun onPostFavUncheckClicked(postItem: Post, position: Int)
    fun observeViewModel()
}
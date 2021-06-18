package com.mahi.evergreen.view.adapter

import com.mahi.evergreen.model.ChatMessage

/**
 * Provides a listener to control what happens when:
 * - a Chat Message populated in the RecyclerView is clicked
 * - The RecyclerView's Chat Message list has been updated
 */
interface ChatMessagesListener {
    fun onMessageItemClicked(chatMessageItem: ChatMessage, position: Int)
    fun observeViewModel()
}
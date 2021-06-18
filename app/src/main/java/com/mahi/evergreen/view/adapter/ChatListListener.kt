package com.mahi.evergreen.view.adapter

import com.mahi.evergreen.model.Chat

/**
 * Provides a listener to control what happens when:
 * - a Chat populated in the RecyclerView is clicked
 * - The RecyclerView's Chat list has been updated
 */
interface ChatListListener {
    fun onChatListItemClicked(chatListItem: Chat, position: Int)
    fun observeViewModel()
}
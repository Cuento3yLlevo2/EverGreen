package com.mahi.evergreen.view.adapter

import com.mahi.evergreen.model.ChatListItem

interface ChatListListener {
    fun onChatListItemClicked(chatListItem: ChatListItem, position: Int)
    fun observeViewModel()
}
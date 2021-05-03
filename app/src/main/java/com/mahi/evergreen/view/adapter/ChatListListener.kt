package com.mahi.evergreen.view.adapter

import com.mahi.evergreen.model.Chat

interface ChatListListener {
    fun onChatListItemClicked(chatListItem: Chat, position: Int)
    fun observeViewModel()
}
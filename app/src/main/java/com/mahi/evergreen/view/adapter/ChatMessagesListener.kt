package com.mahi.evergreen.view.adapter

import com.mahi.evergreen.model.ChatMessageItem

interface ChatMessagesListener {
    fun onChatClicked(chatMessageItem: ChatMessageItem, position: Int)
    fun observeViewModel()
}
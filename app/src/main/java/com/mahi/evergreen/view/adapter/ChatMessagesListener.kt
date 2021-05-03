package com.mahi.evergreen.view.adapter

import com.mahi.evergreen.model.ChatMessage

interface ChatMessagesListener {
    fun onChatClicked(chatMessageItem: ChatMessage, position: Int)
    fun observeViewModel()
}
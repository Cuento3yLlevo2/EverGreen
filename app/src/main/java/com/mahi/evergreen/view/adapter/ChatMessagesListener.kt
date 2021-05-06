package com.mahi.evergreen.view.adapter

import com.mahi.evergreen.model.ChatMessage

interface ChatMessagesListener {
    fun onMessageItemClicked(chatMessageItem: ChatMessage, position: Int)
    fun observeViewModel()
}
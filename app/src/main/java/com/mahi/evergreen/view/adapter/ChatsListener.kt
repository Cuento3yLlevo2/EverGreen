package com.mahi.evergreen.view.adapter

import com.mahi.evergreen.model.Chat

interface ChatsListener {
    fun onChatClicked(chat: Chat, position: Int)
    fun observeViewModel()
}
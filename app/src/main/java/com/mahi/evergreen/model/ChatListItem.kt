package com.mahi.evergreen.model

class ChatListItem(val lastChatMessageItem: ChatMessageItem? = null,
                   val postImageURL: String? = null, val postTitle: String? = null,
                   val chatUsername: String? = null, val chatListID: String? = null) {
    // Null default values create a no-argument default constructor, which is needed
    // for deserialization from a DataSnapshot.
}


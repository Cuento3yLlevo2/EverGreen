package com.mahi.evergreen.model

import com.google.firebase.database.IgnoreExtraProperties

/**
    This Object represents a list of ChatMessages when adding it to the Firebase database
    storage all ChatMessages related with a chat
 */
@IgnoreExtraProperties
class ChatMessages(val chatID: String? = "",
                   val messages: MutableMap<String, ChatMessage> = HashMap()
) {
    // Null default values create a no-argument default constructor, which is needed
    // for deserialization from a DataSnapshot.
}
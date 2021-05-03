package com.mahi.evergreen.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
// This Object represents ChatMessages when adding it to the database
class ChatMessages(val chatID: String? = "",
                   val messages: MutableMap<String, ChatMessage> = HashMap()
) {
    // Null default values create a no-argument default constructor, which is needed
    // for deserialization from a DataSnapshot.
}
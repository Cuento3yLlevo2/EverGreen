package com.mahi.evergreen.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
// This Object represents a ChatMessage when adding it to the database
class ChatMessage(val messageID: String? = "",
                  val sender: String? = "",
                  val message: String? = "",
                  val url: String? = "",
                  val isSeen: Boolean? = false,
                  val timestamp: Long? = 0
) {
    // Null default values create a no-argument default constructor, which is needed
    // for deserialization from a DataSnapshot.
}
package com.mahi.evergreen.model

import com.google.firebase.database.IgnoreExtraProperties

/**
    This Object represents a ChatMessage when adding it to the Firebase database
    storage all data for a single message on a Chat activity
 */
@IgnoreExtraProperties
class ChatMessage(val messageID: String? = "",
                  val sender: String? = "",
                  val message: String? = "",
                  val url: String? = "",
                  val seen: Boolean? = null,
                  val timestamp: Long? = 0
) {
    // Null default values create a no-argument default constructor, which is needed
    // for deserialization from a DataSnapshot.
}
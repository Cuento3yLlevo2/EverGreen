package com.mahi.evergreen.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
// This Object represents a Chat when adding it to the database
class Chat(val postID: String? = "",
           val postTitle: String? = "",
           val postImageURL: String? = "",
           val lastMessage: String? = "",
           val isSeen: Boolean? = false,
           val timestamp: Long? = null,
           val chatID: String? = ""
) {
    // Null default values create a no-argument default constructor, which is needed
    // for deserialization from a DataSnapshot.
}
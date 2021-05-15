package com.mahi.evergreen.model

import com.google.firebase.database.Exclude
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
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
                "postID" to postID,
                "postTitle" to postTitle,
                "postImageURL" to postImageURL,
                "lastMessage" to lastMessage,
                "isSeen" to isSeen,
                "timestamp" to timestamp,
                "chatID" to chatID
        )
    }

    @Exclude
    fun getChatFromMap(map: Map<*, *>): Chat {
        return Chat(
                map["postID"] as String?,
                map["postTitle"] as String?,
                map["postImageURL"] as String?,
                map["lastMessage"] as String?,
                map["isSeen"] as Boolean?,
                map["timestamp"] as Long?,
                map["chatID"] as String?
        )
    }
}
package com.mahi.evergreen.model

import com.google.firebase.database.IgnoreExtraProperties

/**
    This Object represents a Chat Members when adding it to the Firebase database
    storage a list or members with user id
 */
@IgnoreExtraProperties
class ChatMembers(val chatID: String? = "",
                  val postID: String? = "",
                  val members: MutableMap<String, Boolean> = HashMap()
) {
    // Null default values create a no-argument default constructor, which is needed
    // for deserialization from a DataSnapshot.
}
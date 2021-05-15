package com.mahi.evergreen.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
// This Object represents ChatMembers when adding it to the database
class ChatMembers(val chatID: String? = "",
                  val postID: String? = "",
                  val members: MutableMap<String, Boolean> = HashMap()
) {
    // Null default values create a no-argument default constructor, which is needed
    // for deserialization from a DataSnapshot.
}
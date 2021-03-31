package com.mahi.evergreen.model

import com.google.firebase.database.Exclude

class Chat(val sender: String? = null, val message: String? = null,
            val receiver: String? = null, val isseen: Boolean? = false,
            val url: String? = null, val messageID: String? = null) {
    // Null default values create a no-argument default constructor, which is needed
    // for deserialization from a DataSnapshot.
}
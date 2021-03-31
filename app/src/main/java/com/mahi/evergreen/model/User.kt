package com.mahi.evergreen.model

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
// This Object represents a User when adding it to the database
data class User(val username: String? = null, val email: String? = null,
                val profileImage: String? = null, val coverImage: String? = null,
                val status: String? = null, val search: String? = null, val userID: String? = null) {
    // Null default values create a no-argument default constructor, which is needed
    // for deserialization from a DataSnapshot.
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
                "username" to username,
                "email" to email,
                "profileImage" to profileImage,
                "coverImage" to coverImage,
                "status" to status,
                "search" to search
        )
    }
}
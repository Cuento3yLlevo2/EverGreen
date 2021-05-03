package com.mahi.evergreen.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
// This Object represents a User when adding it to the database
data class User(
        var uid: String? = "",
        var profile: UserProfile? = UserProfile(),
        var search: String? = "",
        var online: Boolean? = false,
        var createdAt: Long? = null,
        var createdPosts: MutableMap<String, Boolean> = HashMap(),
        var favoritePosts: MutableMap<String, Boolean> = HashMap()
) {
    // Null default values create a no-argument default constructor, which is needed
    // for deserialization from a DataSnapshot.
}
package com.mahi.evergreen.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
// This Object represents a Post when adding it to the database
class Post(val coverImage: String? = "",
           val type: Int? = 0,
           val minPrice: Double = 0.toDouble(),
           val title: String? = "",
           val postId: String? = "",
           val publisher: String? = "",
           val category: Int? = 0,
           val createdAt: Int? = 0,
           val updatedAt: Int? = 0,
           val description: String? = "",
           val images: MutableMap<String, Boolean> = HashMap(),
           val membersFollowingAsFavorite: MutableMap<String, Boolean> = HashMap(),
) {
    // Null default values create a no-argument default constructor, which is needed
    // for deserialization from a DataSnapshot.
}
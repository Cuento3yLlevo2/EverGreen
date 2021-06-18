package com.mahi.evergreen.model

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

/**
    This Object represents a Post when adding it to the Firebase database
    storage all data related with a post including Upcycling Ideas or Upcycling services
 */
@IgnoreExtraProperties
class Post (val coverImage: String? = "",
            var type: Int? = 0,
            val minPrice: Double? = 0.toDouble(),
            val title: String? = "",
            val postId: String? = "",
            val publisher: String? = "",
            val category: MutableMap<String, Boolean> = HashMap(),
            val createdAt: Long? = null,
            val updatedAt: Long? = null,
            val description: String? = "",
            val images: MutableMap<String, String> = HashMap(),
            val membersFollowingAsFavorite: MutableMap<String, Boolean> = HashMap()
) {
    // Null default values create a no-argument default constructor, which is needed
    // for deserialization from a DataSnapshot.
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "coverImage" to coverImage,
            "type" to type,
            "minPrice" to minPrice,
            "title" to title,
            "postId" to postId,
            "publisher" to publisher,
            "category" to category,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt,
            "description" to description,
            "images" to images,
            "membersFollowingAsFavorite" to membersFollowingAsFavorite,
        )
    }

    @Exclude
    @Suppress("UNCHECKED_CAST")
    fun getPostFromMap(map: Map<*, *>): Post {
        return Post(
            map["coverImage"] as String?,
            map["type"] as Int?,
            map["minPrice"] as Double?,
            map["title"] as String?,
            map["postId"] as String?,
            map["publisher"] as String?,
            map["category"] as MutableMap<String, Boolean>,
            map["createdAt"] as Long?,
            map["updatedAt"] as Long?,
            map["description"] as String,
            map["images"] as MutableMap<String, String>,
            map["membersFollowingAsFavorite"] as MutableMap<String, Boolean>,
        )
    }

}
package com.mahi.evergreen.model

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

/**
    This Object represents a User Category when adding it to the Firebase database
    Also storage a list of favorites post and a list of the post created by the user
 */
@IgnoreExtraProperties
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
    @Exclude
    fun toMap(): Map<String, Any?> {
            return mapOf(
                    "uid" to uid,
                    "profile" to profile,
                    "search" to search,
                    "online" to online,
                    "createdAt" to createdAt,
                    "createdPosts" to createdPosts,
                    "favoritePosts" to favoritePosts
            )
    }

        @Exclude
        @Suppress("UNCHECKED_CAST")
        fun getPostFromMap(map: Map<*, *>): User {
                return User(
                        map["uid"] as String?,
                        map["profile"] as UserProfile?,
                        map["search"] as String?,
                        map["online"] as Boolean?,
                        map["createdAt"] as Long?,
                        map["createdPosts"] as MutableMap<String, Boolean>,
                        map["favoritePosts"] as MutableMap<String, Boolean>
                )
        }
}
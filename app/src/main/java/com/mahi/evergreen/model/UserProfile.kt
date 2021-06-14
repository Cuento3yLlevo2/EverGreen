package com.mahi.evergreen.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
// This Object represents a UserProfile when adding it to the database
class UserProfile(val username: String? = "", val email: String? = "",
                  val profileImage: String? = "", var seedsPoints: Int? = 0) {
    // Null default values create a no-argument default constructor, which is needed
    // for deserialization from a DataSnapshot.
}
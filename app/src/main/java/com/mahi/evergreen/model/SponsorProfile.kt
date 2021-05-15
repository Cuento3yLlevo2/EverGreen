package com.mahi.evergreen.model

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
class SponsorProfile(val brandImage: String? = "", val title: String? = "",
                     val linkText: String? = "") {
    // Null default values create a no-argument default constructor, which is needed
    // for deserialization from a DataSnapshot.
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
                "brandImage" to brandImage,
                "title" to title,
                "linkText" to linkText
        )
    }
}
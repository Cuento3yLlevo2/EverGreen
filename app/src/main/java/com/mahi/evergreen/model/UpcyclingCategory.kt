package com.mahi.evergreen.model

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
class UpcyclingCategory (val name: String? = "", val description: String? = "",
                         val image: String? = "", val creatorHelp: String? = "", var id: Int? = 0) {
    // Null default values create a no-argument default constructor, which is needed
    // for deserialization from a DataSnapshot.
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "name" to name,
            "description" to description,
            "image" to image,
            "creatorHelp" to creatorHelp,
            "id" to id
        )
    }

    @Exclude
    fun getPostUpcyclingCategoryFromMap(map: Map<*, *>): UpcyclingCategory {
        return UpcyclingCategory(
            map["name"] as String?,
            map["description"] as String?,
            map["image"] as String?,
            map["creatorHelp"] as String?,
            map["id"] as Int?
        )
    }
}
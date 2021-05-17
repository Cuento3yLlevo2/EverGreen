package com.mahi.evergreen.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
class UpcyclingCategory (val name: String? = "", val description: String? = "",
                         val image: String? = "", val creatorHelp: String? = "", var id: Int? = 0) {
    // Null default values create a no-argument default constructor, which is needed
    // for deserialization from a DataSnapshot.
}
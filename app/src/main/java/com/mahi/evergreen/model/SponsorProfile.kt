package com.mahi.evergreen.model

class SponsorProfile(val brandImage: String? = "", val title: String? = "",
                     val linkText: String? = "") {
    // Null default values create a no-argument default constructor, which is needed
    // for deserialization from a DataSnapshot.
}
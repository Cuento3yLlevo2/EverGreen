package com.mahi.evergreen.view.adapter

import com.mahi.evergreen.model.UpcyclingCategory

/**
 * Provides a listener to control what happens when:
 * - a Upcycling Category populated in the RecyclerView is clicked
 * - The RecyclerView's Upcycling Category list has been updated
 */
interface UpcyclingCategoriesListener {
    fun onUpcyclingCategoryItemClicked(upcyclingCategoryItem: UpcyclingCategory, position: Int)
    fun observeViewModel()
}
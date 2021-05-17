package com.mahi.evergreen.view.adapter

import com.mahi.evergreen.model.UpcyclingCategory

interface UpcyclingCategoriesListener {
    fun onUpcyclingCategoryItemClicked(upcyclingCategoryItem: UpcyclingCategory, position: Int)
    fun observeViewModel()
}
package com.mahi.evergreen.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mahi.evergreen.model.UpcyclingCategory
import com.mahi.evergreen.network.Callback
import com.mahi.evergreen.network.DatabaseService

class UpcyclingCategoriesViewModel : ViewModel() {

    private val firestoreService = DatabaseService()
    var upcyclingCategoriesList: MutableLiveData<List<UpcyclingCategory>> = MutableLiveData()
    var isLoading = MutableLiveData<Boolean>()

    fun refreshUpcyclingCategoriesList() {
        getUpcyclingCategoriesFromFirebase()
    }

    private fun getUpcyclingCategoriesFromFirebase() {
        firestoreService.getUpcyclingCategoriesFromDatabase(object: Callback<List<UpcyclingCategory>>
        {
            override fun onSuccess(result: List<UpcyclingCategory>?) {
                upcyclingCategoriesList.postValue(result)
                processFinished()
            }

            override fun onFailure(exception: Exception) {
                processFinished()
            }
        }
        )
    }

    fun processFinished() {
        isLoading.value = true
    }

}
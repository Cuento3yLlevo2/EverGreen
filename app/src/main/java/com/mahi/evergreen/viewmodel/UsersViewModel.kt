package com.mahi.evergreen.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.mahi.evergreen.model.User
import com.mahi.evergreen.network.Callback
import com.mahi.evergreen.network.FirebaseDatabaseService
import java.lang.Exception

class UsersViewModel: ViewModel() {
    val firestoreService = FirebaseDatabaseService()
    var listUsers: MutableLiveData<List<User>> = MutableLiveData()
    var isLoading = MutableLiveData<Boolean>()
    private var firebaseUser: FirebaseUser? = null

    @ExperimentalStdlibApi
    fun refreshUsersList(keyword: CharSequence?) {
        if (keyword.isNullOrEmpty()) {
            getUsersFromFirebase()
        } else {
            searchForUsers(keyword.toString().lowercase())
        }
    }

    fun getUsersFromFirebase() {
        firebaseUser = Firebase.auth.currentUser
        firestoreService.getUsersExcludingCurrent(firebaseUser, object: Callback<List<User>> {
            override fun onSuccess(result: List<User>?) {
                listUsers.postValue(result)
                processFinished()
            }

            override fun onFailure(exception: Exception) {
                processFinished()
            }
        })
    }

    fun searchForUsers(keyword: String) {
        firebaseUser = Firebase.auth.currentUser
        firestoreService.getUsersQuery(keyword, firebaseUser, object: Callback<List<User>> {
            override fun onSuccess(result: List<User>?) {
                listUsers.postValue(result)
                processFinished()
            }

            override fun onFailure(exception: Exception) {
                processFinished()
            }
        })

    }

    fun getCurrentUserUID(): String? {
        firebaseUser = Firebase.auth.currentUser
        return firebaseUser?.uid
    }



    fun processFinished() {
        isLoading.value = true
    }

}
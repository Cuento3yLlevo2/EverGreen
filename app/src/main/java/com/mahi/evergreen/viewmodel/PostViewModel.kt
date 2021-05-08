package com.mahi.evergreen.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.mahi.evergreen.model.Post
import com.mahi.evergreen.network.Callback
import com.mahi.evergreen.network.DatabaseService
import java.lang.Exception

class PostViewModel : ViewModel() {

    val firestoreService = DatabaseService()
    var postList: MutableLiveData<List<Post>> = MutableLiveData()
    var isLoading = MutableLiveData<Boolean>()
    private var firebaseUser: FirebaseUser? = null

    fun refreshPostList() {
        getPostsFromFirebase()
    }

    private fun getPostsFromFirebase() {
        firestoreService.getPostsFromDatabase(object: Callback<List<Post>>
        {
            override fun onSuccess(result: List<Post>?) {
                postList.postValue(result)
                processFinished()
            }

            override fun onFailure(exception: Exception) {
                processFinished()
            }
        }
        )
    }

    fun refreshFavoritePostList() {
        firebaseUser = Firebase.auth.currentUser
        val currentUserID = firebaseUser?.uid
        getFavoritePostsFromFirebase(currentUserID)
    }

    private fun getFavoritePostsFromFirebase(currentUserID: String?) {
        if (currentUserID != null) {
            firestoreService.getFavoritePostsFromDatabase(currentUserID, object: Callback<List<Post>> {
                override fun onSuccess(result: List<Post>?) {
                    postList.postValue(result)
                    processFinished()
                }

                override fun onFailure(exception: Exception) {
                    processFinished()
                }
            }
            )
        }
    }

    fun processFinished() {
        isLoading.value = true
    }



}
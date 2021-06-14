package com.mahi.evergreen.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.mahi.evergreen.model.Post
import com.mahi.evergreen.network.Callback
import com.mahi.evergreen.network.DatabaseService

class PostViewModel : ViewModel() {

    private val firestoreService = DatabaseService()
    var postList: MutableLiveData<List<Post>> = MutableLiveData()
    var createdPostIDList: MutableLiveData<List<String>> = MutableLiveData()
    var isLoading = MutableLiveData<Boolean>()
    private var firebaseUser: FirebaseUser? = null


    fun refreshPostList() {
        getPostsFromFirebase()
    }

    fun searchForPosts(keyword: String) {
        firestoreService.getPostQuery(keyword, object: Callback<List<Post>> {
            override fun onSuccess(result: List<Post>?) {
                postList.postValue(result)
                processFinished()
            }

            override fun onFailure(exception: Exception) {
                processFinished()
            }
        })

    }

    fun refreshFavoritePostList() {
        firebaseUser = Firebase.auth.currentUser
        val currentUserID = firebaseUser?.uid
        getFavoritePostsFromFirebase(currentUserID)
    }

    fun refreshProfilePostListByType(type: Int) {
        firebaseUser = Firebase.auth.currentUser
        val currentUserID = firebaseUser?.uid
        getProfilePostsFromFirebaseByType(type, currentUserID)
    }

    fun refreshProfilePostListByCategory(categoryID: String) {
        getPostsFromFirebaseByCategory(categoryID)
    }

    fun changeFavPostState(postId: String?, action: Int) {
        firebaseUser = Firebase.auth.currentUser
        val currentUserID = firebaseUser?.uid
        changeFavPostStateInFirebase(currentUserID, postId, action)
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

    private fun getProfilePostsFromFirebaseByType(type: Int, currentUserID: String?) {
        Log.d("DebugPost", "type => $type and user => $currentUserID")
        if (currentUserID != null) {
            firestoreService.getProfilePostsFromDatabaseByType(type, currentUserID, object: Callback<List<Post>> {
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

    private fun getPostsFromFirebaseByCategory(categoryID: String) {
        firestoreService.getPostsFromDatabaseByCategory(categoryID, object: Callback<List<Post>> {
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

    private fun changeFavPostStateInFirebase(currentUserID: String?, postId: String?, action: Int) {
        firestoreService.changeFavPostStateInDatabase(currentUserID, postId, action, object: Callback<Boolean> {
            override fun onSuccess(result: Boolean?) {
                if (result == true){
                    Log.i("firestoreService", "Writing changeFavPostState success")

                } else {
                    Log.w("firestoreService", "Writing changeFavPostState failed")
                }
            }

            override fun onFailure(exception: Exception) {
                Log.w("firestoreService", exception.toString())
            }
        }
        )
    }

    fun processFinished() {
        isLoading.value = true
    }




}
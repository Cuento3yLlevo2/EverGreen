package com.mahi.evergreen.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.mahi.evergreen.model.ChatListItem
import com.mahi.evergreen.model.ChatMessageItem
import com.mahi.evergreen.network.Callback
import com.mahi.evergreen.network.FirebaseDatabaseService
import java.lang.Exception

class ChatListViewModel : ViewModel() {

    val firestoreService = FirebaseDatabaseService()
    var listOfChatListItems: MutableLiveData<List<ChatListItem>> = MutableLiveData()
    var isLoading = MutableLiveData<Boolean>()

    fun refreshChatList() {

    }

    fun getChatListFromFirebase() {
        val currentUserID = Firebase.auth.currentUser?.uid
        firestoreService.getChatListFromDatabase(currentUserID, object: Callback<List<ChatListItem>>

            {
                override fun onSuccess(result: List<ChatListItem>?) {
                    listOfChatListItems.postValue(result)
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
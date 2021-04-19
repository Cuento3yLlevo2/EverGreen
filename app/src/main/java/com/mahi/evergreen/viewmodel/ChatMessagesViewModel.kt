package com.mahi.evergreen.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mahi.evergreen.model.ChatMessageItem
import com.mahi.evergreen.network.Callback
import com.mahi.evergreen.network.FirebaseDatabaseService
import java.lang.Exception

class ChatMessagesViewModel: ViewModel() {

    val firestoreService = FirebaseDatabaseService()
    var listOfChatMessages: MutableLiveData<List<ChatMessageItem>> = MutableLiveData()
    var isLoading = MutableLiveData<Boolean>()

    fun refreshChatMessages(currentUserID: String?, visitedUserID: String, visitedUserProfileImage: String?) {
        if (currentUserID != null && visitedUserProfileImage != null){
            getChatMessagesFromFirebase(currentUserID, visitedUserID, visitedUserProfileImage)
        }
    }

    fun getChatMessagesFromFirebase(currentUserID: String, visitedUserID: String, visitedUserProfileImage: String) {
        firestoreService.getChatMessageItemsFromDatabase(
                currentUserID,
                visitedUserID,
                visitedUserProfileImage,
                object: Callback<List<ChatMessageItem>>

                {
                    override fun onSuccess(result: List<ChatMessageItem>?) {
                        listOfChatMessages.postValue(result)
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
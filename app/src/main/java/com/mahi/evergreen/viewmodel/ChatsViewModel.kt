package com.mahi.evergreen.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mahi.evergreen.model.Chat
import com.mahi.evergreen.network.Callback
import com.mahi.evergreen.network.FirebaseDatabaseService
import java.lang.Exception

class ChatsViewModel: ViewModel() {

    val firestoreService = FirebaseDatabaseService()
    var listChats: MutableLiveData<List<Chat>> = MutableLiveData()
    var isLoading = MutableLiveData<Boolean>()

    fun refreshChatList(currentUserID: String?, visitedUserID: String, visitedUserProfileImage: String?) {
        if (currentUserID != null && visitedUserProfileImage != null){
            getChatsFromFirebase(currentUserID, visitedUserID, visitedUserProfileImage)
        }
    }

    fun getChatsFromFirebase(currentUserID: String, visitedUserID: String, visitedUserProfileImage: String) {
        firestoreService.getChatsFromDatabase(
                currentUserID,
                visitedUserID,
                visitedUserProfileImage,
                object: Callback<List<Chat>>

                {
                    override fun onSuccess(result: List<Chat>?) {
                        listChats.postValue(result)
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
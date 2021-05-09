package com.mahi.evergreen.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.mahi.evergreen.model.Chat
import com.mahi.evergreen.network.Callback
import com.mahi.evergreen.network.DatabaseService
import java.lang.Exception

class ChatListViewModel : ViewModel() {

    private val firestoreService = DatabaseService()
    var chatList: MutableLiveData<List<Chat>> = MutableLiveData()
    var isLoading = MutableLiveData<Boolean>()

    fun refreshChatList() {
        getChatListFromFirebase()
    }

    private fun getChatListFromFirebase() {
        val currentUserID = Firebase.auth.currentUser?.uid
        if (currentUserID != null) {
            firestoreService.getChatListFromDatabase(currentUserID, object: Callback<List<Chat>>
                {
                    override fun onSuccess(result: List<Chat>?) {
                        chatList.postValue(result)
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
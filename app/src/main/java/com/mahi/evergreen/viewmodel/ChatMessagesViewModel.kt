package com.mahi.evergreen.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mahi.evergreen.model.ChatMessage
import com.mahi.evergreen.network.Callback
import com.mahi.evergreen.network.DatabaseService
import com.mahi.evergreen.view.ui.activities.MessageChatActivity

class ChatMessagesViewModel: ViewModel() {

    private val firestoreService = DatabaseService()
    var chatID: String = ""
    var listOfChatMessages: MutableLiveData<List<ChatMessage>> = MutableLiveData()
    var isLoading = MutableLiveData<Boolean>()



    fun writeMessage(senderID: String?, chatID: String, message: String, url: String) {
        firestoreService.writeNewMessage(senderID, chatID, message, url)
    }

    fun getChatIDAndRefreshChatMessages(
        currentUserID: String,
        visitedUserID: String,
        postID: String,
        postTitle: String,
        postImageURL: String,
        messageChatActivity: MessageChatActivity
    ) {
        firestoreService.getChatIDFromDatabase(postID, currentUserID, visitedUserID, object: Callback<String>
        {
            override fun onSuccess(result: String?) {
                if (result != null) {
                    chatID = result
                    if (chatID == ""){
                        firestoreService.writeNewChat(postImageURL, postTitle, postID, currentUserID, visitedUserID, object: Callback<String>{
                            override fun onSuccess(result: String?) {
                                if (result != null) {
                                    chatID = result
                                    refreshChatMessages(chatID)
                                    messageChatActivity.displayChatData(chatID)
                                }
                            }

                            override fun onFailure(exception: java.lang.Exception) {
                                // on faliure
                            }
                        })
                    } else {
                        Log.d("cccc2", "El Chat ID es ->>>>>>>>>>>> $chatID")
                        refreshChatMessages(chatID)
                        messageChatActivity.displayChatData(chatID)
                    }
                }
            }

            override fun onFailure(exception: java.lang.Exception) {
                // on faliure
            }
        })
    }

    fun refreshChatMessages(chatID: String) {
        getChatMessagesFromFirebase(chatID)
    }

    private fun getChatMessagesFromFirebase(chatID: String) {
        firestoreService.getChatMessageItemsFromDatabase(
                chatID,
                object: Callback<List<ChatMessage>>
                {
                    override fun onSuccess(result: List<ChatMessage>?) {
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
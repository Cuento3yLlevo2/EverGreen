package com.mahi.evergreen.network

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.mahi.evergreen.model.Chat
import com.mahi.evergreen.model.User

const val USERS_REFERENCE = "users"
const val CHATS_REFERENCE = "chats"
const val CHAT_LISTS_REFERENCE = "chatList"

class FirebaseDatabaseService {
    val database = Firebase.database("https://evergreen-app-bdbc2-default-rtdb.europe-west1.firebasedatabase.app")

    init {
        // database.setPersistenceEnabled(true)
    }

    fun getDatabaseUsersReference(): DatabaseReference {
        database.getReference(USERS_REFERENCE)
        return database.getReference(USERS_REFERENCE)
    }

    fun getUsersExcludingCurrent(firebaseUser: FirebaseUser?, callback: Callback<List<User>>){
        database.getReference(USERS_REFERENCE)
                .get()
                .addOnSuccessListener { result ->
                    var userList = ArrayList<User>()
                    for (child in result.children) {
                        Log.d("Data reading success", "${child.key} => ${child.value}")
                        if (firebaseUser != null) {
                            if (child.key?.equals(firebaseUser.uid) == false) {
                                child.getValue(User::class.java)?.let { userList.add(it) }
                            }
                        }
                    }
                    callback.onSuccess(userList)
                }
                .addOnFailureListener { exception ->
                    Log.w("Data reading failure", "Error getting documents.", exception)
                }
    }

    fun getUsersQuery(keyword: String, firebaseUser: FirebaseUser?, callback: Callback<List<User>>) {
        database.getReference(USERS_REFERENCE)
                .orderByChild("search")
                .startAt(keyword)
                .endAt(keyword + "\uf8ff")
                .get()
                .addOnSuccessListener { result ->
                    var userList = ArrayList<User>()
                    for (child in result.children) {
                        Log.d("Data reading success", "${child.key} => ${child.value}")
                        if (firebaseUser != null) {
                            if (child.key?.equals(firebaseUser.uid) == false) {
                                child.getValue(User::class.java)?.let { userList.add(it) }
                            }
                        }
                    }
                    callback.onSuccess(userList)
                }
                .addOnFailureListener { exception ->
                    Log.w("Data reading failure", "Error getting documents.", exception)
                }
    }

    fun getChatsFromDatabase(currentUserID: String, visitedUserID: String, visitedUserProfileImage: String, callback: Callback<List<Chat>>) {

        database.getReference(CHATS_REFERENCE)
                .addValueEventListener(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var chatList = ArrayList<Chat>()
                        for (child in snapshot.children) {
                            val chat = child.getValue(Chat::class.java)
                            if (chat != null) {
                                var chatsFromCurrentUserToVisitedUser = false
                                if (chat.sender.equals(currentUserID) && chat.receiver.equals(visitedUserID))
                                    chatsFromCurrentUserToVisitedUser = true
                                var chatsFromVisitedUserToCurrentUser = false
                                if (chat.sender.equals(visitedUserID) && chat.receiver.equals(currentUserID))
                                    chatsFromVisitedUserToCurrentUser = true

                                if (chatsFromCurrentUserToVisitedUser || chatsFromVisitedUserToCurrentUser) {
                                    chatList.add(chat)
                                }
                            }
                        }
                        callback.onSuccess(chatList)
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Log.w("Data reading failure", "Error getting documents.", error.toException())
                    }
                })
    }

    fun setProgressDialogWhenDataLoading(context: Context, message:String): AlertDialog {
        val llPadding = 30
        val ll = LinearLayout(context)
        ll.orientation = LinearLayout.HORIZONTAL
        ll.setPadding(llPadding, llPadding, llPadding, llPadding)
        ll.gravity = Gravity.CENTER
        var llParam = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)
        llParam.gravity = Gravity.CENTER
        ll.layoutParams = llParam

        val progressBar = ProgressBar(context)
        progressBar.isIndeterminate = true
        progressBar.setPadding(0, 0, llPadding, 0)
        progressBar.layoutParams = llParam

        llParam = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
        llParam.gravity = Gravity.CENTER
        val tvText = TextView(context)
        tvText.text = message
        tvText.setTextColor(Color.parseColor("#000000"))
        tvText.textSize = 20.toFloat()
        tvText.layoutParams = llParam

        ll.addView(progressBar)
        ll.addView(tvText)

        val builder = AlertDialog.Builder(context)
        builder.setCancelable(true)
        builder.setView(ll)

        val dialog = builder.create()
        val window = dialog.window
        if (window != null) {
            val layoutParams = WindowManager.LayoutParams()
            layoutParams.copyFrom(dialog.window?.attributes)
            layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
            layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT
            dialog.window?.attributes = layoutParams
        }
        return dialog
    }



}
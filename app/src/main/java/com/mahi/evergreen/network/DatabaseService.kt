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
import com.mahi.evergreen.model.*

const val USERS = "users"
const val CHATS = "chats"
const val CHAT_MESSAGES = "chatMessages"
const val CHAT_MEMBERS = "chatMembers"
const val POSTS = "posts"
const val UPCYCLING_CATEGORIES = "upcyclingCategories"
const val POST_SERVICE_TYPE = 1
const val POST_IDEA_TYPE = 2
// const val POST_AD_TYPE = 3

class DatabaseService {
    val database = Firebase.database("https://evergreen-app-bdbc2-default-rtdb.europe-west1.firebasedatabase.app")

    // Write to Realtime Database

    fun writeNewUser(userId: String, username: String, email: String, callback: Callback<Boolean>) {
        val defaultProfileImage = "https://firebasestorage.googleapis.com/v0/b/evergreen-app-bdbc2.appspot.com/o/user_default.png?alt=media&token=53382a72-8fc1-4a11-b63f-0cb342bb02c6"
        val defaultCoverImage = "https://firebasestorage.googleapis.com/v0/b/evergreen-app-bdbc2.appspot.com/o/profile_cover_default.jpg?alt=media&token=07a0cfd5-a6df-4877-8ea6-8e0ecc2d98fb"
        val createdAt = System.currentTimeMillis()
        val userProfile = UserProfile(
                username,
                email,
                defaultProfileImage,
                defaultCoverImage,
            0
        )

        val user = User(
                userId,
                userProfile,
                username.lowercase(),
                false,
                createdAt
        )

        database.reference.child(USERS).child(userId).setValue(user)
                .addOnSuccessListener {
                    // Write was successful!
                        Log.w("FireBaseLogs", "Write was successful")
                    callback.onSuccess(true)
                }
                .addOnFailureListener {
                     // Write failed
                        Log.w("FireBaseLogs", "Write failed")
                }
    }

    fun writeNewChat(
        postImageURL: String,
        postTitle: String,
        postID: String,
        receiverID: String,
        senderID: String,
        callback: Callback<String>
    ) {
        var chatID: String
        val currentTime = System.currentTimeMillis()
        val chatKey = database.reference.push().key
        if (chatKey != null) {

            val chat = Chat(postID, postTitle, postImageURL, null, false, currentTime, chatKey)
            val chatMembers = ChatMembers(chatKey, postID, hashMapOf(senderID to true, receiverID to true))
            database.reference.child(CHATS).child(chatKey).setValue(chat).addOnCompleteListener { task ->
                if (task.isSuccessful){
                    database.reference.child(CHAT_MEMBERS).child(chatKey).setValue(chatMembers).addOnCompleteListener {
                        task2 ->
                        if (task2.isSuccessful){
                            chatID = chatKey
                            callback.onSuccess(chatID)
                        }
                    }
                }
            }
        }
    }

    fun writeNewMessage(senderID: String?, chatKey: String, message: String, url: String) {
        val currentTime = System.currentTimeMillis()
        val messageKey = database.reference.child(CHAT_MESSAGES).child(chatKey).push().key
        val chatMessage = ChatMessage(messageKey, senderID, message, url, false, currentTime)
        if (messageKey != null) {
            database.reference.child(CHAT_MESSAGES)
                    .child(chatKey)
                    .child(messageKey)
                    .setValue(chatMessage)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("WriteData", "Message creation successful")
                            database.reference.child(CHATS).child(chatKey).updateChildren(hashMapOf<String, Any>(
                                    "timestamp" to currentTime,
                                    "lastMessage" to message))
                        }
                    }
        }
    }

    fun writeNewPost(coverImage: String,
                     type: Int,
                     minPrice: Double?,
                     title: String,
                     publisherID: String,
                     category: MutableMap<String, Boolean>,
                     description: String,
                     images: MutableMap<String, String>,
                     callback: Callback<Boolean>) {

        val createdAt = System.currentTimeMillis()
        val postKey = database.reference.child(POSTS).push().key
        val post = Post(
            coverImage,
            type,
            minPrice,
            title,
            postKey,
            publisherID,
            category,
            createdAt,
            createdAt, // updatedAt
            description,
            images
        )

        if (postKey != null) {
            database.reference.child(POSTS).child(postKey).setValue(post)
                .addOnSuccessListener {
                    // Write was successful!
                    Log.w("FireBaseLogs", "Write was successful")

                    database.reference.child(USERS).child(publisherID).child("createdPosts").updateChildren(hashMapOf<String, Any>(postKey to true))

                    callback.onSuccess(true)
                }
                .addOnFailureListener {
                    // Write failed
                    Log.w("FireBaseLogs", "Write failed")
                }
        }
    }



    // Read From Realtime Database


    fun usernameAlreadyExists(newUsername: String, callback: Callback<Boolean>) {
        database.getReference(USERS)
                .get()
                .addOnSuccessListener { result ->
                    var exists = false
                    for (child in result.children) {
                        Log.d("Data reading success", "${child.key} => ${child.value}")
                        val user = child.getValue(User::class.java)
                        if (user != null) {
                            Log.d("Data reading success 2", "$newUsername => ${user.search}")
                            if (user.search.equals(newUsername, true)) {
                                exists = true
                            }
                        }
                    }
                    Log.d("Data reading success 3", "$exists")
                    callback.onSuccess(exists)
                }
                .addOnFailureListener { exception ->
                    Log.w("Data reading failure", "Error getting documents.", exception)
                }
    }

    fun getDatabaseUsersReference(): DatabaseReference {
        database.getReference(USERS)
        return database.getReference(USERS)
    }

    fun getUsersExcludingCurrent(firebaseUser: FirebaseUser?, callback: Callback<List<User>>){
        database.getReference(USERS)
                .get()
                .addOnSuccessListener { result ->
                    val userList = ArrayList<User>()
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
        database.getReference(USERS)
                .orderByChild("search")
                .startAt(keyword)
                .endAt(keyword + "\uf8ff")
                .get()
                .addOnSuccessListener { result ->
                    val userList = ArrayList<User>()
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

    fun getChatMessageItemsFromDatabase(chatID: String, callback: Callback<List<ChatMessage>>) {
        Log.d("ccccData", "El Chat ID es ->>>>>>>>>>>> $chatID")
        database.getReference(CHAT_MESSAGES)
                .child(chatID)
                .orderByChild("timestamp")
                .addValueEventListener(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val chatList = ArrayList<ChatMessage>()
                        for (child in snapshot.children) {
                            val chatMessage = child.getValue(ChatMessage::class.java)
                            if (chatMessage != null) {
                                chatList.add(chatMessage)
                            }
                        }
                        callback.onSuccess(chatList)
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Log.w("Data reading failure", "Error getting documents.", error.toException())
                    }
                })
    }

    fun getChatIDFromDatabase(
        postID: String,
        currentUserID: String,
        visitedUserID: String,
        callback: Callback<String>
    ) {
        var chatID = ""
        database.getReference(CHAT_MEMBERS)
                .get()
                .addOnSuccessListener { result ->
                    for (child in result.children) {
                        val chatMembers : ChatMembers? = child.getValue(ChatMembers::class.java)
                        if (chatMembers != null) {
                            if (chatMembers.postID == postID) {
                                if(chatMembers.members == hashMapOf(currentUserID to true, visitedUserID to true)){
                                    chatID = chatMembers.chatID.toString()
                                } else if (chatMembers.members == hashMapOf(visitedUserID to true, currentUserID to true)) {
                                    chatID = chatMembers.chatID.toString()
                                }
                            }
                        }
                    }
                    callback.onSuccess(chatID)
                }
                .addOnFailureListener { exception ->
                    Log.w("Data reading failure", "Error getting documents.", exception)
                }
    }

    fun getChatUsername(chatID: String, currentUserID: String, callback: Callback<String>) {
        var chatUsernameID: String
        var chatUsername: String
        database.getReference(CHAT_MEMBERS).child(chatID)
                .get()
                .addOnSuccessListener { result ->
                    val chatMembers : ChatMembers? = result.getValue(ChatMembers::class.java)
                    if (chatMembers != null) {
                        for (member in chatMembers.members){
                            if (member.key != currentUserID){
                                chatUsernameID = member.key
                                database.getReference(USERS)
                                        .child(chatUsernameID)
                                        .child("profile")
                                        .child("username")
                                        .get()
                                        .addOnSuccessListener { chatUsernameResult ->
                                            chatUsername = chatUsernameResult.getValue(String::class.java).toString()
                                            callback.onSuccess(chatUsername)
                                        }
                                        .addOnFailureListener { exception ->
                                            Log.w("Data reading failure", "Error getting documents.", exception)
                                        }
                            }
                        }

                    }
                }
                .addOnFailureListener { exception ->
                    Log.w("Data reading failure", "Error getting documents.", exception)
                }
    }

    fun getUserIDVisited(chatIDFromChatList: String, currentUserID: String, callback: Callback<String>) {
        var chatUsernameID: String
        database.getReference(CHAT_MEMBERS).child(chatIDFromChatList)
            .get()
            .addOnSuccessListener { result ->
                val chatMembers : ChatMembers? = result.getValue(ChatMembers::class.java)
                if (chatMembers != null) {
                    for (member in chatMembers.members){
                        if (member.key != currentUserID){
                            chatUsernameID = member.key
                            callback.onSuccess(chatUsernameID)
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.w("Data reading failure", "Error getting documents.", exception)
            }
    }

    fun getChatListFromDatabase(currentUserID: String, callback: Callback<List<Chat>>){
        val chatRefList = ArrayList<String>()
        database.getReference(CHAT_MEMBERS)
                .addValueEventListener(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (child in snapshot.children) {
                            val chatMembers = child.getValue(ChatMembers::class.java)
                            if (chatMembers != null) {
                                for (member in chatMembers.members){
                                    if (member.key == currentUserID){
                                        chatMembers.chatID?.let { chatRefList.add(it) }
                                    }
                                }
                            }
                        }
                        database.getReference(CHATS)
                                .orderByChild("timestamp")
                                .addValueEventListener(object : ValueEventListener{
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        val chatList = ArrayList<Chat>()
                                        for (child in snapshot.children) {
                                            val chat = child.getValue(Chat::class.java)
                                            if (chat != null) {
                                                for ((index, value) in chatRefList.withIndex()){
                                                    if (chat.chatID.equals(value)){

                                                        chatList.add(chat)
                                                        chatRefList.removeAt(index)
                                                        break
                                                    }
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
                    override fun onCancelled(error: DatabaseError) {
                        Log.w("Data reading failure", "Error getting documents.", error.toException())
                    }
                })
    }

    fun getUpcyclingCategoriesFromDatabase(callback: Callback<List<UpcyclingCategory>>) {
        database.getReference(UPCYCLING_CATEGORIES)
            .orderByChild("name")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val upcyclingCategList = ArrayList<UpcyclingCategory>()
                    for (child in snapshot.children) {
                        val upcyclingCategory = child.getValue(UpcyclingCategory::class.java)
                        if (upcyclingCategory != null) {
                            upcyclingCategList.add(upcyclingCategory)
                        }
                    }
                    callback.onSuccess(upcyclingCategList)
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.w("Data reading failure", "Error getting documents.", error.toException())
                }
            })

    }

    fun getPostsFromDatabase(callback: Callback<List<Post>>) {
        database.getReference(POSTS)
                .orderByChild("updatedAt")
                .addValueEventListener(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val postList = ArrayList<Post>()
                        for (child in snapshot.children) {
                            val post = child.getValue(Post::class.java)
                            if (post != null) {
                                postList.add(post)
                            }
                        }
                        callback.onSuccess(postList)
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Log.w("Data reading failure", "Error getting documents.", error.toException())
                    }
                })
    }

    fun getFavoritePostsFromDatabase(currentUserID: String, callback: Callback<List<Post>>) {
        database.getReference(USERS)
                .child(currentUserID)
                .child("favoritePosts").addValueEventListener(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val favoritePostsList = ArrayList<String>()
                        for (child in snapshot.children) {
                            child.key?.let { favoritePostsList.add(it) }
                        }
                        val postList = ArrayList<Post>()
                        var counter = 1
                        if (favoritePostsList.isEmpty()){
                            callback.onSuccess(postList)
                        }
                        for (favoritePost in favoritePostsList) {
                            database.getReference(POSTS).child(favoritePost)
                                    .addValueEventListener(object : ValueEventListener{
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            val post = snapshot.getValue(Post::class.java)
                                            if (post != null) {
                                                postList.add(post)
                                            }
                                            if (counter == favoritePostsList.size){
                                                callback.onSuccess(postList)
                                            }
                                            counter++
                                        }
                                        override fun onCancelled(error: DatabaseError) {
                                            counter++
                                            Log.w("Data reading failure", "Error getting documents.", error.toException())
                                        }
                                    })
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Log.w("Data reading failure", "Error getting documents.", error.toException())
                    }
                })
    }

    fun getProfilePostsFromDatabaseByType(type: Int, currentUserID: String, callback: Callback<List<Post>>) {
        database.getReference(USERS)
                .child(currentUserID)
                .child("createdPosts").addValueEventListener(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val createdPostsList = ArrayList<String>()
                        for (child in snapshot.children) {
                            child.key?.let { createdPostsList.add(it) }
                        }
                        val postList = ArrayList<Post>()
                        var counter = 1
                        if (createdPostsList.isEmpty()){
                            callback.onSuccess(postList)
                        }
                        for (createdPost in createdPostsList) {
                            getPostFromDatabaseByType(type, createdPost, object: Callback<Post> {
                                override fun onSuccess(result: Post?) {
                                    if (result != null) {
                                        postList.add(result)
                                    }
                                    Log.d("DebugPost", "counter => $counter Listsize => ${createdPostsList.size}")
                                    if (counter == createdPostsList.size){
                                        callback.onSuccess(postList)
                                    }
                                    counter++
                                }
                                override fun onFailure(exception: Exception) {
                                    counter++

                                }
                            }
                            )
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Log.w("Data reading failure", "Error getting documents.", error.toException())
                    }
                })
    }



    private fun getPostFromDatabaseByType(type: Int, createdPost: String, callback: Callback<Post>) {
        database.getReference(POSTS).child(createdPost)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val post = snapshot.getValue(Post::class.java)
                    if (post != null) {
                        if (post.type == type) {
                            callback.onSuccess(post)
                        } else {
                            callback.onSuccess(null)
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.w("Data reading failure", "Error getting documents.", error.toException())
                }
            })
    }

    // Extra Tools

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
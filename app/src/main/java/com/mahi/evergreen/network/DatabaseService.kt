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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.mahi.evergreen.model.*
import java.util.*
import kotlin.collections.ArrayList

const val USERS = "users"
const val CHATS = "chats"
const val CHAT_MESSAGES = "chatMessages"
const val CHAT_MEMBERS = "chatMembers"
const val POSTS = "posts"
const val UPCYCLING_CATEGORIES = "upcyclingCategories"
const val POST_SERVICE_TYPE = 1
const val POST_IDEA_TYPE = 2
// const val POST_AD_TYPE = 3
const val REMOVE_FAV_POST = 0
const val ADD_FAV_POST = 1

/**
 * Manages main interactions with Firebase Realtime Database.
 * Takes care of Database updates, inserts, deletes
 * Firebase Realtime Database is a non-relational database base in JSON
 * for more info go to https://firebase.google.com/docs/database/android/read-and-write
 */
class DatabaseService {

    // project's server database reference host by firebase services (for more info go to https://firebase.google.com/)
    val database = Firebase.database("https://evergreen-app-bdbc2-default-rtdb.europe-west1.firebasedatabase.app")

    // Write to Realtime Database

    /**
     * Writes a new user to database using User.kt and UserProfile.kt Models
     * handles registrations with Email, Google Token and Facebook Token
     */
    fun writeNewUser(userId: String, usernameInput: String?, email: String?, profileImageInput: String?, callback: Callback<Boolean>) {
        val defaultProfileImage = "https://firebasestorage.googleapis.com/v0/b/evergreen-app-bdbc2.appspot.com/o/user_default.png?alt=media&token=53382a72-8fc1-4a11-b63f-0cb342bb02c6"
        val profileImage = profileImageInput ?: defaultProfileImage
        val username = usernameInput ?: "Introduce un nombre de usuario"
        val createdAt = System.currentTimeMillis()
        val userProfile = UserProfile(
                username,
                email,
                profileImage,
            3
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
                        Log.i("FireBaseLogs", "Write was successful")
                    callback.onSuccess(true)
                }
                .addOnFailureListener {
                     // Write failed
                        Log.w("FireBaseLogs", "Write failed")
                }
    }

    /**
     * Writes a new chat to database using Chat.kt and ChatMembers.kt
     * this new chat will start without ChatMessages and these will be created
     * by the user later
     */
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

    /**
     * Writes a new single messages to the database using ChatMessage.kt and also ads the message
     * to the ChatMessages list
     */
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

    /**
     * Writes new post to the database using Post.kt
     * Also updates the user created post list
     */
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
                    addSeedsPointsToUser(publisherID, 3)

                    callback.onSuccess(true)
                }
                .addOnFailureListener {
                    // Write failed
                    Log.w("FireBaseLogs", "Write failed")
                }
        }
    }

    /**
     * Given a number of points this class add the new Seeds points to the current user points
     */
    private fun addSeedsPointsToUser(userID: String, newPoints: Int) {
        database.reference.child(USERS)
            .child(userID)
            .child("profile")
            .child("seedsPoints")
            .get()
            .addOnSuccessListener { result ->
                Log.d("Data reading success", "${result.key} => ${result.value}")
                val currentPoints : Long = result.value as Long
                val finalPoints = currentPoints.plus(newPoints)
                database.reference.child(USERS)
                    .child(userID)
                    .child("profile")
                    .child("seedsPoints").setValue(finalPoints)
                    .addOnSuccessListener {
                    // Write was successful!
                    Log.w("FireBaseLogs", "Write was successful")
                    }
                    .addOnFailureListener {
                        // Write failed
                        Log.w("FireBaseLogs", "Write failed")
                    }
            }
            .addOnFailureListener { exception ->
                Log.w("Data reading failure", "Error getting documents.", exception)
            }
    }

    /**
     * Removes or Adds a given post from the user's favorites list depending on the input
     * on the action param
     */
    fun changeFavPostStateInDatabase(currentUserID: String?, postId: String?, action: Int, callback: Callback<Boolean>) {
        if(action == ADD_FAV_POST){
            val childUpdates = hashMapOf<String, Any>(
                "/posts/$postId/membersFollowingAsFavorite/$currentUserID" to true,
                "/users/$currentUserID/favoritePosts/$postId" to true
            )
            database.reference.updateChildren(childUpdates)
                .addOnSuccessListener { callback.onSuccess(true) }
                .addOnFailureListener { callback.onSuccess(false) }
        }
        else if (action == REMOVE_FAV_POST){
            val childUpdates = hashMapOf<String, Any?>(
                "/posts/$postId/membersFollowingAsFavorite/$currentUserID" to null,
                "/users/$currentUserID/favoritePosts/$postId" to null
            )
            database.reference.updateChildren(childUpdates)
                .addOnSuccessListener { callback.onSuccess(true) }
                .addOnFailureListener { callback.onSuccess(false) }
        }
    }

    /**
     * Updates the user's username given the username string as input
     */
    fun updateUsername(userID: String, newUsername: String, callback: Callback<Boolean>) {
        database.reference.child(USERS)
            .child(userID)
            .child("profile")
            .child("username").setValue(newUsername)
            .addOnSuccessListener {
                // Write was successful!
                database.reference.child(USERS)
                    .child(userID)
                    .child("search").setValue(newUsername.lowercase(Locale.getDefault()))
                    .addOnSuccessListener {
                        // Write was successful!
                        Log.i("FireBaseLogs", "Write was successful")
                        callback.onSuccess(true)
                    }
                    .addOnFailureListener {
                        // Write failed
                        Log.w("FireBaseLogs", "Write failed")
                    }
            }
            .addOnFailureListener {
                // Write failed
                Log.w("FireBaseLogs", "Write failed")
            }
    }



    // Read From Realtime Database

    /**
     * Determines whether a username already exists on the database and returns
     * True on the callback if it does
     */
    fun usernameAlreadyExists(newUsername: String, callback: Callback<Boolean>) {
        database.getReference(USERS)
                .get()
                .addOnSuccessListener { result ->
                    var exists = false
                    for (child in result.children) {
                        val user = child.getValue(User::class.java)
                        if (user != null) {
                            if (user.search.equals(newUsername, true)) {
                                exists = true
                            }
                        }
                    }
                    callback.onSuccess(exists)
                }
                .addOnFailureListener { exception ->
                    Log.w("Data reading failure", "Error getting documents.", exception)
                }
    }

    /**
     * Mainly use for Google and Facebook token singIn.
     * Since these singIns does not control whether in the database already exists a user
     * related to the token, this method takes care of that returning True in the Callback is the
     * user already exists
     */
    fun userAlreadyExists(uid: String, callback: Callback<Boolean>) {
        database.getReference(USERS)
            .child(uid)
            .get()
            .addOnSuccessListener { result ->
                var exists = false
                if(result.exists()){
                    exists = true
                }
                callback.onSuccess(exists)
            }
            .addOnFailureListener { exception ->
                Log.w("Data reading failure", "Error getting documents.", exception)
            }
    }

    /**
     * Provides users reference data from Database
     */
    fun getDatabaseUsersReference(): DatabaseReference {
        database.getReference(USERS)
        return database.getReference(USERS)
    }

    /**
     * Populates the list of previous chat messages when a user starts a chat activity
     */
    fun getChatMessageItemsFromDatabase(chatID: String, callback: Callback<List<ChatMessage>>) {
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

    /**
     * Retrieves the chatID for a given postID and chat members
     */
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

    /**
     * Retrieves chat user name to display on a chat activity given a chatID and current user ID
     */
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

    /**
     * Retrieves the UserID for the visited user id on a chat activity given the currentUserID to
     * know which one is the current user and which one is the visited
     */
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

    /**
     * Populates the main ChatListFragment and show all active chats for the current user
     */
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

    /**
     * Retrieves all the data for the upcycling categories from the database to populate the upcycling categories list
     */
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

    /**
     * This method retrieves all the posts crated on the database
     * in order of updated post first. It populates the HomeFragment
     */
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

    /**
     * This method retrieves all the posts that the given user has set as favorites
     * from the database. It populates the FavoritesFragment
     */
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

    /**
     * Retrieves all post matching the category id given
     */
    fun getPostsFromDatabaseByCategory(categoryID: String, callback: Callback<List<Post>>) {
        database.getReference(POSTS)
            .orderByChild("updatedAt")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val postList = ArrayList<Post>()
                    for (child in snapshot.children) {
                        val post = child.getValue(Post::class.java)
                        if (post != null) {
                            if (post.category.containsKey(categoryID)){
                                postList.add(post)
                            }
                        }
                    }
                    callback.onSuccess(postList)
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.w("Data reading failure", "Error getting documents.", error.toException())
                }
            })

    }

    /**
     * Retrieves user's created Upcycling Services or Ideas given the user id
     */
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

    /**
     * Given a list of post ids this method will return only the post that match with the given post type
     */
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

    /**
     * Search for posts matching the post description string provided as param
     */
    fun getPostQuery(keyword: String, callback: Callback<List<Post>>) {
        database.getReference(POSTS)
            .orderByChild("description")
            .startAt(keyword)
            .endAt(keyword + "\uf8ff")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val postList = ArrayList<Post>()
                    for (child in snapshot.children) {
                        child.getValue(Post::class.java)?.let { postList.add(it) }
                    }
                    callback.onSuccess(postList)
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.w("Data reading failure", "Error getting documents.", error.toException())
                }
            })
    }

    // Extra Tools
    /**
     * This tool shows a charging dialog window to the user until data is successfully wrote on the database
     * It is only use when loading heavy images to de Firebase Storage service
     */
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
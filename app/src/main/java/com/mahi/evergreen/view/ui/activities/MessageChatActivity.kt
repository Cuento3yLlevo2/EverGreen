package com.mahi.evergreen.view.ui.activities

import android.app.PendingIntent.getActivity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.core.view.size
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import com.mahi.evergreen.databinding.ActivityMessageChatBinding
import com.mahi.evergreen.model.Chat
import com.mahi.evergreen.model.User
import com.mahi.evergreen.network.FirebaseDatabaseService
import com.mahi.evergreen.view.adapter.ChatsAdapter
import com.mahi.evergreen.view.adapter.ChatsListener
import com.mahi.evergreen.viewmodel.ChatsViewModel
import com.squareup.picasso.Picasso

class MessageChatActivity : AppCompatActivity(), ChatsListener {

    private lateinit var chatsAdapter: ChatsAdapter
    private lateinit var viewModel: ChatsViewModel

    private lateinit var binding: ActivityMessageChatBinding
    private var userIDVisited: String = ""
    private var firebaseUser: FirebaseUser? = null
    private var firebaseDatabaseService = FirebaseDatabaseService()
    val reference = firebaseDatabaseService.database.reference

    val imageMessageRequestCode = 878
    private var imageUri: Uri? = null
    private var storageRef: StorageReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessageChatBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        // retrieve from previous activity the Visited User UID
        userIDVisited = intent.getStringExtra("visit_user_id").toString()
        // retrieve from Firebase.auth the current user UID
        firebaseUser = Firebase.auth.currentUser

        viewModel = ViewModelProvider(this).get(ChatsViewModel::class.java)


        // Display Chat List for Layout
        val visitedUserDbRef = reference.child("users").child(userIDVisited)
        visitedUserDbRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val visitedUserData: User? = snapshot.getValue(User::class.java)
                if (visitedUserData?.profileImage != null) {
                    Picasso.get().load(visitedUserData.profileImage).into(binding.ivVisitedProfileImage)

                    viewModel.refreshChatList(firebaseUser?.uid, userIDVisited, visitedUserData.profileImage)

                    chatsAdapter = ChatsAdapter(this@MessageChatActivity, visitedUserData.profileImage)

                    binding.rvMessageChats.apply {
                        val linearLayoutManager = LinearLayoutManager(view.context, LinearLayoutManager.VERTICAL, false)
                        linearLayoutManager.stackFromEnd = true
                        layoutManager = linearLayoutManager
                        adapter = chatsAdapter
                        setHasFixedSize(true)
                    }

                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w("Data reading failure", "Error getting documents.", error.toException())
            }
        })


        // When Current User presses the SendMessageBtn the message will be sent to visited user's phone
        binding.ivSendMessageBtn.setOnClickListener {
            val message = binding.etTextMessage.text.toString()
            sendMessageToUser(firebaseUser?.uid, userIDVisited, message)
        }

        // When Current User presses the AttachImageBtn will appear an activity to ask for the image
        binding.ivAttachImageFileBtn.setOnClickListener {
            createImageMessageIntent()
        }

        observeViewModel()
    }

    /**
     * Creates Intent that will ask the user for an image on device storage
     */
    private fun createImageMessageIntent() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        startActivityForResult(Intent.createChooser(intent,"Pick Image"), imageMessageRequestCode)
    }


    /***
     * Sends texts message from current user to visited user and updates the database to get results on realtime
     */
    private fun sendMessageToUser(senderID: String?, receiverID: String, message: String) {
        if (message.isBlank()){
            Toast.makeText(this@MessageChatActivity, "El campo de texto esta vació!", Toast.LENGTH_LONG).show()
        } else {
            val messageKey = reference.push().key
            val chat: Chat = Chat(senderID, message, receiverID, false, "", messageKey)
            if (messageKey != null && senderID != null) {
                reference.child("chats").child(messageKey).setValue(chat).addOnCompleteListener { task ->
                    if (task.isSuccessful){
                        createChatsRefereces(senderID)
                    }
                }
            }
        }
        binding.etTextMessage.setText("")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null) {

            // If the activity resulted on imageMessageRequestCode we now tha we need to retrieve an image URL from previous activity.
            if (requestCode == imageMessageRequestCode && resultCode == RESULT_OK && data.data != null){
                imageUri = data.data
                uploadImageToDatabase(this)
            }
        }
    }


    /**
     * Retrieves the Attached image from Activity collecting the Image uri and saving URL on database
     */
    private fun uploadImageToDatabase(context: Context) {
        val progressBar = firebaseDatabaseService.setProgressDialogWhenDataLoading(this, "La imagen se está enviando...")
        progressBar.show()

        if (imageUri!=null){

            val messageID = reference.push().key
            storageRef = Firebase.storage.reference.child("Chat Images")
            val fileRef = storageRef?.child("$messageID.jpg")

            var uploadTask: StorageTask<*>

            if (fileRef != null) {
                uploadTask = fileRef.putFile(imageUri!!)
                uploadTask.continueWithTask(Continuation <UploadTask.TaskSnapshot, Task<Uri>>{ task ->
                    if (!task.isSuccessful){
                        task.exception?.let {
                            throw it
                        }

                    }
                    return@Continuation fileRef.downloadUrl
                }).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val downloadUri = task.result
                        val url = downloadUri.toString()
                        val senderID = firebaseUser?.uid

                        val chat: Chat = Chat(senderID,
                                "sent you an image.",
                                userIDVisited,
                                false,
                                url,
                                messageID)

                        if (messageID != null && senderID != null) {
                            reference.child("chats")
                                    .child(messageID)
                                    .setValue(chat)
                                    .addOnCompleteListener { task ->
                                if (task.isSuccessful){
                                    createChatsRefereces(senderID)
                                }
                            }
                        }
                        progressBar.dismiss()
                    }

                }
            }
        }
    }



    /**
     * Creates Chat References to be able to push notifications with the last messages to users
     */
    private fun createChatsRefereces(senderID: String) {
        val chatsListReferenceSender = reference
                .child("chatLists")
                .child(senderID)
                .child(userIDVisited)
        chatsListReferenceSender.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(!snapshot.exists()){
                    chatsListReferenceSender.child("id").setValue(userIDVisited)
                }
                val chatsListReferenceReceiver = reference
                        .child("chatLists")
                        .child(userIDVisited)
                        .child(senderID)
                chatsListReferenceReceiver.child("id").setValue(senderID)
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w("Data reading failure", "Error getting documents.", error.toException())
            }
        })
    }

    override fun onChatClicked(chat: Chat, position: Int) {
        // what happen if a chat is clicked
    }

    override fun observeViewModel() {
        viewModel.listChats.observe(this, Observer<List<Chat>> {chat ->
            chatsAdapter.updateData(chat)
            binding.rvMessageChats.smoothScrollToPosition(chatsAdapter.listChats.size)
        })

        viewModel.isLoading.observe(this, Observer<Boolean> {
            if(it != null)
                binding.rlBaseChats.visibility = View.INVISIBLE
        })
    }




}
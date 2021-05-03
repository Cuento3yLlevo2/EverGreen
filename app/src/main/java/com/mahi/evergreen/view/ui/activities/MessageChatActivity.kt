package com.mahi.evergreen.view.ui.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
import com.mahi.evergreen.model.*
import com.mahi.evergreen.network.FirebaseDatabaseService
import com.mahi.evergreen.view.adapter.ChatMessagesAdapter
import com.mahi.evergreen.view.adapter.ChatMessagesListener
import com.mahi.evergreen.viewmodel.ChatMessagesViewModel
import com.squareup.picasso.Picasso

class MessageChatActivity : AppCompatActivity(), ChatMessagesListener {

    private lateinit var binding: ActivityMessageChatBinding

    private lateinit var chatMessagesAdapter: ChatMessagesAdapter
    private lateinit var viewModel: ChatMessagesViewModel
    private var userIDVisited: String = ""
    private var firebaseUser: FirebaseUser? = null
    private var firebaseDatabaseService = FirebaseDatabaseService()
    private val reference = firebaseDatabaseService.database.reference
    private val imageMessageRequestCode = 878
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

        viewModel = ViewModelProvider(this).get(ChatMessagesViewModel::class.java)

        // Display Chat List for Layout
        val visitedUserDbRef = reference.child("users").child(userIDVisited)
        visitedUserDbRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                val visitedUserData: User? = snapshot.getValue(User::class.java)
                if (visitedUserData?.profile?.profileImage != null) {
                    Picasso.get().load(visitedUserData.profile?.profileImage).into(binding.ivVisitedProfileImage)

                    firebaseUser?.uid?.let {
                        viewModel.getChatIDAndRefreshChatMessages(it, userIDVisited)
                        // viewModel.refreshChatMessages(viewModel.chatID)
                    }

                    chatMessagesAdapter = ChatMessagesAdapter(this@MessageChatActivity, visitedUserData.profile?.profileImage!!)

                    binding.rvMessageChats.apply {
                        val linearLayoutManager = LinearLayoutManager(view.context, LinearLayoutManager.VERTICAL, false)
                        linearLayoutManager.stackFromEnd = true
                        layoutManager = linearLayoutManager
                        adapter = chatMessagesAdapter
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
            sendMessageToUser(firebaseUser?.uid, message)
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
    private fun sendMessageToUser(senderID: String?, message: String) {
        if (message.isBlank()){
            Toast.makeText(this@MessageChatActivity, "El campo de texto esta vació!", Toast.LENGTH_LONG).show()
        } else {
            Log.d("ccccMessage", "El Viewmodel Chat ID es ->>>>>>>>>>>> ${viewModel.chatID}")
            viewModel.writeMessage(senderID, viewModel.chatID, message, "")
        }
        binding.etTextMessage.setText("")
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null) {

            // If the activity resulted on imageMessageRequestCode we now tha we need to retrieve an image URL from previous activity.
            if (requestCode == imageMessageRequestCode && resultCode == RESULT_OK && data.data != null){
                imageUri = data.data
                uploadImageToDatabase()
            }
        }
    }


    /**
     * Retrieves the Attached image from Activity collecting the Image uri and saving URL on database
     */
    private fun uploadImageToDatabase() {
        val progressBar = firebaseDatabaseService.setProgressDialogWhenDataLoading(this, "La imagen se está enviando...")
        progressBar.show()

        if (imageUri!=null){

            val imageID = reference.push().key
            storageRef = Firebase.storage.reference.child("Chat Images")
            val fileRef = storageRef?.child("$imageID.jpg")

            val uploadTask: StorageTask<*>

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
                        val message = "sent you an image."

                        Log.d("ccccImage", "El Chat ID es ->>>>>>>>>>>> ${viewModel.chatID}")

                        viewModel.writeMessage(senderID, viewModel.chatID, message, url)

                        progressBar.dismiss()
                    }

                }
            }
        }
    }

    override fun onChatClicked(chatMessageItem: ChatMessage, position: Int) {
        // what happen if a chat is clicked
    }

    override fun observeViewModel() {
        viewModel.listOfChatMessages.observe(this, { chat ->
            chatMessagesAdapter.updateData(chat)
            binding.rvMessageChats.smoothScrollToPosition(chatMessagesAdapter.listOfMessages.size)
        })

        viewModel.isLoading.observe(this, {
            if(it != null)
                binding.rlBaseChats.visibility = View.INVISIBLE
        })
    }

}
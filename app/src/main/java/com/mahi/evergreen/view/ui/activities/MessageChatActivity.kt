package com.mahi.evergreen.view.ui.activities

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
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
import com.squareup.picasso.Picasso

class MessageChatActivity : AppCompatActivity() {

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

        // Display VisitedUser data on Chat Toolbar
        val visitedUserDbRef = reference.child("users").child(userIDVisited)
        visitedUserDbRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val user: User? = snapshot.getValue(User::class.java)
                if (user != null) {
                    Picasso.get().load(user.profileImage).into(binding.ivVisitedProfileImage)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // do something
            }
        })

        // When Current User presses the SendMessageBtn the message will be sent to visited user's phone
        binding.ivSendMessageBtn.setOnClickListener {
            val message = binding.etTextMessage.text.toString()
            if (message.isBlank()){
                Toast.makeText(this@MessageChatActivity, "El campo de texto esta vació!", Toast.LENGTH_LONG).show()
            } else {
                // This method implements sending chat messages and pushing notifications
                sendMessageToUser(firebaseUser?.uid, userIDVisited, message)
            }
            binding.etTextMessage.setText("")
        }

        binding.ivAttachImageFileBtn.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(Intent.createChooser(intent,"Pick Image"), imageMessageRequestCode)
        }

    }

    private fun sendMessageToUser(senderID: String?, receiverID: String, message: String) {

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


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null) {
            if (requestCode == imageMessageRequestCode && resultCode == RESULT_OK && data.data != null){
                imageUri = data.data
                uploadImageToDatabase(this)
            }
        }
    }

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

                        val chat: Chat = Chat(senderID, "sent you an image.", userIDVisited, false, url, messageID)
                        if (messageID != null && senderID != null) {
                            reference.child("chats").child(messageID).setValue(chat).addOnCompleteListener { task ->
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
}
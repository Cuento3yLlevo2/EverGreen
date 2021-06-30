package com.mahi.evergreen.view.ui.fragments

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
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
import com.mahi.evergreen.R
import com.mahi.evergreen.databinding.FragmentMessageChatBinding
import com.mahi.evergreen.model.Chat
import com.mahi.evergreen.model.ChatMessage
import com.mahi.evergreen.model.User
import com.mahi.evergreen.network.Callback
import com.mahi.evergreen.network.DatabaseService
import com.mahi.evergreen.view.adapter.ChatMessagesAdapter
import com.mahi.evergreen.view.adapter.ChatMessagesListener
import com.mahi.evergreen.viewmodel.ChatMessagesViewModel
import com.squareup.picasso.Picasso
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MessageChatFragment : BaseDialogFragment(), ChatMessagesListener {

    private lateinit var chatMessagesAdapter: ChatMessagesAdapter
    private lateinit var viewModel: ChatMessagesViewModel
    private var userIDVisited: String = ""
    private var chatIDFromChatList: String = ""
    private var type = 0
    private var firebaseUser: FirebaseUser? = null
    private var databaseService = DatabaseService()
    private val reference = databaseService.database.reference
    private var imageUri: Uri? = null
    private var storageRef: StorageReference? = null
    private lateinit var currentPhotoPath: String
    override var bottomNavigationViewVisibility: Int = View.GONE

    private var _binding: FragmentMessageChatBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMessageChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.toolbarMessageChat.navigationIcon = ContextCompat.getDrawable(view.context, R.drawable.arrow_backwards)
        binding.toolbarMessageChat.setNavigationOnClickListener {
            findNavController().popBackStack()
            dismiss()
        }

        // retrieve from Firebase.auth the current user UID
        firebaseUser = Firebase.auth.currentUser
        // connect viewModel with View
        viewModel = ViewModelProvider(this).get(ChatMessagesViewModel::class.java)

        // type 1 = chatActivity started from chatList,
        // type 2 = chatActivity started from userList,
        // type 3 = chatActivity started from Post
        type = arguments?.getInt("type", 0)!!
        when (type) {
            1 -> {
                // retrieve from previous activity the chatID
                chatIDFromChatList = arguments?.getString("clicked_chat_id").toString()
                // update directly viewModel ChatID
                viewModel.chatID = chatIDFromChatList
                firebaseUser?.let { displayMessagesWithChatID(chatIDFromChatList, view.context, it.uid) }
                displayChatData(viewModel.chatID)
            }
            2 -> {
                // retrieve from previous activity the Visited User UID
                /*
                userIDVisited = intent.getStringExtra("visit_user_id").toString()
                firebaseUser?.let { displayMessagesWithVisitedUserID(userIDVisited, view.context, it.uid) }
                */
                Log.w("Activity creation error", "Error: type of activity not currentlyused.")
                dismiss()
            }
            3 -> {
                // retrieve post data from previous activity
                val postImageURL = arguments?.getString("postImageURL").toString()
                val postTitle = arguments?.getString("postTitle").toString()
                val postID = arguments?.getString("postID").toString()
                // retrieve from previous activity the Visited User UID
                userIDVisited = arguments?.getString("visit_user_id").toString()
                firebaseUser?.let { displayMessagesWithVisitedUserID(userIDVisited, view.context, it.uid, postID, postTitle, postImageURL, this) }

            }
            else -> {
                Log.w("Activity creation error", "Error: type of activity not declare.")
                dismiss()
            }
        }


        // When Current User presses the SendMessageBtn the message will be sent to visited user's phone
        binding.ivSendMessageBtn.setOnClickListener {
            val message = binding.etTextMessage.text.toString()
            sendMessageToUser(firebaseUser?.uid, message)
        }

        // When Current User presses the AttachImageBtn will appear an activity to ask for the image
        binding.ivAttachImageFileBtn.setOnClickListener {
            if(hasCameraSupport()){
                loadImageWithCameraOrGallery()
            }
            // createImageMessageIntent()
        }

        observeViewModel()
    }

    /**
     * Asks the user if prefers to upload a image from the gallery or take a picture
     */
    private fun loadImageWithCameraOrGallery() {
        val options = arrayOf<CharSequence>("Tomar una foto", "Seleccionar de galería")
        val builder : android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(requireContext())
        builder.setTitle("¿Qué quieres hacer?")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> {
                    // make sure the permissions needed are allowed
                    checkForPermissions(android.Manifest.permission.CAMERA, "Camera",
                        UpcyclingCreationFragment.REQUEST_IMAGE_CAPTURE
                    )
                }
                1 -> {
                    // make sure the permissions needed are allowed
                    checkForPermissions(android.Manifest.permission.READ_EXTERNAL_STORAGE, "Gallery",
                        UpcyclingCreationFragment.REQUEST_GALLERY_ACCESS
                    )
                }
            }
        }
        builder.show()
    }

    /**
     * Shows dialog to explain to user why a given permission access is required for the app to work
     */
    private fun showDialog(permission: String, name: String, requestCode: Int){
        val builder = context?.let { AlertDialog.Builder(it) }
        builder?.apply {
            setMessage("Se requiere permiso para acceder a la $name para usar esta aplicación")
            setTitle("Permiso requerido")
            setPositiveButton("OK") { _, _ ->
                activity?.let { ActivityCompat.requestPermissions(it, arrayOf(permission), requestCode) }
            }
        }
        val dialog = builder?.create()
        dialog?.show()
    }

    /**
     * Handles the needed permissions for the camera or gallery and then calls intent for given activity
     */
    private fun checkForPermissions(permission: String, name: String, requestCode: Int){
        when {
            context?.let { ContextCompat.checkSelfPermission(it, permission) } == PackageManager.PERMISSION_GRANTED -> {
                // If the Permissions have been granted continue
                if (requestCode == UpcyclingCreationFragment.REQUEST_IMAGE_CAPTURE){
                    dispatchTakePictureIntent()
                } else if (requestCode == UpcyclingCreationFragment.REQUEST_GALLERY_ACCESS){
                    dispatchGalleryPictureIntent()
                }

            }
            // If the Permissions are not granted Request Permission
            shouldShowRequestPermissionRationale(permission) -> showDialog(permission, name, requestCode)

            else -> activity?.let { ActivityCompat.requestPermissions(it, arrayOf(permission), requestCode) }
        }
    }

    /**
     * Starts the process to load a image from the gallery or camera depending on the customer's selection
     */
    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            context?.let { contextNotNull ->
                takePictureIntent.resolveActivity(contextNotNull.packageManager)?.also {
                    // Create the File where the photo should go
                    val photoFile: File? = try {
                        createImageFile()
                    } catch (ex: IOException) {
                        // Error occurred while creating the File
                        Log.e("TakePictureIntentError", "ERROR ==> $ex")
                        null
                    }
                    // Continue only if the File was successfully created
                    photoFile?.also {
                        imageUri = FileProvider.getUriForFile(
                            requireContext(),
                            "com.mahi.evergreen.fileprovider",
                            it
                        )
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                        startActivityForResult(takePictureIntent,
                            UpcyclingCreationFragment.REQUEST_IMAGE_CAPTURE
                        )
                    }
                }
            }
        }
    }

    /**
     * Creates a temporal file from the image took from camera
     */
    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir: File? = context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    /**
     * Takes care of the gallery intent to let the user pick a picture from the device internal storage
     */
    private fun dispatchGalleryPictureIntent() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, UpcyclingCreationFragment.REQUEST_GALLERY_ACCESS)
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            UpcyclingCreationFragment.REQUEST_IMAGE_CAPTURE -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Permission is granted. Continue the action or workflow
                    // If the Permissions have been granted continue
                    dispatchTakePictureIntent()
                } else {
                    // Explain to the user that the feature is unavailable because
                    // the features requires a permission that the user has denied.
                    // At the same time, respect the user's decision. Don't link to
                    // system settings in an effort to convince the user to change
                    // their decision.
                    Toast.makeText(context, "Debes permitir el acceso a la cámara para tomar fotos", Toast.LENGTH_LONG).show()
                }
                return
            }
            UpcyclingCreationFragment.REQUEST_GALLERY_ACCESS -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Permission is granted. Continue the action or workflow
                    // If the Permissions have been granted continue
                    dispatchGalleryPictureIntent()
                } else {
                    // Explain to the user that the feature is unavailable because
                    // the features requires a permission that the user has denied.
                    // At the same time, respect the user's decision. Don't link to
                    // system settings in an effort to convince the user to change
                    // their decision.
                    Toast.makeText(context, "Debes permitir el acceso los archivos para elegir una imagen", Toast.LENGTH_LONG).show()
                }
                return
            }

            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }

    /**
     * Verifies if the given device has camera available
     */
    /**
     * Verifies if the given device has camera available
     */
    private fun hasCameraSupport(): Boolean {
        var hasSupport = false
        if(context?.packageManager?.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY) == true){
            hasSupport = true
        }
        return hasSupport
    }

    /**
     * When user start Chat activity directly from a Post,
     * this method Populates the data from the database given the userVisited id and other data
     */
    private fun displayMessagesWithVisitedUserID(
        userIDVisited: String,
        context: Context,
        currentUserID: String,
        postID: String,
        postTitle: String,
        postImageURL: String,
        messageChatActivity: MessageChatFragment
    ) {
        val visitedUserDbRef = reference.child("users").child(userIDVisited)
        visitedUserDbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                val visitedUserData: User? = snapshot.getValue(User::class.java)
                if (visitedUserData?.profile?.profileImage != null) {
                    Picasso.get().load(visitedUserData.profile?.profileImage).into(binding.ivVisitedProfileImage)

                    viewModel.getChatIDAndRefreshChatMessages(currentUserID, userIDVisited, postID, postTitle, postImageURL, messageChatActivity)


                    chatMessagesAdapter = ChatMessagesAdapter(this@MessageChatFragment, visitedUserData.profile?.profileImage!!, context)

                    binding.rvMessageChats.apply {
                        val linearLayoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
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
    }

    /**
     * given a ChatID retrieve chat data from the database to populate the view
     */
    fun displayChatData(chatID: String) {
        val chatRef = reference.child("chats").child(chatID)
        chatRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                val chatData: Chat? = snapshot.getValue(Chat::class.java)

                if (chatData != null && _binding != null) {
                    context?.let {
                        Glide.with(it) // contexto
                            .load(chatData.postImageURL) // donde esta la url de la imagen
                            .placeholder(R.drawable.post_default_image) // placeholder
                            .into(binding.ivVisitedPostImage)
                    } // donde la vamos a colocar

                    chatData.postTitle?.let { binding.tvVisitedMessageTitle.text = it }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w("Data reading failure", "Error getting documents.", error.toException())
            }
        })
    }

    /**
     * Using ChatID as input search for list of messages previously made
     */
    private fun displayMessagesWithChatID(chatIDFromChatList: String, context: Context, currentUserID: String) {
        databaseService.getUserIDVisited(chatIDFromChatList, currentUserID, object:
            Callback<String> {
            override fun onSuccess(result: String?) {
                if (result != null) {
                    userIDVisited = result
                    val visitedUserDbRef = reference.child("users").child(userIDVisited)
                    visitedUserDbRef.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {

                            val visitedUserData: User? = snapshot.getValue(User::class.java)
                            if (visitedUserData?.profile?.profileImage != null) {
                                Picasso.get().load(visitedUserData.profile?.profileImage).into(binding.ivVisitedProfileImage)

                                viewModel.refreshChatMessages(chatIDFromChatList, firebaseUser?.uid)

                                chatMessagesAdapter = ChatMessagesAdapter(
                                    this@MessageChatFragment,
                                    visitedUserData.profile?.profileImage!!,
                                    context
                                )

                                binding.rvMessageChats.apply {
                                    val linearLayoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
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
                }
            }

            override fun onFailure(exception: Exception) {
                // on failure
            }
        })
    }

    /***
     * Sends texts message from current user to visited user and updates the database to get results on realtime
     */
    private fun sendMessageToUser(senderID: String?, message: String) {
        if (message.isBlank()){
            Toast.makeText(requireContext(), "El campo de texto esta vació!", Toast.LENGTH_LONG).show()
        } else {
            viewModel.writeMessage(senderID, viewModel.chatID, message, "")
        }
        binding.etTextMessage.setText("")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == UpcyclingCreationFragment.REQUEST_IMAGE_CAPTURE && resultCode == AppCompatActivity.RESULT_OK) {

            uploadImageToDatabase()

        } else if (requestCode == UpcyclingCreationFragment.REQUEST_GALLERY_ACCESS && resultCode == AppCompatActivity.RESULT_OK){
            if (data != null) {
                imageUri = data.data
                uploadImageToDatabase()
            }
        }

    }


    /**
     * Retrieves the Attached image from Activity collecting the Image uri and saving URL on database
     */
    private fun uploadImageToDatabase() {
        val progressBar = databaseService.setProgressDialogWhenDataLoading(requireContext(), "La imagen se está enviando...")
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

    override fun onMessageItemClicked(chatMessageItem: ChatMessage, position: Int) {
        // what happen if a Message is clicked, we cloud ask the user to delete the message if he wants

        // If chat.url is Not NullOrEmpty && chat.message.equals("sent you an image.") We know that massage clicked an Image
        if (!chatMessageItem.url.isNullOrEmpty() && chatMessageItem.message.equals("sent you an image.")){
            Toast.makeText(requireContext(), "Próximamente", Toast.LENGTH_SHORT).show()
        }
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

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    override fun onDestroyView() {
        bottomNavigationViewVisibility = View.VISIBLE
        _binding = null
        super.onDestroyView()
    }

}
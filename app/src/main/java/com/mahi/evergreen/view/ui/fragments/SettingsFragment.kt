package com.mahi.evergreen.view.ui.fragments

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import com.mahi.evergreen.databinding.FragmentSettingsBinding
import com.mahi.evergreen.model.User
import com.mahi.evergreen.network.FirebaseDatabaseService
import com.mahi.evergreen.viewmodel.UsersViewModel
import com.squareup.picasso.Picasso


class SettingsFragment : Fragment() {

    private lateinit var viewModel: UsersViewModel
    private var firebaseDatabaseService = FirebaseDatabaseService()
    private lateinit var dataBaseUsersReference: DatabaseReference
    private lateinit var userUid: String

    private val pickImageRequestCode = 438
    private var imageUri: Uri? = null
    private var storageRef: StorageReference? = null
    private var imageChecker: Int? = null

    private var _binding: FragmentSettingsBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(UsersViewModel::class.java)

        dataBaseUsersReference = firebaseDatabaseService.getDatabaseUsersReference()
        userUid = viewModel.getCurrentUserUID().toString()
        storageRef = Firebase.storage.reference.child("Users Images")

        dataBaseUsersReference.child(userUid).addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val user: User? = snapshot.getValue(User::class.java)
                if (context!=null && user != null){
                    binding.tvSettingsUsername.text = user.username
                    Picasso.get().load(user.profileImage).into(binding.ivSettingsProfileImage)
                    Glide.with(this@SettingsFragment).load(user.coverImage).into(binding.ivSettingsCoverImage)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w("Data reading failure", "Error getting documents.", error.toException())
            }
        })

        binding.ivSettingsProfileImage.setOnClickListener {
            imageChecker = PROFILE_IMAGE
            pickImage()
        }

        binding.ivSettingsCoverImage.setOnClickListener {
            imageChecker = COVER_IMAGE
            pickImage()
        }

    }

    private fun pickImage() {
        val intent = Intent()
        intent.type = "image/"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, pickImageRequestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == pickImageRequestCode && resultCode == Activity.RESULT_OK && data!!.data != null){
            imageUri = data.data
            context?.let { uploadImageToDatabase(it) }
        }
    }

    private fun uploadImageToDatabase(context: Context) {
        val progressBar = firebaseDatabaseService.setProgressDialogWhenDataLoading(context, "Cargando....")
        progressBar.show()

        if (imageUri!=null){
            val fileRef = storageRef?.child(System.currentTimeMillis().toString() + ".jpg")
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
                        if (imageChecker == COVER_IMAGE){
                            dataBaseUsersReference.child(userUid).updateChildren(hashMapOf<String, Any>("coverImage" to url))
                        } else if (imageChecker == PROFILE_IMAGE){
                            dataBaseUsersReference.child(userUid).updateChildren(hashMapOf<String, Any>("profileImage" to url))
                        }
                        progressBar.dismiss()
                    }

                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val COVER_IMAGE = 0
        private const val PROFILE_IMAGE = 1
    }

}
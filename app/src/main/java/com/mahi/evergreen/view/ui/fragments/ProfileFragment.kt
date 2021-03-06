package com.mahi.evergreen.view.ui.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import com.mahi.evergreen.R
import com.mahi.evergreen.databinding.FragmentProfileBinding
import com.mahi.evergreen.model.User
import com.mahi.evergreen.model.UserProfile
import com.mahi.evergreen.network.DatabaseService
import com.mahi.evergreen.network.POST_IDEA_TYPE
import com.mahi.evergreen.network.POST_SERVICE_TYPE
import com.squareup.picasso.Picasso

/**
 * Fragment class that Populates the view of the profile user data
 * It shows all relevant data of the user including profile image and username
 * It starts a Upcycling service/idea creation if the user decides to do so
 * It continues to the Upcycling service/idea created list if the user decides to do so
 * it shows settigns and support options
 */
@Suppress("DEPRECATION")
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var databaseService = DatabaseService()
    private lateinit var dataBaseUsersReference: DatabaseReference
    private var userUid: String? = null
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val pickImageRequestCode = 438
    private var imageUri: Uri? = null
    private var storageRef: StorageReference? = null
    private var currentUserData: User? = null

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dataBaseUsersReference = databaseService.getDatabaseUsersReference()

        userUid = auth.currentUser?.uid
        storageRef = Firebase.storage.reference.child("Users Images")

        userUid?.let {
            dataBaseUsersReference.child(it).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user: User? = snapshot.getValue(User::class.java)
                    if (context!=null && user != null && _binding != null){
                        currentUserData = user
                        val userProfile: UserProfile? = user.profile
                        if (userProfile!=null){
                            binding.tvProfileUsername.text = userProfile.username
                            Picasso.get().load(userProfile.profileImage).placeholder(R.drawable.user_default).into(binding.ivProfileImage)
                            binding.tvProfileSeedsPoints.text = userProfile.seedsPoints.toString()
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w("Data reading failure", "Error getting documents.", error.toException())
                }
            })
        }

        binding.ivProfileImage.setOnClickListener {
            pickImage()
        }

        binding.clProfileToPublicProfile.setOnClickListener {
            if (currentUserData != null){
                val userValues = currentUserData!!.toMap()
                val bundle = bundleOf("user" to userValues)
                findNavController().navigate(R.id.action_navProfileFragment_to_profilePublicDetailDialogFragment, bundle)
            }

        }

        binding.ivProfileToCreateUpcyclingIdeas.setOnClickListener {
            val bundle = bundleOf("isUpcyclingCreationAction" to true, "upcyclingType" to POST_IDEA_TYPE)
            findNavController().navigate(R.id.action_navProfileFragment_to_categoriesDetailDialogFragment, bundle)
        }

        binding.ivProfileToCreateUpcyclingServices.setOnClickListener {
            val bundle = bundleOf("isUpcyclingCreationAction" to true, "upcyclingType" to POST_SERVICE_TYPE)
            findNavController().navigate(R.id.action_navProfileFragment_to_categoriesDetailDialogFragment, bundle)
        }

        binding.clProfileToUpcyclingIdeas.setOnClickListener {
            findNavController().navigate(R.id.action_navProfileFragment_to_profileIdeasDetailDialogFragment)
        }

        binding.clProfileToUpcyclingServices.setOnClickListener {
            findNavController().navigate(R.id.action_navProfileFragment_to_profileServicesDetailDialogFragment)
        }

        binding.clProfileToProfileSettings.setOnClickListener {
            findNavController().navigate(R.id.action_navProfileFragment_to_profileSettingsDetailDialogFragment)
        }

        binding.clProfileToSupport.setOnClickListener {
            findNavController().navigate(R.id.action_navProfileFragment_to_profileSupportDetailDialogFragment)
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().navigate(R.id.navHomeFragment)
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
        val progressBar = databaseService.setProgressDialogWhenDataLoading(context, "Cargando....")
        progressBar.show()

        if (imageUri!=null){
            val fileRef = storageRef?.child(System.currentTimeMillis().toString() + ".jpg")
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
                        userUid?.let { dataBaseUsersReference.child(it).child("profile").updateChildren(hashMapOf<String, Any>("profileImage" to url)) }
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
}
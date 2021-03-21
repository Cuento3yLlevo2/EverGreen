package com.mahi.evergreen.view.ui.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.StorageReference
import com.mahi.evergreen.databinding.FragmentSettingsBinding
import com.mahi.evergreen.model.User
import com.mahi.evergreen.network.FirebaseDatabaseService
import com.mahi.evergreen.viewmodel.UsersViewModel
import com.squareup.picasso.Picasso


class SettingsFragment : Fragment() {

    private lateinit var viewModel: UsersViewModel
    private var firebaseDatabaseService = FirebaseDatabaseService()

    private val pickImageRequestCode = 438
    private var imageUri: Uri? = null
    private var storageRef: StorageReference? = null

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

        val dataBaseUsersReference = firebaseDatabaseService.getDatabaseUsersReference()
        val userUid = viewModel.getCurrentUserUID()
        if (userUid != null) {
            dataBaseUsersReference.child(userUid).addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user: User? = snapshot.getValue(User::class.java)
                    if (context!=null){
                        if (user != null) {
                            binding.tvSettingsUsername.text = user.username
                            Picasso.get().load(user.profileImage).into(binding.ivSettingsProfileImage)
                            Glide.with(this@SettingsFragment).load(user.coverImage).into(binding.ivSettingsCoverImage)
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.w("Data reading failure", "Error getting documents.", error.toException())
                }
            })
        }

        binding.ivSettingsProfileImage.setOnClickListener {
            pickImage()
        }

        binding.ivSettingsCoverImage.setOnClickListener {
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

        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
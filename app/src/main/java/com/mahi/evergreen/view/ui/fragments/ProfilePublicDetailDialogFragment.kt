package com.mahi.evergreen.view.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.mahi.evergreen.R
import com.mahi.evergreen.databinding.FragmentProfilePublicDetailDialogBinding
import com.mahi.evergreen.model.User
import com.squareup.picasso.Picasso

class ProfilePublicDetailDialogFragment : BaseDialogFragment() {

    override var bottomNavigationViewVisibility: Int = View.GONE

    private var _binding: FragmentProfilePublicDetailDialogBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var currentUserData: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfilePublicDetailDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbarPublicProfile.navigationIcon = ContextCompat.getDrawable(view.context, R.drawable.arrow_backwards)
        binding.toolbarPublicProfile.setNavigationOnClickListener {
            findNavController().popBackStack()
            dismiss()
        }

        val userMap = arguments?.getSerializable("user") as Map<*, *>
        currentUserData = User().getPostFromMap(userMap)

        binding.tvPublicProfileUsername.text = currentUserData!!.profile?.username ?: "Nombre de usuario"
        Picasso.get().load(currentUserData!!.profile?.profileImage).placeholder(R.drawable.user_default).into(binding.ivPublicProfileImage)
        binding.tvPublicProfileSeedsPoints.text = currentUserData!!.profile?.seedsPoints.toString()



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
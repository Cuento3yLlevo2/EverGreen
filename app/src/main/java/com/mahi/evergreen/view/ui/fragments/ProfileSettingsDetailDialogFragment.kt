package com.mahi.evergreen.view.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.mahi.evergreen.R
import com.mahi.evergreen.databinding.FragmentProfileSettingsDetailDialogBinding
import com.mahi.evergreen.view.ui.activities.WelcomeActivity


class ProfileSettingsDetailDialogFragment : BaseDialogFragment() {

    override var bottomNavigationViewVisibility: Int = View.GONE

    private var _binding: FragmentProfileSettingsDetailDialogBinding? = null
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
        _binding = FragmentProfileSettingsDetailDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        binding.toolbarSettings.navigationIcon = ContextCompat.getDrawable(view.context, R.drawable.arrow_backwards)
        binding.toolbarSettings.setNavigationOnClickListener {
            findNavController().popBackStack()
            dismiss()
        }
        binding.toolbarSettings.title = resources.getString(R.string.profile_settings_text)

        binding.tvSingOut.setOnClickListener {
            signOut()
        }
    }

    private fun signOut() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(context, WelcomeActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        setHasOptionsMenu(false)
        inflater
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
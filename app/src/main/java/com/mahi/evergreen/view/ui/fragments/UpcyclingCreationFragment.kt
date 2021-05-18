package com.mahi.evergreen.view.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.mahi.evergreen.R
import com.mahi.evergreen.databinding.FragmentUpcyclingCreationBinding
import com.mahi.evergreen.network.POST_IDEA_TYPE
import com.mahi.evergreen.network.POST_SERVICE_TYPE

class UpcyclingCreationFragment : BaseDialogFragment() {

    override var bottomNavigationViewVisibility: Int = View.GONE

    private var upcyclingType: Int? = 0
    private var categoryID: String? = null

    private var _binding: FragmentUpcyclingCreationBinding? = null
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
        _binding = FragmentUpcyclingCreationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbarUpcyclingCreation.navigationIcon = ContextCompat.getDrawable(view.context, R.drawable.arrow_backwards)
        binding.toolbarUpcyclingCreation.setNavigationOnClickListener {
            findNavController().popBackStack()
            dismiss()
        }

        upcyclingType = arguments?.getInt("upcyclingType")
        categoryID = arguments?.getString("categoryID")
        if (upcyclingType == POST_SERVICE_TYPE){
            binding.toolbarUpcyclingCreation.title = resources.getString(R.string.upcycling_service_creation_toolbar_text)
        } else if(upcyclingType == POST_IDEA_TYPE) {
            binding.toolbarUpcyclingCreation.title = resources.getString(R.string.upcycling_idea_creation_toolbar_text)
        }

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
package com.mahi.evergreen.view.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.mahi.evergreen.R
import com.mahi.evergreen.databinding.FragmentCategoriesDetailDialogBinding
import com.mahi.evergreen.model.UpcyclingCategory
import com.mahi.evergreen.view.adapter.UpcyclingCategoriesAdapter
import com.mahi.evergreen.view.adapter.UpcyclingCategoriesListener
import com.mahi.evergreen.viewmodel.UpcyclingCategoriesViewModel

class CategoriesDetailDialogFragment : DialogFragment(), UpcyclingCategoriesListener {

    private lateinit var upcyclingCategoriesAdapter: UpcyclingCategoriesAdapter
    private lateinit var viewModel: UpcyclingCategoriesViewModel
    private var isUpcyclingCreationAction: Boolean? = false
    private var upcyclingType: Int? = 0

    private var _binding: FragmentCategoriesDetailDialogBinding? = null
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
        _binding = FragmentCategoriesDetailDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbarCategories.navigationIcon = ContextCompat.getDrawable(view.context, R.drawable.arrow_backwards)
        binding.toolbarCategories.setNavigationOnClickListener {
            findNavController().popBackStack()
            dismiss()
        }

        isUpcyclingCreationAction = arguments?.getBoolean("isUpcyclingCreationAction")
        if (isUpcyclingCreationAction == true){
            upcyclingType = arguments?.getInt("upcyclingType")
            Toast.makeText(context, "upcyclingType = $upcyclingType", Toast.LENGTH_SHORT).show()
            binding.toolbarCategories.title = resources.getString(R.string.upcycling_creation_categories_toolbar_text)
        } else {
            binding.toolbarCategories.title = resources.getString(R.string.categories_toolbar_text)
        }

        viewModel = ViewModelProvider(this).get(UpcyclingCategoriesViewModel::class.java)

        viewModel.refreshUpcyclingCategoriesList()

        upcyclingCategoriesAdapter = UpcyclingCategoriesAdapter(isUpcyclingCreationAction, this)
        binding.rvCategories.apply {
            layoutManager = LinearLayoutManager(view.context, LinearLayoutManager.VERTICAL, false)
            adapter = upcyclingCategoriesAdapter
        }

        observeViewModel()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    override fun onUpcyclingCategoryItemClicked(
        upcyclingCategoryItem: UpcyclingCategory,
        position: Int
    ) {
        Toast.makeText(context, "Próximamente", Toast.LENGTH_SHORT).show()
    }

    override fun observeViewModel() {
        viewModel.upcyclingCategoriesList.observe(viewLifecycleOwner, { upcyclingCategories ->
            upcyclingCategoriesAdapter.updateData(upcyclingCategories)
        })

        viewModel.isLoading.observe(viewLifecycleOwner, {
            if(it != null)
                binding.rlBaseCategories.visibility = View.INVISIBLE
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
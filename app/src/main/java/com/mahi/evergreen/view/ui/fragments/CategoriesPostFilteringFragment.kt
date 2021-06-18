package com.mahi.evergreen.view.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.mahi.evergreen.R
import com.mahi.evergreen.databinding.FragmentCategoriesPostFilteringBinding
import com.mahi.evergreen.model.Post
import com.mahi.evergreen.model.UpcyclingCategory
import com.mahi.evergreen.network.ADD_FAV_POST
import com.mahi.evergreen.network.REMOVE_FAV_POST
import com.mahi.evergreen.view.adapter.PostAdapter
import com.mahi.evergreen.view.adapter.PostListener
import com.mahi.evergreen.viewmodel.PostViewModel

/**
 * Fragment class that Populates the view of the list of Post filtered by a category
 * Using a recyclerview
 */
class CategoriesPostFilteringFragment : BaseDialogFragment() , PostListener {

    override var bottomNavigationViewVisibility: Int = View.GONE

    private lateinit var postAdapter: PostAdapter
    private lateinit var viewModel: PostViewModel
    private lateinit var category: UpcyclingCategory
    private lateinit var categoryMap: Map<*, *>

    private var _binding: FragmentCategoriesPostFilteringBinding? = null
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
        _binding = FragmentCategoriesPostFilteringBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbarCategoriesPostsFiltering.navigationIcon = ContextCompat.getDrawable(view.context, R.drawable.arrow_backwards)
        binding.toolbarCategoriesPostsFiltering.setNavigationOnClickListener {
            // findNavController().popBackStack()
            findNavController().navigate(R.id.action_categoriesPostFilteringFragment_to_categoriesDetailDialogFragment)
        }

        categoryMap = arguments?.getSerializable("upcyclingCategory") as Map<*, *>
        category = UpcyclingCategory().getPostUpcyclingCategoryFromMap(categoryMap)

        binding.toolbarCategoriesPostsFiltering.title = category.name

        viewModel = ViewModelProvider(this).get(PostViewModel::class.java)

        viewModel.refreshProfilePostListByCategory("\"${category.id}\"")

        postAdapter = PostAdapter(this)
        binding.rvCategoriesPostsFiltering.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = postAdapter
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().navigate(R.id.action_categoriesPostFilteringFragment_to_categoriesDetailDialogFragment)
        }

        observeViewModel()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }


    override fun onPostItemClicked(postItem: Post, position: Int) {
        val postValues = postItem.toMap()
        val bundle = bundleOf("post" to postValues, "upcyclingCategory" to categoryMap, "isCategoryFiltering" to true)
        findNavController().navigate(R.id.action_categoriesPostFilteringFragment_to_postDetailDialogFragment, bundle)
    }

    override fun onPostFavCheckClicked(postItem: Post, position: Int) {
        viewModel.changeFavPostState(postItem.postId, REMOVE_FAV_POST)
    }

    override fun onPostFavUncheckClicked(postItem: Post, position: Int) {
        viewModel.changeFavPostState(postItem.postId, ADD_FAV_POST)
    }

    override fun observeViewModel() {
        viewModel.postList.observe(viewLifecycleOwner, { post ->
            postAdapter.updateData(post)
            if (post.isEmpty()) {
                binding.rlCategoriesPostsFilteringEmpty.visibility = View.VISIBLE
            } else {
                binding.rlCategoriesPostsFilteringEmpty.visibility = View.GONE
            }
        })

        viewModel.isLoading.observe(viewLifecycleOwner, {
            if(it != null)
                binding.rlCategoriesPostsFiltering.visibility = View.INVISIBLE
        })
    }

    override fun onDestroyView() {
        bottomNavigationViewVisibility = View.VISIBLE
        _binding = null
        super.onDestroyView()
    }

}
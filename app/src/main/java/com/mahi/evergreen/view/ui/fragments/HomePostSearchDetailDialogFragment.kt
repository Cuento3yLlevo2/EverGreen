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
import com.mahi.evergreen.databinding.FragmentHomePostSearchDetailDialogBinding
import com.mahi.evergreen.model.Post
import com.mahi.evergreen.network.ADD_FAV_POST
import com.mahi.evergreen.network.REMOVE_FAV_POST
import com.mahi.evergreen.view.adapter.PostAdapter
import com.mahi.evergreen.view.adapter.PostListener
import com.mahi.evergreen.viewmodel.PostViewModel


class HomePostSearchDetailDialogFragment : BaseDialogFragment() , PostListener {

    override var bottomNavigationViewVisibility: Int = View.VISIBLE

    private lateinit var postAdapter: PostAdapter
    private lateinit var viewModel: PostViewModel
    private var keyword: String? = ""

    private var _binding: FragmentHomePostSearchDetailDialogBinding? = null
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
        _binding = FragmentHomePostSearchDetailDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbarHomePostsSearch.navigationIcon = ContextCompat.getDrawable(view.context, R.drawable.arrow_backwards)
        binding.toolbarHomePostsSearch.setNavigationOnClickListener {
            findNavController().navigate(R.id.navHomeFragment)
        }

        keyword = arguments?.getString("keyword")

        binding.toolbarHomePostsSearch.title = keyword

        viewModel = ViewModelProvider(this).get(PostViewModel::class.java)

        if (keyword != null) {
            viewModel.searchForPosts(keyword!!)
        }

        postAdapter = PostAdapter(this)
        binding.rvHomePostsSearch.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = postAdapter
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().navigate(R.id.navHomeFragment)
        }

        observeViewModel()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }


    override fun onPostItemClicked(postItem: Post, position: Int) {
        val postValues = postItem.toMap()
        val bundle = bundleOf("post" to postValues, "keyword" to keyword, "isCategoryFiltering" to false, "isSearchFiltering" to true)
        findNavController().navigate(R.id.action_homePostSearchDetailDialogFragment_to_postDetailDialogFragment, bundle)
    }

    override fun onPostFavCheckClicked(postItem: Post, position: Int) {
        viewModel.changeFavPostState(postItem.postId, REMOVE_FAV_POST)
        val bundle = bundleOf("keyword" to keyword)
        findNavController().navigate(R.id.homePostSearchDetailDialogFragment, bundle)
    }

    override fun onPostFavUncheckClicked(postItem: Post, position: Int) {
        viewModel.changeFavPostState(postItem.postId, ADD_FAV_POST)
        val bundle = bundleOf("keyword" to keyword)
        findNavController().navigate(R.id.homePostSearchDetailDialogFragment, bundle)
    }

    override fun observeViewModel() {
        viewModel.postList.observe(viewLifecycleOwner, { post ->
            postAdapter.updateData(post)
            if (post.isEmpty()) {
                binding.rlHomePostsSearchEmpty.visibility = View.VISIBLE
            } else {
                binding.rlHomePostsSearchEmpty.visibility = View.GONE
            }
        })

        viewModel.isLoading.observe(viewLifecycleOwner, {
            if(it != null)
                binding.rlHomePostsSearch.visibility = View.INVISIBLE
        })
    }

    override fun onDestroyView() {
        bottomNavigationViewVisibility = View.VISIBLE
        _binding = null
        super.onDestroyView()
    }

}
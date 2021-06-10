package com.mahi.evergreen.view.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.mahi.evergreen.R
import com.mahi.evergreen.databinding.FragmentProfileIdeasDetailDialogBinding
import com.mahi.evergreen.model.Post
import com.mahi.evergreen.network.ADD_FAV_POST
import com.mahi.evergreen.network.POST_IDEA_TYPE
import com.mahi.evergreen.network.REMOVE_FAV_POST
import com.mahi.evergreen.view.adapter.PostAdapter
import com.mahi.evergreen.view.adapter.PostListener
import com.mahi.evergreen.viewmodel.PostViewModel


class ProfileIdeasDetailDialogFragment : BaseDialogFragment() , PostListener {

    override var bottomNavigationViewVisibility: Int = View.GONE

    private lateinit var postAdapter: PostAdapter
    private lateinit var viewModel: PostViewModel

    private var _binding: FragmentProfileIdeasDetailDialogBinding? = null
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
        _binding = FragmentProfileIdeasDetailDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbarProfileIdeasPosts.navigationIcon = ContextCompat.getDrawable(view.context, R.drawable.arrow_backwards)
        binding.toolbarProfileIdeasPosts.setNavigationOnClickListener {
            findNavController().popBackStack()
            dismiss()
        }
        binding.toolbarProfileIdeasPosts.title = resources.getString(R.string.profile_upcycling_idea_text)

        viewModel = ViewModelProvider(this).get(PostViewModel::class.java)

        viewModel.refreshProfilePostListByType(POST_IDEA_TYPE)

        postAdapter = PostAdapter(this)
        binding.rvProfileIdeasPosts.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = postAdapter
        }

        observeViewModel()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    override fun onPostItemClicked(postItem: Post, position: Int) {
        val postValues = postItem.toMap()
        val bundle = bundleOf("post" to postValues, "isCategoryFiltering" to false)
        findNavController().navigate(R.id.action_profileIdeasDetailDialogFragment_to_postDetailDialogFragment, bundle)
    }

    override fun onPostFavCheckClicked(postItem: Post, position: Int) {
        viewModel.changeFavPostState(postItem.postId, REMOVE_FAV_POST)
        viewModel.refreshProfilePostListByType(POST_IDEA_TYPE)
    }

    override fun onPostFavUncheckClicked(postItem: Post, position: Int) {
        viewModel.changeFavPostState(postItem.postId, ADD_FAV_POST)
        viewModel.refreshProfilePostListByType(POST_IDEA_TYPE)
    }

    override fun observeViewModel() {
        viewModel.postList.observe(viewLifecycleOwner, { post ->
            postAdapter.updateData(post)
            if (post.isEmpty()) {
                binding.rlIdeasListEmpty.visibility = View.VISIBLE
            } else {
                binding.rlIdeasListEmpty.visibility = View.GONE
            }
        })

        viewModel.isLoading.observe(viewLifecycleOwner, {
            if(it != null)
                binding.rlProfileIdeasPosts.visibility = View.INVISIBLE
        })
    }

    override fun onDestroyView() {
        bottomNavigationViewVisibility = View.VISIBLE
        _binding = null
        super.onDestroyView()
    }
}
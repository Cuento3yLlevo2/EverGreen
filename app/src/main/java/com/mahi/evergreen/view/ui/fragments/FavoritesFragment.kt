package com.mahi.evergreen.view.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.mahi.evergreen.R
import com.mahi.evergreen.databinding.FragmentFavoritesBinding
import com.mahi.evergreen.model.Post
import com.mahi.evergreen.network.ADD_FAV_POST
import com.mahi.evergreen.network.REMOVE_FAV_POST
import com.mahi.evergreen.view.adapter.PostAdapter
import com.mahi.evergreen.view.adapter.PostListener
import com.mahi.evergreen.viewmodel.PostViewModel

/**
 * Fragment class that Populates the view of the list of favorite post for a user
 * Using a recyclerview
 */
class FavoritesFragment : Fragment() , PostListener {

    private lateinit var postAdapter: PostAdapter
    private lateinit var viewModel: PostViewModel

    private var _binding: FragmentFavoritesBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(PostViewModel::class.java)

        viewModel.refreshFavoritePostList()

        postAdapter = PostAdapter(this)
        binding.rvFavoritePosts.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = postAdapter
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().navigate(R.id.navHomeFragment)
        }


        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshFavoritePostList()
        observeViewModel()
    }


    override fun onPostItemClicked(postItem: Post, position: Int) {
        val postValues = postItem.toMap()
        val bundle = bundleOf("post" to postValues)
        findNavController().navigate(R.id.action_navFavoritesFragment_to_postDetailDialogFragment, bundle)
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
                binding.rlFavoritesListEmpty.visibility = View.VISIBLE
            } else {
                binding.rlFavoritesListEmpty.visibility = View.GONE
            }
        })

        viewModel.isLoading.observe(viewLifecycleOwner, {
            if(it != null)
                binding.rlBaseFavoritesPost.visibility = View.INVISIBLE

        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
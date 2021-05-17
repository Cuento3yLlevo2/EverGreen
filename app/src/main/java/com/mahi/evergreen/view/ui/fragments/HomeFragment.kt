package com.mahi.evergreen.view.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.mahi.evergreen.R
import com.mahi.evergreen.databinding.FragmentHomeBinding
import com.mahi.evergreen.model.Post
import com.mahi.evergreen.network.POST_IDEA_TYPE
import com.mahi.evergreen.view.adapter.PostAdapter
import com.mahi.evergreen.view.adapter.PostListener
import com.mahi.evergreen.viewmodel.PostViewModel


class HomeFragment : Fragment(), PostListener {

    private lateinit var postAdapter: PostAdapter
    private lateinit var viewModel: PostViewModel


    private var _binding: FragmentHomeBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(PostViewModel::class.java)

        viewModel.refreshPostList()

        postAdapter = PostAdapter(this)
        binding.rvHomePosts.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = postAdapter
        }

        binding.ivHomeToCategories.setOnClickListener {
            val bundle = bundleOf("isUpcyclingCreationAction" to false)
            findNavController().navigate(R.id.action_navHomeFragment_to_categoriesDetailDialogFragment)
        }

        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshPostList()
        observeViewModel()
    }


    override fun onPostItemClicked(postItem: Post, position: Int) {
        val postValues = postItem.toMap()
        val bundle = bundleOf("post" to postValues)
        findNavController().navigate(R.id.action_navHomeFragment_to_postDetailDialogFragment, bundle)
    }

    override fun observeViewModel() {
        viewModel.postList.observe(viewLifecycleOwner, { post ->
            postAdapter.updateData(post)
        })

        viewModel.isLoading.observe(viewLifecycleOwner, {
            if(it != null)
                binding.rlBaseHomePost.visibility = View.INVISIBLE
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
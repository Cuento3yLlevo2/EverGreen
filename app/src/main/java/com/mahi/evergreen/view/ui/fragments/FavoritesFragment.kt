package com.mahi.evergreen.view.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.mahi.evergreen.R
import com.mahi.evergreen.databinding.FragmentFavoritesBinding
import com.mahi.evergreen.databinding.FragmentHomeBinding
import com.mahi.evergreen.model.Post
import com.mahi.evergreen.view.adapter.PostAdapter
import com.mahi.evergreen.view.adapter.PostListener
import com.mahi.evergreen.viewmodel.PostViewModel

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
    ): View? {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
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


        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshFavoritePostList()
        observeViewModel()
    }


    override fun onPostItemClicked(postItem: Post, position: Int) {
        // what happens when clicked
    }

    override fun observeViewModel() {
        viewModel.postList.observe(viewLifecycleOwner, Observer { post ->
            postAdapter.updateData(post)
        })

        viewModel.isLoading.observe(viewLifecycleOwner, Observer {
            if(it != null)
                binding.rlBaseFavoritesPost.visibility = View.INVISIBLE
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
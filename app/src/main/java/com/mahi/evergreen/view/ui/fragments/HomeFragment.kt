package com.mahi.evergreen.view.ui.fragments

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.gms.ads.*
import com.google.android.gms.ads.nativead.NativeAd
import com.mahi.evergreen.databinding.FragmentHomeBinding
import com.mahi.evergreen.model.*
import com.mahi.evergreen.network.ADD_FAV_POST
import com.mahi.evergreen.network.REMOVE_FAV_POST
import com.mahi.evergreen.view.adapter.PostAdapter
import com.mahi.evergreen.view.adapter.PostListener
import com.mahi.evergreen.viewmodel.PostViewModel

/**
 * Fragment class that Populates the view of the list of all the post currently created
 * Using a recyclerview
 * This fragment also populates a list of AdMod Ads
 */
class HomeFragment : Fragment(), PostListener {

    private lateinit var postAdapter: PostAdapter
    private lateinit var viewModel: PostViewModel
    private lateinit var admodItemList: ArrayList<NativeAd>
    private lateinit var recyclerViewList: ArrayList<Any>
    private lateinit var adLoader: AdLoader
    private var addsAlreadyLoad = false
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

        if (_binding != null){
            viewModel = ViewModelProvider(this).get(PostViewModel::class.java)

            viewModel.refreshPostList()

            postAdapter = PostAdapter(this)
            binding.rvHomePosts.apply {
                val gridLayoutManager = GridLayoutManager(context, 2)
                layoutManager = gridLayoutManager
                adapter = postAdapter
            }

            binding.ivHomeToCategories.setOnClickListener {
                val bundle = bundleOf("isUpcyclingCreationAction" to false)
                findNavController().navigate(com.mahi.evergreen.R.id.action_navHomeFragment_to_categoriesDetailDialogFragment, bundle)
            }

            binding.ivSearchPostBtn.setOnClickListener {
                val keyword = binding.etSearchPost.text.toString()
                if (keyword.isNotEmpty()){
                    val bundle = bundleOf("keyword" to keyword)
                    findNavController().navigate(com.mahi.evergreen.R.id.action_navHomeFragment_to_homePostSearchDetailDialogFragment, bundle)
                }
            }

            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
                requireActivity().finish()
            }

            recyclerViewList = ArrayList()
            admodItemList = ArrayList()

            observeViewModel()
        }



    }

    /**
     * This method loads AdMod Native ads following the documentation on https://developers.google.com/admob/android/native/start
     */
    private fun createNativeAd() {
        val admodItem = AdmodItem()
        if (context != null){

            adLoader = AdLoader.Builder(context ?: requireContext(), "ca-app-pub-3940256099942544/2247696110")
                .forNativeAd { NativeAd ->

                    if (activity?.isDestroyed == true) {
                        NativeAd.destroy()
                        return@forNativeAd
                    }

                    admodItemList.add(NativeAd)

                    if (admodItem.adLoader?.isLoading == false){
                        postAdapter.updateAds(admodItemList)
                        binding.rlBaseHomePost.visibility = View.INVISIBLE
                    }

                }.withAdListener(object : AdListener() {
                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {

                        Log.e("onAdFailedToLoad", " domain: ${loadAdError.domain}, code: ${loadAdError.code}, message: ${loadAdError.message}")

                        object: CountDownTimer(10000, 1000) {
                            override fun onTick(millisUntilFinished: Long) {
                                Log.d("onAdFailedToLoad:Timer", "${millisUntilFinished/1000}")
                            }
                            override fun onFinish() {
                                Log.d("onAdFailedToLoad:Timer", "Reloading NativeAd")
                                createNativeAd()
                            }
                        }.start()
                    }
                })
                .build()

            adLoader.loadAds(AdRequest.Builder().build(), 2)
            admodItem.adLoader = adLoader
        }
    }

    override fun onPostItemClicked(postItem: Post, position: Int) {
        val postValues = postItem.toMap()
        val bundle = bundleOf("post" to postValues, "isCategoryFiltering" to false)
        findNavController().navigate(com.mahi.evergreen.R.id.action_navHomeFragment_to_postDetailDialogFragment, bundle)
    }

    override fun onPostFavCheckClicked(postItem: Post, position: Int) {
        viewModel.changeFavPostState(postItem.postId, REMOVE_FAV_POST)
        findNavController().navigate(com.mahi.evergreen.R.id.navHomeFragment)
    }

    override fun onPostFavUncheckClicked(postItem: Post, position: Int) {
        viewModel.changeFavPostState(postItem.postId, ADD_FAV_POST)
        findNavController().navigate(com.mahi.evergreen.R.id.navHomeFragment)
    }

    override fun observeViewModel() {
        viewModel.postList.observe(viewLifecycleOwner, { postList ->
            postAdapter.updateDataOnlyHomeFragment(postList)

            viewModel.isLoading.observe(viewLifecycleOwner, {
                if(it != null && _binding != null){
                    if(!addsAlreadyLoad && postList.size >= 6){
                        createNativeAd()
                        addsAlreadyLoad = true
                    } else {
                        binding.rlBaseHomePost.visibility = View.INVISIBLE
                    }
                }
            })
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
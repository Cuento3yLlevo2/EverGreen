package com.mahi.evergreen.view.ui.fragments

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.mahi.evergreen.R
import com.mahi.evergreen.databinding.FragmentPostDetailDialogBinding
import com.mahi.evergreen.model.Post
import com.mahi.evergreen.model.User
import com.mahi.evergreen.network.ADD_FAV_POST
import com.mahi.evergreen.network.DatabaseService
import com.mahi.evergreen.network.REMOVE_FAV_POST
import com.mahi.evergreen.view.adapter.PostAdapter
import com.mahi.evergreen.view.adapter.PostImagesViewPagerAdapter
import com.mahi.evergreen.view.ui.activities.MessageChatActivity
import com.mahi.evergreen.viewmodel.PostViewModel
import com.squareup.picasso.Picasso

/**
 * Fragment class that Populates the view of the post details of a given post
 * It shows all relevant data of the post including images
 * It starts a chat message activity if the user decides to do so
 */
class PostDetailDialogFragment : BaseDialogFragment(), DialogInterface.OnDismissListener {

    override var bottomNavigationViewVisibility: Int = View.GONE
    private lateinit var viewModel: PostViewModel
    private lateinit var categoryMap: Map<*, *>
    private var isCategoryFiltering: Boolean? = false
    private var isSearchFiltering: Boolean? = false
    private var keyword: String? = ""
    private var adapter: PostImagesViewPagerAdapter? = null
    var firebaseUser = Firebase.auth.currentUser
    private var databaseService = DatabaseService()
    private val reference = databaseService.database.reference

    private var _binding: FragmentPostDetailDialogBinding? = null
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
        _binding = FragmentPostDetailDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbarPostDetails.navigationIcon = ContextCompat.getDrawable(view.context, R.drawable.arrow_backwards)
        binding.toolbarPostDetails.setNavigationOnClickListener {
            dismiss()
        }

        isSearchFiltering = arguments?.getBoolean("isSearchFiltering", false)
        if(isSearchFiltering == true) {
            keyword = arguments?.getString("keyword")
        }

        isCategoryFiltering = arguments?.getBoolean("isCategoryFiltering", false)
        if(isCategoryFiltering == true) {
            categoryMap = arguments?.getSerializable("upcyclingCategory") as Map<*, *>
        }

        val postMap = arguments?.getSerializable("post") as Map<*, *>
        val post: Post = Post().getPostFromMap(postMap)
        binding.toolbarPostDetails.title = post.description



        viewModel = ViewModelProvider(this).get(PostViewModel::class.java)

        val arrayListImages = ArrayList<String>()
        for (image in post.images) {
            arrayListImages.add(image.value)
        }
        val arrayListImagesReversed = arrayListImages.reversed()
        arrayListImages.clear()
        for (image in arrayListImagesReversed) {
            arrayListImages.add(image)
        }

        adapter = PostImagesViewPagerAdapter(arrayListImages, false)
        binding.viewpager.adapter = adapter

        //Custom bind indicator
        binding.indicator.highlighterViewDelegate = {
            val highlighter = View(context)
            highlighter.layoutParams = FrameLayout.LayoutParams(26, 10)
            highlighter.setBackgroundColor(resources.getColor(R.color.black, resources.newTheme()))
            highlighter
        }
        binding.indicator.unselectedViewDelegate = {
            val unselected = View(context)
            unselected.layoutParams = LinearLayout.LayoutParams(26, 10)
            unselected.setBackgroundColor(resources.getColor(R.color.black, resources.newTheme()))
            unselected.alpha = 0.4f
            unselected
        }
        binding.viewpager.onIndicatorProgress = { selectingPosition, progress -> binding.indicator.onPageScrolled(selectingPosition, progress) }

        binding.indicator.updateIndicatorCounts(binding.viewpager.indicatorCount)

        binding.tvPostDetailTitle.text = post.title
        binding.tvPostDetailDesc.text = post.description

        when(post.type){
            PostAdapter.UPCYCLING_SERVICE -> {
                val postServiceTitle = "desde ${post.minPrice}€"
                binding.tvPostDetailUpcyclingService.text = postServiceTitle
                setFavoriteState(post.membersFollowingAsFavorite)

            }
            PostAdapter.UPCYCLING_IDEA -> {
                binding.tvPostDetailUpcyclingService.visibility = View.GONE
                binding.ivPostDetailUpcyclingIdea.visibility = View.VISIBLE
                setFavoriteState(post.membersFollowingAsFavorite)
            }
        }

        post.publisher?.let { displayPostPublisherData(it) }

        binding.ivPostDetailFavCheck.setOnClickListener {
            viewModel.changeFavPostState(post.postId, REMOVE_FAV_POST)
            binding.ivPostDetailFavUncheck.visibility = View.VISIBLE
            binding.ivPostDetailFavCheck.visibility = View.GONE
        }

        binding.ivPostDetailFavUncheck.setOnClickListener {
            viewModel.changeFavPostState(post.postId, ADD_FAV_POST)
            binding.ivPostDetailFavUncheck.visibility = View.GONE
            binding.ivPostDetailFavCheck.visibility = View.VISIBLE
        }

        if (post.publisher == firebaseUser?.uid) {
            binding.bPostDetailChatBtn.visibility = View.GONE
        }
        binding.bPostDetailChatBtn.setOnClickListener {
            val intent = Intent(context, MessageChatActivity::class.java)
            intent.putExtra("visit_user_id", post.publisher)
            intent.putExtra("postImageURL", post.coverImage)
            intent.putExtra("postTitle", post.description)
            intent.putExtra("postID", post.postId)
            intent.putExtra("type", 3)
            startActivity(intent)
        }
    }




    private fun displayPostPublisherData(postPublisherID: String) {
        val postPublisherData = reference.child("users").child(postPublisherID)
        postPublisherData.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                val userData: User? = snapshot.getValue(User::class.java)
                if (userData?.profile?.profileImage != null && _binding != null) {
                    Picasso.get()
                            .load(userData.profile?.profileImage)
                            .placeholder(R.drawable.user_default)
                            .into(binding.ivPostProfileDetailImage)
                    binding.tvPostProfileDetailUsername.text = userData.profile?.username
                    binding.tvPostProfileSeedsPoints.text = userData.profile?.seedsPoints.toString()
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w("Data reading failure", "Error getting documents.", error.toException())
            }
        })
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        when {
            isCategoryFiltering == true -> {
                val bundle = bundleOf("upcyclingCategory" to categoryMap)
                findNavController().navigate(R.id.categoriesPostFilteringFragment, bundle)
            }
            isSearchFiltering == true -> {
                val bundle = bundleOf("keyword" to keyword)
                findNavController().navigate(R.id.homePostSearchDetailDialogFragment, bundle)
            }
            else -> {
                findNavController().previousBackStackEntry?.destination?.id?.let {
                    findNavController().navigate(
                        it
                    )
                }
            }
        }
    }

    private fun setFavoriteState(membersFollowingAsFavorite: MutableMap<String, Boolean>
    ) {
        if (membersFollowingAsFavorite.containsKey(firebaseUser?.uid)){
            binding.ivPostDetailFavUncheck.visibility = View.GONE
            binding.ivPostDetailFavCheck.visibility = View.VISIBLE
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
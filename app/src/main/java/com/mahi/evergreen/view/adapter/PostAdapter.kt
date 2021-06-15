package com.mahi.evergreen.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.ads.nativead.NativeAd
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.mahi.evergreen.R
import com.mahi.evergreen.model.AdsTemplateView
import com.mahi.evergreen.model.Post

class PostAdapter(private val postListener: PostListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var listOfPostsAndAdmodItems = ArrayList<Any?>()
    var firebaseUser = Firebase.auth.currentUser


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == POST){

            PostViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_post, parent, false)
            )
        } else {

            AdmodViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_ads, parent, false)
            )
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if (getItemViewType(position) == SPONSORED_AD){
            val admodViewHolder : AdmodViewHolder = holder as AdmodViewHolder
            admodViewHolder.setNativeAd(listOfPostsAndAdmodItems[position] as NativeAd)

        } else {
            val postViewHolder : PostViewHolder = holder as PostViewHolder
            val post: Post? = listOfPostsAndAdmodItems[position] as Post?

            if (post != null) {
                postViewHolder.ivPostCoverImage?.let {
                    Glide.with(postViewHolder.itemView.context) // contexto
                        .load(post.coverImage) // donde esta la url de la imagen
                        .into(it)
                }
            } // donde la vamos a colocar

            postViewHolder.tvPostDesc?.text = post?.description

            if (post != null) {
                when(post.type){
                    UPCYCLING_SERVICE -> {
                        val postServiceTitle = "desde ${post.minPrice}€"
                        postViewHolder.tvPostUpcyclingService?.text = postServiceTitle
                        setFavoriteState(post, postViewHolder)
                    }
                    UPCYCLING_IDEA -> {
                        postViewHolder.tvPostUpcyclingService?.visibility = View.GONE
                        postViewHolder.ivPostUpcyclingIdea?.visibility = View.VISIBLE
                        setFavoriteState(post, postViewHolder)
                    }
                }
            }

            postViewHolder.itemView.setOnClickListener {
                if (post != null) {
                    postListener.onPostItemClicked(post, position)
                }
            }

            postViewHolder.ivPostFavCheck?.setOnClickListener {
                if (post != null) {
                    postListener.onPostFavCheckClicked(post, position)
                    // We need to notify to the adapter that all layout needs to be refresh
                    notifyItemRangeChanged(0, listOfPostsAndAdmodItems.size)
                }
            }

            postViewHolder.ivPostFavUncheck?.setOnClickListener {
                if (post != null) {
                    postListener.onPostFavUncheckClicked(post, position)
                    // We need to notify to the adapter that all layout needs to be refresh
                    notifyItemRangeChanged(0, listOfPostsAndAdmodItems.size)
                }

            }


        }



    }

    private fun setFavoriteState(post: Post, holder: PostViewHolder) {
        if(post.membersFollowingAsFavorite.containsKey(firebaseUser?.uid)){
            holder.ivPostFavUncheck?.visibility = View.GONE
            holder.ivPostFavCheck?.visibility = View.VISIBLE
        } else {
            holder.ivPostFavUncheck?.visibility = View.VISIBLE
            holder.ivPostFavCheck?.visibility = View.GONE
        }
    }

    override fun getItemCount() = listOfPostsAndAdmodItems.size

    fun updateData(data: List<Any>) {
        listOfPostsAndAdmodItems.clear()
        listOfPostsAndAdmodItems.addAll(data)
        notifyDataSetChanged()
    }

    fun updateDataOnlyHomeFragment(data: List<Any>) {
        listOfPostsAndAdmodItems.clear()
        listOfPostsAndAdmodItems.addAll(data)
    }

    fun updateAds(admodItemList: ArrayList<NativeAd>) {
        val finalList = ArrayList<Any?>()
        var addsInserted = 0
        val listofLists = listOfPostsAndAdmodItems.chunked(3)
        for (list in listofLists){
            finalList.addAll(list)
            if (addsInserted < admodItemList.size){
                finalList.add(admodItemList[addsInserted])
                addsInserted ++
            }
        }
        updateList(finalList)
    }

    private fun updateList(finalList: ArrayList<Any?>){
        listOfPostsAndAdmodItems.clear()
        listOfPostsAndAdmodItems.addAll(finalList)
        notifyDataSetChanged()
        notifyItemRangeChanged(0, listOfPostsAndAdmodItems.size)
    }

    override fun getItemViewType(position: Int): Int {
        return if (listOfPostsAndAdmodItems[position] is NativeAd) {
            SPONSORED_AD
        } else  {
            POST
        }
    }



    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivPostCoverImage : ImageView? = itemView.findViewById(R.id.ivPostCoverImage)
        val tvPostUpcyclingService : TextView? = itemView.findViewById(R.id.tvPostUpcyclingService)
        val ivPostUpcyclingIdea : ImageView? = itemView.findViewById(R.id.ivPostUpcyclingIdea)
        val ivPostFavCheck : ImageView? = itemView.findViewById(R.id.ivPostFavCheck)
        val ivPostFavUncheck : ImageView? = itemView.findViewById(R.id.ivPostFavUncheck)
        // val ivPostEdit : ImageView? = itemView.findViewById(R.id.ivPostEdit)
        val tvPostDesc : TextView? = itemView.findViewById(R.id.tvPostDesc)
        val clPostTitle : ConstraintLayout? = itemView.findViewById(R.id.clPostTitle)
    }

    class AdmodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val template: AdsTemplateView = itemView.findViewById(R.id.admodTemplate)

        fun setNativeAd(nativeAd: NativeAd) {
            template.setNativeAd(nativeAd)
        }
    }

    companion object {
        const val UPCYCLING_SERVICE = 1
        const val UPCYCLING_IDEA = 2
        const val SPONSORED_AD = 3
        const val POST = 4
    }
}
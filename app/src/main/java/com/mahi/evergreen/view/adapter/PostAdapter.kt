package com.mahi.evergreen.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.mahi.evergreen.R
import com.mahi.evergreen.model.Post

class PostAdapter(private val postListener: PostListener) : RecyclerView.Adapter<PostAdapter.ViewHolder>() {

    private var listOfPosts = ArrayList<Post?>()
    var firebaseUser = Firebase.auth.currentUser

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                    R.layout.item_post, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        // Reverse to get the post with new interactions at the top
        val listOfPostReversed = listOfPosts.reversed()
        val post: Post? = listOfPostReversed[position]


        if (post != null) {
            holder.ivPostCoverImage?.let {
                Glide.with(holder.itemView.context) // contexto
                    .load(post.coverImage) // donde esta la url de la imagen
                    .into(it)
            }
        } // donde la vamos a colocar

        holder.tvPostDesc?.text = post?.description

        if (post != null) {
            when(post.type){
                UPCYCLING_SERVICE -> {
                    val postServiceTitle = "desde ${post.minPrice}€"
                    holder.tvPostUpcyclingService?.text = postServiceTitle
                    setFavoriteState(post, holder)
                }
                UPCYCLING_IDEA -> {
                    holder.tvPostUpcyclingService?.visibility = View.GONE
                    holder.ivPostUpcyclingIdea?.visibility = View.VISIBLE
                    setFavoriteState(post, holder)
                }
                SPONSORED_AD -> {
                    holder.clPostTitle?.visibility = View.GONE
                    holder.vPostTitle?.visibility = View.GONE

                    // set cover image smaller
                    val layoutParams = holder.ivPostCoverImage?.layoutParams
                    if (layoutParams != null) {
                        layoutParams.height = 150
                    }
                    holder.ivPostCoverImage?.layoutParams = layoutParams

                    holder.clPostSponsoredTop?.visibility = View.VISIBLE
                    holder.vPostSponsoredTop?.visibility = View.VISIBLE
                    holder.bPostSponsoredLink?.visibility = View.VISIBLE
                    val sponsoredLinkText = "Descúbrelo"
                    holder.bPostSponsoredLink?.text = sponsoredLinkText
                    val sponsoredTitleText = "Motor 100% eléctrico"
                    holder.tvPostSponsoredTitle?.text = sponsoredTitleText
                }
            }
        }

        holder.itemView.setOnClickListener {
            if (post != null) {
                postListener.onPostItemClicked(post, position)
            }
        }

        holder.ivPostFavCheck?.setOnClickListener {
            if (post != null) {
                postListener.onPostFavCheckClicked(post, position)
                // We need to notify to the adapter that all layout needs to be refresh
                notifyItemRangeChanged(0, listOfPostReversed.size)
            }
        }

        holder.ivPostFavUncheck?.setOnClickListener {
            if (post != null) {
                postListener.onPostFavUncheckClicked(post, position)
                // We need to notify to the adapter that all layout needs to be refresh
                notifyItemRangeChanged(0, listOfPostReversed.size)
            }

        }
    }

    private fun setFavoriteState(post: Post, holder: ViewHolder) {
        if(post.membersFollowingAsFavorite.containsKey(firebaseUser?.uid)){
            holder.ivPostFavUncheck?.visibility = View.GONE
            holder.ivPostFavCheck?.visibility = View.VISIBLE
        } else {
            holder.ivPostFavUncheck?.visibility = View.VISIBLE
            holder.ivPostFavCheck?.visibility = View.GONE
        }
    }



    override fun getItemCount() = listOfPosts.size

    fun updateData(data: List<Post>) {
        listOfPosts.clear()
        listOfPosts.addAll(data)
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivPostCoverImage : ImageView? = itemView.findViewById(R.id.ivPostCoverImage)
        val tvPostUpcyclingService : TextView? = itemView.findViewById(R.id.tvPostUpcyclingService)
        val ivPostUpcyclingIdea : ImageView? = itemView.findViewById(R.id.ivPostUpcyclingIdea)
        val ivPostFavCheck : ImageView? = itemView.findViewById(R.id.ivPostFavCheck)
        val ivPostFavUncheck : ImageView? = itemView.findViewById(R.id.ivPostFavUncheck)
        // val ivPostEdit : ImageView? = itemView.findViewById(R.id.ivPostEdit)
        val tvPostDesc : TextView? = itemView.findViewById(R.id.tvPostDesc)

        val clPostTitle : ConstraintLayout? = itemView.findViewById(R.id.clPostTitle)
        val vPostTitle : View? = itemView.findViewById(R.id.vPostTitle)

        val vPostSponsoredTop : View? = itemView.findViewById(R.id.vPostSponsoredTop)
        val clPostSponsoredTop : ConstraintLayout? = itemView.findViewById(R.id.clPostSponsoredTop)
        val ivPostSponsoredImage : ImageView? = itemView.findViewById(R.id.ivPostSponsoredImage)
        val bPostSponsoredLink : Button? = itemView.findViewById(R.id.bPostSponsoredLink)
        val tvPostSponsoredTitle : TextView? = itemView.findViewById(R.id.tvPostSponsoredTitle)
    }

    companion object {
        const val UPCYCLING_SERVICE = 1
        const val UPCYCLING_IDEA = 2
        const val SPONSORED_AD = 3
    }
}
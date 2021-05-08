package com.mahi.evergreen.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.mahi.evergreen.R
import com.mahi.evergreen.model.Post
import com.mahi.evergreen.network.DatabaseService

class PostAdapter(val postListener: PostListener) : RecyclerView.Adapter<PostAdapter.ViewHolder>() {

    var listOfPosts = ArrayList<Post>()
    var firebaseUser = Firebase.auth.currentUser

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                    R.layout.item_post, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        // Reverse to get the post with new interactions at the top
        var listOfPostReversed = listOfPosts.reversed()
        val post = listOfPostReversed[position]

        Glide.with(holder.itemView.context) // contexto
                .load(post.coverImage) // donde esta la url de la imagen
                .placeholder(R.drawable.post_default_image) // placeholder
                .into(holder.ivPostCoverImage) // donde la vamos a colocar

        holder.tvPostDesc.text = post.description

        when(post.type){
            UPCYCLING_SERVICE -> {
                val postServiceTitle = "desde ${post.minPrice}€"
                holder.tvPostUpcyclingService.text = postServiceTitle
                setFavoriteState(post, holder)

            }
            UPCYCLING_IDEA -> {
                holder.tvPostUpcyclingService.visibility = View.GONE
                holder.ivPostUpcyclingIdea.visibility = View.VISIBLE
                setFavoriteState(post, holder)
            }
            SPONSORED_AD -> {
                holder.clPostTitle.visibility = View.GONE
                holder.vPostTitle.visibility = View.GONE

                // set cover image smaller
                val layoutParams = holder.ivPostCoverImage.layoutParams
                layoutParams.height = 150
                holder.ivPostCoverImage.layoutParams = layoutParams

                holder.clPostSponsoredTop.visibility = View.VISIBLE
                holder.vPostSponsoredTop.visibility = View.VISIBLE
                holder.bPostSponsoredLink.visibility = View.VISIBLE
                val sponsoredLinkText = "Descúbrelo"
                holder.bPostSponsoredLink.text = sponsoredLinkText
                val sponsoredTitleText = "Motor 100% eléctrico"
                holder.tvPostSponsoredTitle.text = sponsoredTitleText
            }
        }

        holder.itemView.setOnClickListener {
            postListener.onPostItemClicked(post, position)
        }
    }

    private fun setFavoriteState(post: Post, holder: ViewHolder) {
        for(member in post.membersFollowingAsFavorite){
            if (member.key == firebaseUser?.uid){
                holder.ivPostFavUncheck.visibility = View.GONE
                holder.ivPostFavCheck.visibility = View.VISIBLE
            }
        }
    }

    override fun getItemCount() = listOfPosts.size

    fun updateData(data: List<Post>) {
        listOfPosts.clear()
        listOfPosts.addAll(data)
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivPostCoverImage = itemView.findViewById<ImageView>(R.id.ivPostCoverImage)
        val tvPostUpcyclingService = itemView.findViewById<TextView>(R.id.tvPostUpcyclingService)
        val ivPostUpcyclingIdea = itemView.findViewById<ImageView>(R.id.ivPostUpcyclingIdea)
        val ivPostFavCheck = itemView.findViewById<ImageView>(R.id.ivPostFavCheck)
        val ivPostFavUncheck = itemView.findViewById<ImageView>(R.id.ivPostFavUncheck)
        val ivPostEdit = itemView.findViewById<ImageView>(R.id.ivPostEdit)
        val tvPostDesc = itemView.findViewById<TextView>(R.id.tvPostDesc)

        val clPostTitle = itemView.findViewById<ConstraintLayout>(R.id.clPostTitle)
        val vPostTitle = itemView.findViewById<View>(R.id.vPostTitle)

        val vPostSponsoredTop = itemView.findViewById<View>(R.id.vPostSponsoredTop)
        val clPostSponsoredTop = itemView.findViewById<ConstraintLayout>(R.id.clPostSponsoredTop)
        val ivPostSponsoredImage = itemView.findViewById<ImageView>(R.id.ivPostSponsoredImage)
        val bPostSponsoredLink = itemView.findViewById<Button>(R.id.bPostSponsoredLink)
        val tvPostSponsoredTitle = itemView.findViewById<TextView>(R.id.tvPostSponsoredTitle)
    }

    companion object {
        const val UPCYCLING_SERVICE = 1
        const val UPCYCLING_IDEA = 2
        const val SPONSORED_AD = 3
    }
}
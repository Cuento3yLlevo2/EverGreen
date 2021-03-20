package com.mahi.evergreen.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.mahi.evergreen.R
import com.mahi.evergreen.model.User
import de.hdodenhof.circleimageview.CircleImageView

class UsersAdapter(val usersListener: UsersListener) : RecyclerView.Adapter<UsersAdapter.ViewHolder>() {

    var listUsers = ArrayList<User>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        LayoutInflater.from(parent.context).inflate(
        R.layout.item_search_users, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = listUsers[position]

        Glide.with(holder.itemView.context) // contexto
            .load(user.profileImage) // donde esta la url de la imagen
            .apply(RequestOptions.circleCropTransform()) // la convertimos en circular
            .into(holder.ivSearchProfileImage) // donde la vamos a colocar

        holder.tvSearchUsername.text = user.username

        if (user.status.equals("offline")) {
            holder.civOfflineStatus.visibility = View.VISIBLE
            holder.civOnlineStatus.visibility = View.GONE
        } else {
            holder.civOfflineStatus.visibility = View.GONE
            holder.civOnlineStatus.visibility = View.VISIBLE
        }

        holder.itemView.setOnClickListener {
            usersListener.onUserClicked(user, position)
        }

    }

    override fun getItemCount() = listUsers.size

    fun updateData(data: List<User>) {
        listUsers.clear()
        listUsers.addAll(data)
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivSearchProfileImage = itemView.findViewById<ImageView>(R.id.ivSearchProfileImage)
        val tvSearchUsername = itemView.findViewById<TextView>(R.id.tvSearchUsername)
        val civOnlineStatus = itemView.findViewById<CircleImageView>(R.id.civOnlineStatus)
        val civOfflineStatus = itemView.findViewById<CircleImageView>(R.id.civOfflineStatus)
    }
}
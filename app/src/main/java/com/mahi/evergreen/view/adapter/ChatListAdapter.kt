package com.mahi.evergreen.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.mahi.evergreen.R
import com.mahi.evergreen.model.ChatListItem

class ChatListAdapter(val chatListListener: ChatListListener) : RecyclerView.Adapter<ChatListAdapter.ViewHolder>() {

    var listOfChatListItems = ArrayList<ChatListItem>()
    var firebaseUser = Firebase.auth.currentUser

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                    R.layout.item_inbox_chat_list, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chatListItem = listOfChatListItems[position]


        holder.tvChatPostTitle.text = chatListItem.postTitle

        Glide.with(holder.itemView.context) // contexto
                .load(chatListItem.postImageURL) // donde esta la url de la imagen
                .placeholder(R.drawable.post_default_image) // placeholder
                .into(holder.ivChatPostImage) // donde la vamos a colocar

        holder.tvChatUsername.text = chatListItem.chatUsername
        // If chat.url.isNullOrEmpty() && !chat.message.equals("sent you an image.") We know that the message sent is a Text message
        if (chatListItem.lastChatMessageItem?.url.isNullOrEmpty() && !chatListItem.lastChatMessageItem?.message.equals("sent you an image.")){
            holder.tvChatLastMessage.text = chatListItem.lastChatMessageItem?.message
        } else {
            // If chat.url is Not NullOrEmpty && chat.message.equals("sent you an image.") We know that massage sent is a Image
            holder.tvChatLastMessage.text = "Se ha enviado una imagen"
        }

        if (chatListItem.lastChatMessageItem?.isseen == true) {
            holder.tvChatLastMessage.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.check_circle,0,0,0)
        } else {
            holder.tvChatLastMessage.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.unchecked_circle,0,0,0)
        }


        holder.itemView.setOnClickListener {
            chatListListener.onChatListItemClicked(chatListItem, position)
        }
    }

    override fun getItemCount() = listOfChatListItems.size

    fun updateData(data: List<ChatListItem>) {
        listOfChatListItems.clear()
        listOfChatListItems.addAll(data)
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivChatPostImage = itemView.findViewById<ImageView>(R.id.ivChatPostImage)
        val tvChatUsername = itemView.findViewById<TextView>(R.id.tvChatUsername)
        val tvChatPostTitle = itemView.findViewById<TextView>(R.id.tvChatPostTitle)
        val tvChatLastMessage = itemView.findViewById<TextView>(R.id.tvChatLastMessage)
    }
}
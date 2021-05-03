package com.mahi.evergreen.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.mahi.evergreen.R
import com.mahi.evergreen.model.Chat
import com.mahi.evergreen.network.FirebaseDatabaseService

class ChatListAdapter(val chatListListener: ChatListListener) : RecyclerView.Adapter<ChatListAdapter.ViewHolder>() {

    var listOfChats = ArrayList<Chat>()
    var firebaseUser = Firebase.auth.currentUser
    var firebaseService = FirebaseDatabaseService()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                    R.layout.item_inbox_chat_list, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chat = listOfChats[position]
        var chatUsername = ""
        if (chat.chatID != null && firebaseUser?.uid != null){
            val currentUserID = firebaseUser?.uid
            chatUsername = currentUserID?.let { firebaseService.getChatUsername(chat.chatID, it) }.toString()
        }

        holder.tvChatPostTitle.text = chat.postTitle

        Glide.with(holder.itemView.context) // contexto
                .load(chat.postImageURL) // donde esta la url de la imagen
                .placeholder(R.drawable.post_default_image) // placeholder
                .into(holder.ivChatPostImage) // donde la vamos a colocar

        holder.tvChatUsername.text = chatUsername
        // If  !chat.lastMessage.equals("sent you an image.") We know that the message sent is a Text message
        if (!chat.lastMessage.equals("sent you an image.")){
            holder.tvChatLastMessage.text = chat.lastMessage
        } else {
            // If chat.lastMessage.equals("sent you an image.") We know that massage sent is a Image
            holder.tvChatLastMessage.text = "Se ha enviado una imagen"
        }

        if (chat.isSeen == true) {
            holder.tvChatLastMessage.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.check_circle,0,0,0)
        } else {
            holder.tvChatLastMessage.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.unchecked_circle,0,0,0)
        }


        holder.itemView.setOnClickListener {
            chatListListener.onChatListItemClicked(chat, position)
        }
    }

    override fun getItemCount() = listOfChats.size

    fun updateData(data: List<Chat>) {
        listOfChats.clear()
        listOfChats.addAll(data)
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivChatPostImage = itemView.findViewById<ImageView>(R.id.ivChatPostImage)
        val tvChatUsername = itemView.findViewById<TextView>(R.id.tvChatUsername)
        val tvChatPostTitle = itemView.findViewById<TextView>(R.id.tvChatPostTitle)
        val tvChatLastMessage = itemView.findViewById<TextView>(R.id.tvChatLastMessage)
    }
}
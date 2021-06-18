package com.mahi.evergreen.view.adapter

import android.content.Context
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
import com.mahi.evergreen.network.Callback
import com.mahi.evergreen.network.DatabaseService
import java.lang.Exception

/**
 * A subclass of RecyclerView.Adapter responsible for providing views
 * that represent every Chat List in a data set.
 * This class provides views for the ChatListFragment.kt
 */
class ChatListAdapter(private val chatListListener: ChatListListener, val context: Context?) : RecyclerView.Adapter<ChatListAdapter.ViewHolder>() {

    private var listOfChats = ArrayList<Chat>()
    var firebaseUser = Firebase.auth.currentUser
    private var firebaseService = DatabaseService()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                    R.layout.item_inbox_chat_list, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Reverse to get the chat with new interactions at the top
        val listOfChatsReversed = listOfChats.reversed()
        val chat = listOfChatsReversed[position]
        var chatUsername = ""
        if (chat.chatID != null && firebaseUser?.uid != null){
            val currentUserID = firebaseUser?.uid
            if (currentUserID != null){
                firebaseService.getChatUsername(chat.chatID, currentUserID, object: Callback<String> {
                    override fun onSuccess(result: String?) {
                        if (result != null) {
                            chatUsername = result
                            holder.tvChatUsername?.text = chatUsername
                        }
                    }

                    override fun onFailure(exception: Exception) {
                        // on failure
                    }

                })
            }
        }

        holder.tvChatPostTitle?.text = chat.postTitle

        holder.ivChatPostImage?.let {
            Glide.with(holder.itemView.context) // contexto
                .load(chat.postImageURL) // donde esta la url de la imagen
                .placeholder(R.drawable.post_default_image) // placeholder
                .into(it)
        } // donde la vamos a colocar

        holder.tvChatUsername?.text = chatUsername
        // If  !chat.lastMessage.equals("sent you an image.") We know that the message sent is a Text message
        if (!chat.lastMessage.equals("sent you an image.")){
            holder.tvChatLastMessage?.text = chat.lastMessage
        } else {

            // If chat.lastMessage.equals("sent you an image.") We know that massage sent is a Image
            // "Se ha enviado una imagen"
            holder.tvChatLastMessage?.text = context?.getString(R.string.chat_image_sent)


        }

        if (chat.isSeen == true) {
            holder.tvChatLastMessage?.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.check_circle,0,0,0)
        } else {
            holder.tvChatLastMessage?.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.unchecked_circle,0,0,0)
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
        val ivChatPostImage: ImageView? = itemView.findViewById(R.id.ivChatPostImage)
        val tvChatUsername: TextView? = itemView.findViewById(R.id.tvChatUsername)
        val tvChatPostTitle: TextView? = itemView.findViewById(R.id.tvChatPostTitle)
        val tvChatLastMessage: TextView? = itemView.findViewById(R.id.tvChatLastMessage)
    }
}
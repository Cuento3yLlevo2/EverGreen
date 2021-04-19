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
import com.mahi.evergreen.model.ChatMessageItem
import de.hdodenhof.circleimageview.CircleImageView

class ChatMessagesAdapter(val chatMessagesListener: ChatMessagesListener, val profileImageUrl: String) : RecyclerView.Adapter<ChatMessagesAdapter.ViewHolder>() {

    var listChats = ArrayList<ChatMessageItem>()
    val rightMessageItem: Int = 1
    val leftMessageItem: Int = 0
    var firebaseUser = Firebase.auth.currentUser
    var currentViewType: Int? = null
    var imageChatSent : Boolean = false


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return if (viewType == leftMessageItem){
            currentViewType = leftMessageItem
            ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_message_left, parent, false))
        } else {
            currentViewType = rightMessageItem
            ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_message_right, parent, false))
        }
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        // Avoids Recycling chats already download from database to be able to keep correct order on layout
        holder.setIsRecyclable(false)

        val chat = listChats[position]

        if (currentViewType != rightMessageItem){
            holder.civLeftProfileImage?.let {
                Glide.with(holder.itemView.context) // contexto
                        .load(profileImageUrl) // donde esta la url de la imagen
                        .apply(RequestOptions.circleCropTransform()) // la convertimos en circular
                        .into(holder.civLeftProfileImage) // donde la vamos a colocar
            }
        }

        // If chat.url.isNullOrEmpty() && !chat.message.equals("sent you an image.") We know that the message sent is a Text message
        if (chat.url.isNullOrEmpty() && !chat.message.equals("sent you an image.")){
            imageChatSent = false
            // Text message - Right side (Current user is the Sender)
            if(chat.sender.equals(firebaseUser?.uid)){
                holder.tvRightTextMessage.text = chat.message

            }

            // Text message - Left side
            else if (!chat.sender.equals(firebaseUser?.uid)) {
                holder.tvLeftTextMessage.text = chat.message
            }

        } else {
            // If chat.url is Not NullOrEmpty && chat.message.equals("sent you an image.") We know that massage sent is a Image
            imageChatSent = true

            // Image message - Right side (Current user is the Sender)
            if(chat.sender.equals(firebaseUser?.uid)){
                holder.tvRightTextMessage.visibility = View.GONE
                Glide.with(holder.itemView.context) // contexto
                        .load(chat.url) // donde esta la url de la imagen
                        .placeholder(R.drawable.ic_baseline_image_24)
                        .into(holder.ivRightImageMessage) // donde la vamos a colocar
                holder.ivRightImageMessage.visibility = View.VISIBLE
            }

            // Image message - Left side
            else if (!chat.sender.equals(firebaseUser?.uid)) {
                holder.tvLeftTextMessage.visibility = View.GONE
                Glide.with(holder.itemView.context) // contexto
                        .load(chat.url) // donde esta la url de la imagen
                        .placeholder(R.drawable.ic_baseline_image_24)
                        .into(holder.ivLeftImageMessage) // donde la vamos a colocar
                holder.ivLeftImageMessage.visibility = View.VISIBLE
            }
        }

        // sent and seen messages
        if (position == listChats.size-1){

            if (chat.isseen == true) {
                holder.tvLeftIsTextSeen?.let {
                    holder.tvLeftIsTextSeen.text = "Leído"
                    if (imageChatSent)
                        holder.tvLeftIsTextSeen.layoutParams = changeLayoutParams(holder.tvLeftIsTextSeen.layoutParams as RelativeLayout.LayoutParams?)
                }
                holder.tvRightIsTextSeen?.let {
                    holder.tvRightIsTextSeen.text = "Leído"
                    if (imageChatSent)
                        holder.tvRightIsTextSeen.layoutParams = changeLayoutParams(holder.tvRightIsTextSeen.layoutParams as RelativeLayout.LayoutParams?)
                }

            } else {

                holder.tvLeftIsTextSeen?.let {
                    holder.tvLeftIsTextSeen.text = "Enviado"
                    if (imageChatSent)
                        holder.tvLeftIsTextSeen.layoutParams = changeLayoutParams(holder.tvLeftIsTextSeen.layoutParams as RelativeLayout.LayoutParams?)
                }
                holder.tvRightIsTextSeen?.let {
                    holder.tvRightIsTextSeen.text = "Enviado"
                    if (imageChatSent)
                        holder.tvRightIsTextSeen.layoutParams = changeLayoutParams(holder.tvRightIsTextSeen.layoutParams as RelativeLayout.LayoutParams?)
                }
            }
        } else {
            holder.tvLeftIsTextSeen?.let { holder.tvLeftIsTextSeen.visibility = View.GONE }
            holder.tvRightIsTextSeen?.let { holder.tvRightIsTextSeen.visibility = View.GONE }
        }


        holder.itemView.setOnClickListener {
            chatMessagesListener.onChatClicked(chat, position)
        }

    }

    private fun changeLayoutParams(layoutParams: RelativeLayout.LayoutParams?): RelativeLayout.LayoutParams? {
        layoutParams?.setMargins(0,245,10,0)
        return layoutParams
    }

    override fun getItemCount() = listChats.size

    fun updateData(chatMessageItems: List<ChatMessageItem>) {
        listChats.clear()
        for (chat in chatMessageItems) {
            listChats.add(chat)
        }
        // listChats.addAll(data)
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val civLeftProfileImage = itemView.findViewById<CircleImageView>(R.id.civLeftProfileImage)
        val tvLeftTextMessage = itemView.findViewById<TextView>(R.id.tvLeftTextMessage)
        val ivLeftImageMessage = itemView.findViewById<ImageView>(R.id.ivLeftImageMessage)
        val tvLeftIsTextSeen = itemView.findViewById<TextView>(R.id.tvLeftIsTextSeen)

        val tvRightTextMessage = itemView.findViewById<TextView>(R.id.tvRightTextMessage)
        val ivRightImageMessage = itemView.findViewById<ImageView>(R.id.ivRightImageMessage)
        val tvRightIsTextSeen = itemView.findViewById<TextView>(R.id.tvRightIsTextSeen)

    }

    override fun getItemViewType(position: Int): Int {
        return if (listChats[position].sender.equals(firebaseUser?.uid)) {
            1
        } else {
            0
        }
    }

}
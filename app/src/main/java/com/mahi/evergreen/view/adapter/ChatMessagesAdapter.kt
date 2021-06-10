package com.mahi.evergreen.view.adapter

import android.content.Context
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
import com.mahi.evergreen.model.ChatMessage
import de.hdodenhof.circleimageview.CircleImageView

class ChatMessagesAdapter(
    private val chatMessagesListener: ChatMessagesListener,
    private val profileImageUrl: String,
    val context: Context?
) : RecyclerView.Adapter<ChatMessagesAdapter.ViewHolder>() {

    var listOfMessages = ArrayList<ChatMessage>()
    private val rightMessageItem: Int = 1
    private val leftMessageItem: Int = 0
    var firebaseUser = Firebase.auth.currentUser
    private var currentViewType: Int? = null
    private var imageChatSent : Boolean = false


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

        val message = listOfMessages[position]

        if (currentViewType != rightMessageItem){
            holder.civLeftProfileImage.let {
                holder.civLeftProfileImage?.let { it1 ->
                    Glide.with(holder.itemView.context) // contexto
                        .load(profileImageUrl) // donde esta la url de la imagen
                        .apply(RequestOptions.circleCropTransform()) // la convertimos en circular
                        .into(it1)
                } // donde la vamos a colocar
            }
        }

        // If chat.url.isNullOrEmpty() && !chat.message.equals("sent you an image.") We know that the message sent is a Text message
        if (message.url.isNullOrEmpty() && !message.message.equals("sent you an image.")){
            imageChatSent = false
            // Text message - Right side (Current user is the Sender)
            if(message.sender.equals(firebaseUser?.uid)){
                holder.tvRightTextMessage?.text = message.message

            }

            // Text message - Left side
            else if (!message.sender.equals(firebaseUser?.uid)) {
                holder.tvLeftTextMessage?.text = message.message
            }

        } else {
            // If chat.url is Not NullOrEmpty && chat.message.equals("sent you an image.") We know that massage sent is a Image
            imageChatSent = true

            // Image message - Right side (Current user is the Sender)
            if(message.sender.equals(firebaseUser?.uid)){
                holder.tvRightTextMessage?.visibility = View.GONE
                holder.ivRightImageMessage?.let {
                    Glide.with(holder.itemView.context) // contexto
                        .load(message.url) // donde esta la url de la imagen
                        .placeholder(R.drawable.ic_baseline_image_24)
                        .into(it)
                } // donde la vamos a colocar
                holder.ivRightImageMessage?.visibility = View.VISIBLE
            }

            // Image message - Left side
            else if (!message.sender.equals(firebaseUser?.uid)) {
                holder.tvLeftTextMessage?.visibility = View.GONE
                holder.ivLeftImageMessage?.let {
                    Glide.with(holder.itemView.context) // contexto
                        .load(message.url) // donde esta la url de la imagen
                        .placeholder(R.drawable.ic_baseline_image_24)
                        .into(it)
                } // donde la vamos a colocar
                holder.ivLeftImageMessage?.visibility = View.VISIBLE
            }
        }

        // sent and seen messages
        if (position == listOfMessages.size-1){

            if (message.isSeen == true) {
                holder.tvLeftIsTextSeen.let {
                    holder.tvLeftIsTextSeen?.text = context?.getString(R.string.message_read)
                    if (imageChatSent)
                        holder.tvLeftIsTextSeen?.layoutParams = changeLayoutParams(holder.tvLeftIsTextSeen?.layoutParams as RelativeLayout.LayoutParams?)
                }
                holder.tvRightIsTextSeen.let {
                    holder.tvRightIsTextSeen?.text = context?.getString(R.string.message_read)
                    if (imageChatSent)
                        holder.tvRightIsTextSeen?.layoutParams = changeLayoutParams(holder.tvRightIsTextSeen?.layoutParams as RelativeLayout.LayoutParams?)
                }

            } else {

                holder.tvLeftIsTextSeen.let {
                    holder.tvLeftIsTextSeen?.text = context?.getString(R.string.message_sent)
                    if (imageChatSent)
                        holder.tvLeftIsTextSeen?.layoutParams = changeLayoutParams(holder.tvLeftIsTextSeen?.layoutParams as RelativeLayout.LayoutParams?)
                }
                holder.tvRightIsTextSeen.let {
                    holder.tvRightIsTextSeen?.text = context?.getString(R.string.message_sent)
                    if (imageChatSent)
                        holder.tvRightIsTextSeen?.layoutParams = changeLayoutParams(holder.tvRightIsTextSeen?.layoutParams as RelativeLayout.LayoutParams?)
                }
            }
        } else {
            holder.tvLeftIsTextSeen.let { holder.tvLeftIsTextSeen?.visibility = View.GONE }
            holder.tvRightIsTextSeen.let { holder.tvRightIsTextSeen?.visibility = View.GONE }
        }


        holder.itemView.setOnClickListener {
            chatMessagesListener.onMessageItemClicked(message, position)
        }

    }

    private fun changeLayoutParams(layoutParams: RelativeLayout.LayoutParams?): RelativeLayout.LayoutParams? {
        layoutParams?.setMargins(0,245,10,0)
        return layoutParams
    }

    override fun getItemCount() = listOfMessages.size

    fun updateData(chatMessageItems: List<ChatMessage>) {
        listOfMessages.clear()
        for (message in chatMessageItems) {
            listOfMessages.add(message)
        }
        // listChats.addAll(data)
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val civLeftProfileImage: CircleImageView? = itemView.findViewById(R.id.civLeftProfileImage)
        val tvLeftTextMessage: TextView? = itemView.findViewById(R.id.tvLeftTextMessage)
        val ivLeftImageMessage: ImageView? = itemView.findViewById(R.id.ivLeftImageMessage)
        val tvLeftIsTextSeen: TextView? = itemView.findViewById(R.id.tvLeftIsTextSeen)

        val tvRightTextMessage: TextView? = itemView.findViewById(R.id.tvRightTextMessage)
        val ivRightImageMessage: ImageView? = itemView.findViewById(R.id.ivRightImageMessage)
        val tvRightIsTextSeen: TextView? = itemView.findViewById(R.id.tvRightIsTextSeen)

    }

    override fun getItemViewType(position: Int): Int {
        return if (listOfMessages[position].sender.equals(firebaseUser?.uid)) {
            1
        } else {
            0
        }
    }

}
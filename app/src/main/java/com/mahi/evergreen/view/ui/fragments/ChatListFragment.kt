package com.mahi.evergreen.view.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.mahi.evergreen.databinding.FragmentChatListBinding
import com.mahi.evergreen.model.Chat
import com.mahi.evergreen.view.adapter.ChatListAdapter
import com.mahi.evergreen.view.adapter.ChatListListener
import com.mahi.evergreen.view.ui.activities.MessageChatActivity
import com.mahi.evergreen.viewmodel.ChatListViewModel


class ChatListFragment : Fragment(), ChatListListener {

    private lateinit var chatListAdapter: ChatListAdapter
    private lateinit var viewModel: ChatListViewModel

    private var _binding: FragmentChatListBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(ChatListViewModel::class.java)

        viewModel.refreshChatList()

        chatListAdapter = ChatListAdapter(this)
        binding.rvChatList.apply {
            layoutManager = LinearLayoutManager(view.context, LinearLayoutManager.VERTICAL, false)
            adapter = chatListAdapter
        }


        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshChatList()
        observeViewModel()
    }


    override fun onChatListItemClicked(chatListItem: Chat, position: Int) {
        val intent = Intent(context, MessageChatActivity::class.java)
        intent.putExtra("clicked_chat_id", chatListItem.chatID)
        // type 1 = chatActivity started from chatList, type 2 = chatActivity started from userList
        intent.putExtra("type", 1)
        startActivity(intent)
    }

    override fun observeViewModel() {
        viewModel.chatList.observe(viewLifecycleOwner, { chat ->
            chatListAdapter.updateData(chat)
        })

        viewModel.isLoading.observe(viewLifecycleOwner, {
            if(it != null)
                binding.rlBaseChats.visibility = View.INVISIBLE
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
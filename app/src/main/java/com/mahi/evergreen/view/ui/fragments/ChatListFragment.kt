package com.mahi.evergreen.view.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.mahi.evergreen.R
import com.mahi.evergreen.databinding.FragmentChatListBinding
import com.mahi.evergreen.model.Chat
import com.mahi.evergreen.view.adapter.ChatListAdapter
import com.mahi.evergreen.view.adapter.ChatListListener
import com.mahi.evergreen.viewmodel.ChatListViewModel

/**
 * Fragment class that Populates the view of the list of Chats available for a user
 * Using a recyclerview
 */
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

        chatListAdapter = ChatListAdapter(this, context)
        binding.rvChatList.apply {
            layoutManager = LinearLayoutManager(view.context, LinearLayoutManager.VERTICAL, false)
            adapter = chatListAdapter
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().navigate(R.id.navHomeFragment)
        }

        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshChatList()
        observeViewModel()
    }


    override fun onChatListItemClicked(chatListItem: Chat, position: Int) {
        // type 1 = chatActivity started from chatList,
        // type 2 = chatActivity started from userList,
        // type 3 = chatActivity started from Post

        /*
        val intent = Intent(context, MessageChatActivity::class.java)
        intent.putExtra("clicked_chat_id", chatListItem.chatID)
        intent.putExtra("type", 1)
        startActivity(intent)

         */

        val bundle = bundleOf(
            "type" to 1,
            "clicked_chat_id" to chatListItem.chatID
        )
        findNavController().navigate(R.id.action_navChatListFragment_to_messageChatFragment, bundle)

    }

    override fun observeViewModel() {
        viewModel.chatList.observe(viewLifecycleOwner, { chat ->
            chatListAdapter.updateData(chat)
            if (chat.isEmpty()) {
                binding.rlChatListEmpty.visibility = View.VISIBLE
            } else {
                binding.rlChatListEmpty.visibility = View.GONE
            }
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
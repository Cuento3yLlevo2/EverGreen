package com.mahi.evergreen.view.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.mahi.evergreen.databinding.FragmentChatListBinding
import com.mahi.evergreen.view.adapter.ChatListAdapter
import com.mahi.evergreen.view.adapter.ChatMessagesAdapter
import com.mahi.evergreen.viewmodel.ChatListViewModel
import com.mahi.evergreen.viewmodel.ChatMessagesViewModel


class ChatListFragment : Fragment() {

    private lateinit var chatListAdapter: ChatListAdapter
    private lateinit var viewModel: ChatListViewModel

    private var _binding: FragmentChatListBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentChatListBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(ChatListViewModel::class.java)

        // viewModel.refreshChatList(null)


        // observeViewModel()

    }

    private fun retrieveChatList() {

    }

}
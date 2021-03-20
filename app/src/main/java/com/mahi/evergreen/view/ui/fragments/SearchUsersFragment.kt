package com.mahi.evergreen.view.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.mahi.evergreen.databinding.FragmentSearchUsersBinding
import com.mahi.evergreen.model.User
import com.mahi.evergreen.view.adapter.UsersAdapter
import com.mahi.evergreen.view.adapter.UsersListener
import com.mahi.evergreen.viewmodel.UsersViewModel


class SearchUsersFragment : Fragment(), UsersListener {
    private lateinit var usersAdapter: UsersAdapter
    private lateinit var viewModel: UsersViewModel

    private var _binding: FragmentSearchUsersBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSearchUsersBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(UsersViewModel::class.java)
        viewModel.refresh()

        usersAdapter = UsersAdapter(this)
        binding.rvUsers.apply {
            layoutManager = LinearLayoutManager(view.context, LinearLayoutManager.VERTICAL, false)
            adapter = usersAdapter
        }

        observeViewModel()

    }

    override fun observeViewModel() {
        viewModel.listUsers.observe(viewLifecycleOwner, Observer<List<User>> {user ->
            usersAdapter.updateData(user)
        })

        viewModel.isLoading.observe(viewLifecycleOwner, Observer<Boolean> {
            if(it != null)
                binding.rlBaseUsers.visibility = View.INVISIBLE
        })
    }

    override fun onUserClicked(user: User, position: Int) {
        Toast.makeText(context, "User Clicked", Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
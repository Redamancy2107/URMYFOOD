package com.urmyfood.user.presentation.main.chat

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.urmyfood.user.R
import com.urmyfood.user.databinding.FragmentMainChatBinding
import com.urmyfood.user.di.ServiceLocator
import com.urmyfood.user.presentation.main.chat.adapter.ChatListAdapter

class ChatFragment : Fragment() {

    private var _binding: FragmentMainChatBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ChatViewModel by viewModels {
        ServiceLocator.provideChatViewModelFactory()
    }
    private lateinit var chatListAdapter: ChatListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        chatListAdapter = ChatListAdapter { session ->
            val bundle = Bundle().apply {
                putLong("sessionId", session.id)
                putString("shopName", session.shopName)
                putString("shopAvatarUrl", session.shopAvatarUrl)
            }
            findNavController().navigate(R.id.chatDetailFragment, bundle)
        }
        binding.rvChatList.adapter = chatListAdapter
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ChatViewModel.UiState.Loading -> Unit
                is ChatViewModel.UiState.Success -> {
                    chatListAdapter.submitList(state.sessions)
                    val isEmpty = state.sessions.isEmpty()
                    binding.rvChatList.visibility = if (isEmpty) View.GONE else View.VISIBLE
                    binding.tvEmptyChat.visibility = if (isEmpty) View.VISIBLE else View.GONE
                    binding.tvEmptyChat.text = if (binding.etSearchChat.text.isNullOrBlank())
                        "Chưa có cuộc trò chuyện nào"
                    else
                        "Không tìm thấy kết quả"
                }
                is ChatViewModel.UiState.Error -> Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnNotification.setOnClickListener {
            Toast.makeText(requireContext(), getString(R.string.toast_feature_in_development), Toast.LENGTH_SHORT).show()
        }
        binding.etSearchChat.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                viewModel.filterSessions(s?.toString() ?: "")
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

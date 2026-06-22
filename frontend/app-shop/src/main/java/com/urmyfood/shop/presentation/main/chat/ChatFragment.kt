package com.urmyfood.shop.presentation.main.chat

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
import com.urmyfood.shop.R
import com.urmyfood.shop.databinding.FragmentMainChatBinding
import com.urmyfood.shop.di.ServiceLocator
import com.urmyfood.shop.presentation.main.chat.adapter.ChatSessionsAdapter

class ChatFragment : Fragment() {

    private var _binding: FragmentMainChatBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ChatViewModel by viewModels {
        ServiceLocator.provideChatViewModelFactory()
    }
    private lateinit var chatSessionsAdapter: ChatSessionsAdapter

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

    override fun onResume() {
        super.onResume()
        viewModel.loadSessions()
    }

    private fun setupRecyclerView() {
        chatSessionsAdapter = ChatSessionsAdapter { session ->
            val bundle = Bundle().apply {
                putLong("sessionId", session.id)
                putString("customerName", session.customerName)
                putString("customerAvatarUrl", session.customerAvatarUrl)
            }
            findNavController().navigate(R.id.action_chat_to_chatDetail, bundle)
        }
        binding.rvChatList.adapter = chatSessionsAdapter
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ChatViewModel.UiState.Loading -> Unit
                is ChatViewModel.UiState.Success -> {
                    chatSessionsAdapter.submitList(state.sessions)
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
        binding.mainHeader.btnNotification.setOnClickListener {
            Toast.makeText(requireContext(), "Tính năng đang phát triển", Toast.LENGTH_SHORT).show()
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

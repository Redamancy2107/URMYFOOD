package com.urmyfood.shop.presentation.main.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.urmyfood.shop.R
import com.urmyfood.shop.databinding.FragmentChatDetailBinding
import com.urmyfood.shop.di.ServiceLocator
import com.urmyfood.shared.util.Event
import com.urmyfood.shop.presentation.main.chat.adapter.MessageAdapter

class ChatDetailFragment : Fragment() {

    private var _binding: FragmentChatDetailBinding? = null
    private val binding get() = _binding!!

    private val sessionId: Long by lazy { arguments?.getLong("sessionId") ?: 0L }

    private val viewModel: ChatDetailViewModel by viewModels {
        ServiceLocator.provideChatDetailViewModelFactory(sessionId)
    }

    private lateinit var messageAdapter: MessageAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
        viewModel.loadHistory()
    }

    override fun onResume() {
        super.onResume()
        viewModel.connectWebSocket()
    }

    override fun onPause() {
        super.onPause()
        viewModel.disconnectWebSocket()
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        binding.tvDetailName.text = arguments?.getString("customerName") ?: "Khách hàng"
        binding.tvDetailStatus.text = "● Đang hoạt động"
    }

    private fun setupRecyclerView() {
        messageAdapter = MessageAdapter()
        binding.rvMessages.apply {
            layoutManager = LinearLayoutManager(requireContext()).also { it.stackFromEnd = true }
            adapter = messageAdapter
        }
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ChatDetailViewModel.UiState.Loading -> Unit
                is ChatDetailViewModel.UiState.Success -> {
                    messageAdapter.submitList(state.messages)
                    scrollToBottom()
                }
                is ChatDetailViewModel.UiState.Error ->
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.incomingMessage.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { message ->
                messageAdapter.appendMessage(message)
                scrollToBottom()
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnSend.setOnClickListener {
            val text = binding.etMessage.text.toString()
            if (text.isNotBlank()) {
                viewModel.sendMessage(text)
                binding.etMessage.text?.clear()
            }
        }
        binding.btnMenu.setOnClickListener {
            Toast.makeText(requireContext(), "Chức năng đang phát triển", Toast.LENGTH_SHORT).show()
        }
        binding.btnCall.setOnClickListener {
            Toast.makeText(requireContext(), "Chức năng đang phát triển", Toast.LENGTH_SHORT).show()
        }
    }

    private fun scrollToBottom() {
        val count = messageAdapter.itemCount
        if (count > 0) binding.rvMessages.scrollToPosition(count - 1)
    }

    override fun onDestroyView() {
        // Safety net: ensure WebSocket is disconnected if onPause was not called
        // (e.g., programmatic fragment removal or unexpected lifecycle paths).
        viewModel.disconnectWebSocket()
        super.onDestroyView()
        _binding = null
    }
}

package com.urmyfood.user.presentation.main.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.urmyfood.user.R
import com.urmyfood.user.databinding.FragmentChatDetailBinding
import com.urmyfood.user.di.ServiceLocator
import com.urmyfood.shared.util.ChatEmojiPicker
import com.urmyfood.shared.util.Event
import com.urmyfood.user.presentation.main.chat.adapter.MessageAdapter

class ChatDetailFragment : Fragment() {

    private var _binding: FragmentChatDetailBinding? = null
    private val binding get() = _binding!!

    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let { viewModel.sendImageMessage(it, requireContext()) }
        }

    private val sessionId: Long by lazy { arguments?.getLong("sessionId") ?: 0L }
    private val otherAvatarUrl: String? by lazy { arguments?.getString("shopAvatarUrl") }

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
    }

    override fun onResume() {
        super.onResume()
        viewModel.connectWebSocket()
        viewModel.loadHistory()
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        binding.tvDetailName.text = arguments?.getString("shopName") ?: "Cửa hàng"
        binding.tvDetailStatus.text = "● Đang hoạt động"
        Glide.with(this)
            .load(otherAvatarUrl)
            .placeholder(R.drawable.ic_person_placeholder)
            .circleCrop()
            .into(binding.ivDetailAvatar)
    }

    private fun setupRecyclerView() {
        messageAdapter = MessageAdapter(otherAvatarUrl)
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

        viewModel.uploadError.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
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
        listOf(binding.chipReply1, binding.chipReply2, binding.chipReply3).forEach { chip ->
            chip.setOnClickListener {
                binding.etMessage.setText(chip.text)
                binding.etMessage.setSelection(binding.etMessage.length())
            }
        }
        binding.btnEmoji.setOnClickListener {
            ChatEmojiPicker.open(binding.btnEmoji, binding.etMessage)
        }
        binding.btnAddImage.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }
    }

    private fun scrollToBottom() {
        val count = messageAdapter.itemCount
        if (count > 0) binding.rvMessages.scrollToPosition(count - 1)
    }

    override fun onDestroyView() {
        // Keep WebSocket alive through temporary overlays such as the image picker;
        // disconnect only when leaving this chat view.
        ChatEmojiPicker.dismiss()
        viewModel.disconnectWebSocket()
        super.onDestroyView()
        _binding = null
    }
}

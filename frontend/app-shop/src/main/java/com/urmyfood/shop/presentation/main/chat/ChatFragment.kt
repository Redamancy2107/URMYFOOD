package com.urmyfood.shop.presentation.main.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.fragment.app.viewModels
import com.urmyfood.shop.R
import com.urmyfood.shop.databinding.FragmentMainChatBinding
import com.urmyfood.shop.presentation.main.chat.adapter.ChatSessionsAdapter

/**
 * Chat List Fragment.
 * Displays a list of conversations between the user and stores/other users.
 */
class ChatFragment : Fragment() {

    private var _binding: FragmentMainChatBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ChatViewModel by viewModels()
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

    private fun setupRecyclerView() {
        chatSessionsAdapter = ChatSessionsAdapter { session ->
            val bundle = Bundle().apply {
                putString("customerName", session.customerName)
            }
            findNavController().navigate(R.id.action_chat_to_chatDetail, bundle)
        }
        binding.rvChatList.adapter = chatSessionsAdapter
    }

    private fun observeViewModel() {
        viewModel.chatSessions.observe(viewLifecycleOwner) { sessions ->
            chatSessionsAdapter.submitList(sessions)
        }
    }

    private fun setupClickListeners() {
        binding.mainHeader.btnNotification.setOnClickListener {
            Toast.makeText(requireContext(), "Tính năng đang phát triển", Toast.LENGTH_SHORT).show()
        }

        // Placeholder for search action
        binding.etSearchChat.setOnEditorActionListener { _, _, _ ->
            Toast.makeText(requireContext(), "Tìm kiếm: ${binding.etSearchChat.text}", Toast.LENGTH_SHORT).show()
            true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

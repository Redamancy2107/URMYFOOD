package com.urmyfood.user.presentation.main.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.urmyfood.user.R
import com.urmyfood.user.databinding.FragmentMainChatBinding

/**
 * Chat List Fragment.
 * Displays a list of conversations between the user and stores/other users.
 */
class ChatFragment : Fragment() {

    private var _binding: FragmentMainChatBinding? = null
    private val binding get() = _binding!!

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
    }

    private fun setupRecyclerView() {
        val dummySessions = listOf(
            com.urmyfood.user.presentation.model.ChatSession(
                1, "The Coffee House", com.urmyfood.user.R.drawable.bg_food_banner,
                "Dạ quán đã nhận được ghi chú ít đá của bạn ạ.", "10:45", 1
            ),
            com.urmyfood.user.presentation.model.ChatSession(
                2, "Pizza Hut - Phạm Văn Đồng", com.urmyfood.user.R.drawable.bg_food_banner,
                "Cảm ơn bạn đã ủng hộ quán nhé!", "Hôm qua", 0
            ),
            com.urmyfood.user.presentation.model.ChatSession(
                3, "Linh Trần", com.urmyfood.user.R.drawable.bg_food_banner,
                "Ê trưa nay ăn bún bò không?", "T.2", 0
            )
        )

        binding.rvChatList.adapter = com.urmyfood.user.presentation.main.chat.adapter.ChatListAdapter(dummySessions) { session ->
            findNavController().navigate(R.id.chatDetailFragment)
        }
    }

    private fun setupClickListeners() {
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

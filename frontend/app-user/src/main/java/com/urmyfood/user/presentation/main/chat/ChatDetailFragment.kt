package com.urmyfood.user.presentation.main.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.urmyfood.user.databinding.FragmentChatDetailBinding
import com.urmyfood.user.presentation.main.chat.adapter.MessageAdapter
import com.urmyfood.user.presentation.model.Message

class ChatDetailFragment : Fragment() {

    private var _binding: FragmentChatDetailBinding? = null
    private val binding get() = _binding!!

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
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
        // Dummy data for header
        binding.tvDetailName.text = "Linh Trần"
        binding.tvDetailStatus.text = "● Đang hoạt động"
    }

    private fun setupRecyclerView() {
        val dummyMessages = listOf(
            Message(1, "Ê trưa nay ăn bún bò không?", "10:30", false),
            Message(2, "Bún bò quán nào thế?", "10:31", true),
            Message(3, "Quán bà Chiểu ấy, đang có Flash Sale trên URMYFOOD nè", "10:32", false),
            Message(4, "Ok chốt luôn, đặt đi tôi trả tiền cho!", "10:35", true),
            Message(5, "Đã đặt! Đơn hàng đang được chuẩn bị nhé.", "10:36", false, isOrderCard = true)
        )

        binding.rvMessages.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = MessageAdapter(dummyMessages)
        }
    }

    private fun setupClickListeners() {
        binding.btnSend.setOnClickListener {
            val text = binding.etMessage.text.toString()
            if (text.isNotBlank()) {
                Toast.makeText(requireContext(), "Gửi: $text", Toast.LENGTH_SHORT).show()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

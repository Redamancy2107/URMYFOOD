package com.urmyfood.shop.presentation.main.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.urmyfood.shop.R
import com.urmyfood.shop.databinding.FragmentChatDetailBinding
import com.urmyfood.shop.presentation.main.chat.adapter.MessageAdapter
import com.urmyfood.shop.presentation.model.Message

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
        
        // Read custom name from args
        val customerName = arguments?.getString("customerName") ?: "Khách hàng"
        binding.tvDetailName.text = customerName
        binding.tvDetailStatus.text = "● Đang hoạt động"
    }

    private fun setupRecyclerView() {
        val dummyMessages = listOf(
            Message(1, "Chào shop, đơn hàng #ORD-1001 của mình chuẩn bị đến đâu rồi ạ?", "10:30", false),
            Message(2, "Dạ quán đang chuẩn bị món cho bạn rồi nhé ạ, shipper sắp tới nhận rồi.", "10:31", true),
            Message(3, "Ok shop, nhớ cho mình xin ít nước mắm ngọt nha.", "10:32", false),
            Message(4, "Dạ shop đã ghi chú lại rồi ạ, cảm ơn bạn đã ủng hộ quán!", "10:35", true),
            Message(5, "Đơn hàng đang chuẩn bị", "10:36", false, isOrderCard = true, orderId = "#ORD-1001")
        )

        binding.rvMessages.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = MessageAdapter(dummyMessages) { orderId ->
                val bundle = Bundle().apply {
                    putString("orderId", orderId)
                }
                findNavController().navigate(R.id.orderDetailFragment, bundle)
            }
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

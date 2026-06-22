package com.urmyfood.admin.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.urmyfood.admin.R
import com.urmyfood.admin.data.model.ShopVerification
import com.urmyfood.admin.data.repository.AdminRepository
import com.urmyfood.admin.databinding.FragmentPartnerApprovalBinding
import kotlinx.coroutines.launch

class PartnerApprovalFragment : Fragment() {
    private var _binding: FragmentPartnerApprovalBinding? = null
    private val binding get() = _binding!!
    private val repository = AdminRepository()
    private var verificationList = mutableListOf<ShopVerification>()
    private var selectedVerification: ShopVerification? = null
    private lateinit var adapter: VerificationAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPartnerApprovalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        loadPendingVerifications()

        binding.btnApprove.setOnClickListener {
            selectedVerification?.let { approve(it) }
        }

        binding.btnReject.setOnClickListener {
            selectedVerification?.let { reject(it) }
        }
    }

    private fun setupRecyclerView() {
        adapter = VerificationAdapter(verificationList) { verification ->
            selectVerification(verification)
        }
        binding.rvVerifications.layoutManager = LinearLayoutManager(requireContext())
        binding.rvVerifications.adapter = adapter
    }

    private fun loadPendingVerifications() {
        lifecycleScope.launch {
            val result = repository.getPendingVerifications()
            result.onSuccess { list ->
                verificationList.clear()
                verificationList.addAll(list)
                adapter.notifyDataSetChanged()
                binding.tvPendingCount.text = "${list.size} đang chờ"
                
                if (list.isEmpty()) {
                    binding.llDetailContent.visibility = View.GONE
                    binding.tvEmptyDetail.visibility = View.VISIBLE
                    binding.tvEmptyDetail.text = "Không có yêu cầu phê duyệt nào"
                } else {
                    binding.tvEmptyDetail.visibility = View.GONE
                }
            }.onFailure { error ->
                Toast.makeText(requireContext(), "Lỗi tải yêu cầu: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun selectVerification(verification: ShopVerification) {
        selectedVerification = verification
        binding.tvEmptyDetail.visibility = View.GONE
        binding.llDetailContent.visibility = View.VISIBLE

        binding.tvDetailShopName.text = verification.shopName
        binding.tvDetailCategory.text = verification.category ?: "Chưa xác định"
        binding.tvDetailAddress.text = verification.address ?: "Chưa xác định"
        binding.tvDetailCreatedAt.text = verification.createdAt ?: ""

        Glide.with(this)
            .load(verification.cccdFrontUrl)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .into(binding.ivCccdFront)

        Glide.with(this)
            .load(verification.cccdBackUrl)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .into(binding.ivCccdBack)
    }

    private fun approve(verification: ShopVerification) {
        lifecycleScope.launch {
            val result = repository.approveVerification(verification.id)
            result.onSuccess {
                Toast.makeText(requireContext(), "Đã phê duyệt đối tác thành công", Toast.LENGTH_SHORT).show()
                resetDetailPanel()
                loadPendingVerifications()
            }.onFailure { error ->
                Toast.makeText(requireContext(), "Phê duyệt thất bại: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun reject(verification: ShopVerification) {
        val input = EditText(requireContext()).apply {
            hint = "Nhập lý do từ chối"
        }
        AlertDialog.Builder(requireContext())
            .setTitle("Từ chối đối tác")
            .setMessage("Vui lòng nhập lý do từ chối yêu cầu của shop ${verification.shopName}:")
            .setView(input)
            .setPositiveButton("Xác nhận") { _, _ ->
                val reason = input.text.toString().trim()
                if (reason.isEmpty()) {
                    Toast.makeText(requireContext(), "Lý do từ chối không được để trống", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                performReject(verification.id, reason)
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun performReject(id: Long, reason: String) {
        lifecycleScope.launch {
            val result = repository.rejectVerification(id, reason)
            result.onSuccess {
                Toast.makeText(requireContext(), "Đã từ chối đối tác", Toast.LENGTH_SHORT).show()
                resetDetailPanel()
                loadPendingVerifications()
            }.onFailure { error ->
                Toast.makeText(requireContext(), "Thất bại: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun resetDetailPanel() {
        selectedVerification = null
        binding.llDetailContent.visibility = View.GONE
        binding.tvEmptyDetail.visibility = View.VISIBLE
        binding.tvEmptyDetail.text = "Chọn một yêu cầu từ danh sách bên trái để xem chi tiết"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Inner Adapter Class
    private class VerificationAdapter(
        private val items: List<ShopVerification>,
        private val onClick: (ShopVerification) -> Unit
    ) : RecyclerView.Adapter<VerificationAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val shopName: TextView = view.findViewById(R.id.tv_shop_name)
            val category: TextView = view.findViewById(R.id.tv_category)
            val createdAt: TextView = view.findViewById(R.id.tv_created_at)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_verification, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.shopName.text = item.shopName
            holder.category.text = item.category ?: "Chưa xác định"
            holder.createdAt.text = "Ngày gửi: ${item.createdAt?.substringBefore("T") ?: ""}"
            holder.itemView.setOnClickListener { onClick(item) }
        }

        override fun getItemCount() = items.size
    }
}

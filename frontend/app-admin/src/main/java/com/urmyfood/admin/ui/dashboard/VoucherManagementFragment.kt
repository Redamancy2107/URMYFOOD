package com.urmyfood.admin.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.urmyfood.admin.R
import com.urmyfood.admin.data.model.VoucherItem
import com.urmyfood.admin.data.repository.AdminRepository
import com.urmyfood.admin.databinding.FragmentVoucherManagementBinding
import com.urmyfood.admin.databinding.FragmentAddVoucherBinding
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class VoucherManagementFragment : Fragment() {
    private var _binding: FragmentVoucherManagementBinding? = null
    private val binding get() = _binding!!
    private val repository = AdminRepository()
    private val voucherList = mutableListOf<VoucherItem>()
    private lateinit var adapter: VoucherAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVoucherManagementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        loadVouchers()

        binding.btnAddVoucher.setOnClickListener {
            showAddVoucherDialog()
        }
    }

    private fun setupRecyclerView() {
        adapter = VoucherAdapter(voucherList) { voucher ->
            confirmDeleteVoucher(voucher)
        }
        binding.rvVouchers.layoutManager = LinearLayoutManager(requireContext())
        binding.rvVouchers.adapter = adapter
    }

    private fun loadVouchers() {
        lifecycleScope.launch {
            val result = repository.getAllVouchers()
            result.onSuccess { list ->
                voucherList.clear()
                voucherList.addAll(list)
                adapter.notifyDataSetChanged()
            }.onFailure { error ->
                Toast.makeText(requireContext(), "Lỗi tải voucher: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showAddVoucherDialog() {
        val dialogBinding = FragmentAddVoucherBinding.inflate(LayoutInflater.from(requireContext()))
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        var selectedExpiryDateBackend = ""

        dialogBinding.etExpiryDate.setOnClickListener {
            val calendar = java.util.Calendar.getInstance()
            val year = calendar.get(java.util.Calendar.YEAR)
            val month = calendar.get(java.util.Calendar.MONTH)
            val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)

            android.app.DatePickerDialog(requireContext(), { _, selYear, selMonth, selDay ->
                val displayDay = String.format("%02d", selDay)
                val displayMonth = String.format("%02d", selMonth + 1)
                dialogBinding.etExpiryDate.setText("$displayDay/$displayMonth/$selYear")
                selectedExpiryDateBackend = String.format("%d-%02d-%02d", selYear, selMonth + 1, selDay)
            }, year, month, day).show()
        }

        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnCreate.setOnClickListener {
            val code = dialogBinding.etCode.text.toString().trim()
            val title = dialogBinding.etTitle.text.toString().trim()
            val description = dialogBinding.etDescription.text.toString().trim()
            val discountValue = dialogBinding.etDiscountValue.text.toString().toDoubleOrNull()
            val minOrderValue = dialogBinding.etMinOrder.text.toString().toDoubleOrNull()
            val expiryDateText = dialogBinding.etExpiryDate.text.toString().trim()

            if (code.isEmpty() || title.isEmpty() || discountValue == null || minOrderValue == null || expiryDateText.isEmpty() || selectedExpiryDateBackend.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newVoucher = VoucherItem(
                id = null,
                code = code,
                title = title,
                description = description,
                discountValue = discountValue,
                minOrderValue = minOrderValue,
                expiryDate = selectedExpiryDateBackend
            )

            lifecycleScope.launch {
                val result = repository.createVoucher(newVoucher)
                result.onSuccess {
                    Toast.makeText(requireContext(), "Tạo mã giảm giá thành công", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    loadVouchers()
                }.onFailure { error ->
                    Toast.makeText(requireContext(), "Tạo voucher thất bại: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        dialog.show()
    }

    private fun confirmDeleteVoucher(voucher: VoucherItem) {
        AlertDialog.Builder(requireContext())
            .setTitle("Xóa mã giảm giá")
            .setMessage("Bạn có chắc chắn muốn xóa mã giảm giá '${voucher.code}' không?")
            .setPositiveButton("Xóa") { _, _ ->
                voucher.id?.let { deleteVoucher(it) }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun deleteVoucher(id: Long) {
        lifecycleScope.launch {
            val result = repository.deleteVoucher(id)
            result.onSuccess {
                Toast.makeText(requireContext(), "Đã xóa mã giảm giá thành công", Toast.LENGTH_SHORT).show()
                loadVouchers()
            }.onFailure { error ->
                Toast.makeText(requireContext(), "Xóa thất bại: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Inner Adapter Class
    private class VoucherAdapter(
        private val items: List<VoucherItem>,
        private val onDelete: (VoucherItem) -> Unit
    ) : RecyclerView.Adapter<VoucherAdapter.ViewHolder>() {

        private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val code: TextView = view.findViewById(R.id.tv_code)
            val title: TextView = view.findViewById(R.id.tv_title)
            val discount: TextView = view.findViewById(R.id.tv_discount)
            val minOrder: TextView = view.findViewById(R.id.tv_min_order)
            val expiry: TextView = view.findViewById(R.id.tv_expiry)
            val btnDelete: ImageView = view.findViewById(R.id.btn_delete)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_voucher, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.code.text = item.code
            holder.title.text = item.title
            holder.discount.text = currencyFormat.format(item.discountValue ?: 0.0)
            holder.minOrder.text = currencyFormat.format(item.minOrderValue ?: 0.0)
            
            val rawDate = item.expiryDate ?: ""
            val formattedDate = try {
                val parts = rawDate.split("-")
                if (parts.size == 3) {
                    "${parts[2]}/${parts[1]}/${parts[0]}"
                } else {
                    rawDate
                }
            } catch (e: Exception) {
                rawDate
            }
            holder.expiry.text = formattedDate
            
            holder.btnDelete.setOnClickListener { onDelete(item) }
        }

        override fun getItemCount() = items.size
    }
}

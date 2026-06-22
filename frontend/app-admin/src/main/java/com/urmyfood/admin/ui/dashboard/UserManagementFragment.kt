package com.urmyfood.admin.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.urmyfood.admin.R
import com.urmyfood.admin.data.model.AccountProfile
import com.urmyfood.admin.data.repository.AdminRepository
import com.urmyfood.admin.databinding.FragmentUserManagementBinding
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class UserManagementFragment : Fragment() {
    private var _binding: FragmentUserManagementBinding? = null
    private val binding get() = _binding!!
    private val repository = AdminRepository()
    private val accountsList = mutableListOf<AccountProfile>()
    private lateinit var adapter: AccountAdapter
    private var targetRole: String? = null

    companion object {
        private const val ARG_ROLE = "target_role"

        fun newInstance(role: String): UserManagementFragment {
            return UserManagementFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_ROLE, role)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        targetRole = arguments?.getString(ARG_ROLE)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserManagementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        loadAccounts()
    }

    private fun setupRecyclerView() {
        adapter = AccountAdapter(accountsList) { account ->
            confirmToggleAccountActive(account)
        }
        binding.rvUsers.layoutManager = LinearLayoutManager(requireContext())
        binding.rvUsers.adapter = adapter
    }

    private fun loadAccounts() {
        lifecycleScope.launch {
            val result = repository.getAllAccounts(0, 100, targetRole)
            result.onSuccess { page ->
                accountsList.clear()
                accountsList.addAll(page.content)
                adapter.notifyDataSetChanged()
            }.onFailure { error ->
                Toast.makeText(requireContext(), "Lỗi tải tài khoản: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun confirmToggleAccountActive(account: AccountProfile) {
        val actionText = if (account.isActive) "Khóa" else "Mở khóa"
        AlertDialog.Builder(requireContext())
            .setTitle("$actionText tài khoản")
            .setMessage("Bạn có chắc chắn muốn $actionText tài khoản của '${account.fullName}'?")
            .setPositiveButton("Xác nhận") { _, _ ->
                toggleAccountActive(account)
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun toggleAccountActive(account: AccountProfile) {
        lifecycleScope.launch {
            val result = repository.lockUnlockAccount(account.id, !account.isActive)
            result.onSuccess {
                Toast.makeText(requireContext(), "Đã cập nhật trạng thái tài khoản", Toast.LENGTH_SHORT).show()
                loadAccounts()
            }.onFailure { error ->
                Toast.makeText(requireContext(), "Cập nhật thất bại: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Inner Adapter Class
    private class AccountAdapter(
        private val items: List<AccountProfile>,
        private val onToggle: (AccountProfile) -> Unit
    ) : RecyclerView.Adapter<AccountAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val fullName: TextView = view.findViewById(R.id.tv_full_name)
            val email: TextView = view.findViewById(R.id.tv_email)
            val phone: TextView = view.findViewById(R.id.tv_phone)
            val role: TextView = view.findViewById(R.id.tv_role)
            val status: TextView = view.findViewById(R.id.tv_status)
            val btnToggle: MaterialButton = view.findViewById(R.id.btn_toggle)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_account, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.fullName.text = item.fullName ?: "Chưa thiết lập"
            holder.email.text = item.email
            holder.phone.text = item.phone ?: "Chưa thiết lập"
            holder.role.text = item.role

            val context = holder.itemView.context
            if (item.isActive) {
                holder.status.text = "Hoạt động"
                holder.status.setTextColor(context.getColor(R.color.mint_green))
                holder.btnToggle.text = "Khóa"
                holder.btnToggle.setBackgroundColor(context.getColor(R.color.error_red))
            } else {
                holder.status.text = "Đã khóa"
                holder.status.setTextColor(context.getColor(R.color.error_red))
                holder.btnToggle.text = "Mở khóa"
                holder.btnToggle.setBackgroundColor(context.getColor(R.color.mint_green))
            }

            holder.btnToggle.setOnClickListener { onToggle(item) }
        }

        override fun getItemCount() = items.size
    }
}

package com.urmyfood.admin.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.imageview.ShapeableImageView
import com.urmyfood.admin.R
import com.urmyfood.admin.data.model.AccountProfile
import com.urmyfood.admin.databinding.FragmentUserManagementBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class UserManagementFragment : Fragment() {
    private var _binding: FragmentUserManagementBinding? = null
    private val binding get() = _binding!!
    private val viewModel: UserManagementViewModel by viewModels()
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
        
        binding.tvPageTitle.text = if (targetRole == "SHOP") "Quản Lý Cửa Hàng" else "Quản Lý Người Dùng"
        
        setupRecyclerView()
        setupSortingHeaders()
        observeViewModel()

        viewModel.loadAccounts(targetRole)
    }

    private fun setupRecyclerView() {
        adapter = AccountAdapter(
            items = accountsList,
            onBlockClick = { account -> confirmAction(account, "BLOCK") },
            onDeleteClick = { account -> confirmAction(account, "DELETE") }
        )
        binding.rvUsers.layoutManager = LinearLayoutManager(requireContext())
        binding.rvUsers.adapter = adapter
    }

    private fun setupSortingHeaders() {
        binding.tvHeaderName.setOnClickListener { viewModel.setSort("fullName") }
        binding.tvHeaderEmail.setOnClickListener { viewModel.setSort("email") }
        binding.tvHeaderPhone.setOnClickListener { viewModel.setSort("phone") }
        binding.tvHeaderStatus.setOnClickListener { viewModel.setSort("isActive") }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                when (state) {
                    is UserManagementState.Loading -> {
                        // Could show a progress bar
                    }
                    is UserManagementState.Success -> {
                        accountsList.clear()
                        accountsList.addAll(state.accounts)
                        adapter.notifyDataSetChanged()
                    }
                    is UserManagementState.Error -> {
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> {}
                }
            }
        }
    }

    private fun confirmAction(account: AccountProfile, actionType: String) {
        val actionText = when (actionType) {
            "BLOCK" -> if (account.isActive) "Khóa" else "Mở khóa"
            "DELETE" -> "Xóa"
            else -> ""
        }

        val input = EditText(requireContext()).apply {
            hint = "Nhập lý do $actionText"
        }
        
        AlertDialog.Builder(requireContext())
            .setTitle("$actionText tài khoản")
            .setMessage("Bạn có chắc chắn muốn $actionText tài khoản của '${account.fullName}'?\nVui lòng nhập lý do:")
            .setView(input)
            .setPositiveButton("Xác nhận") { _, _ ->
                val reason = input.text.toString().trim()
                if (reason.isEmpty()) {
                    Toast.makeText(requireContext(), "Lý do không được để trống", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                
                if (actionType == "BLOCK") {
                    viewModel.toggleAccountActive(account, reason)
                } else if (actionType == "DELETE") {
                    viewModel.deleteAccount(account, reason)
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Inner Adapter Class
    private class AccountAdapter(
        private val items: List<AccountProfile>,
        private val onBlockClick: (AccountProfile) -> Unit,
        private val onDeleteClick: (AccountProfile) -> Unit
    ) : RecyclerView.Adapter<AccountAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val ivAvatar: ShapeableImageView = view.findViewById(R.id.iv_avatar)
            val fullName: TextView = view.findViewById(R.id.tv_full_name)
            val email: TextView = view.findViewById(R.id.tv_email)
            val phone: TextView = view.findViewById(R.id.tv_phone)
            val status: TextView = view.findViewById(R.id.tv_status)
            val btnToggle: ImageView = view.findViewById(R.id.btn_toggle)
            val btnDelete: ImageView = view.findViewById(R.id.btn_delete)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_account, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            val context = holder.itemView.context

            holder.fullName.text = item.fullName ?: "Chưa thiết lập"
            holder.email.text = item.email
            holder.phone.text = item.phone ?: "Chưa thiết lập"

            Glide.with(context)
                .load(item.avatarUrl)
                .circleCrop()
                .placeholder(R.drawable.ic_admin_avatar_default)
                .error(R.drawable.ic_admin_avatar_default)
                .into(holder.ivAvatar)

            if (item.isActive) {
                holder.status.text = "Hoạt động"
                holder.status.setTextColor(context.getColor(R.color.mint_green))
                holder.btnToggle.setImageResource(R.drawable.ic_lock_outline)
                holder.btnToggle.setColorFilter(context.getColor(R.color.error_red))
            } else {
                holder.status.text = "Đã khóa"
                holder.status.setTextColor(context.getColor(R.color.error_red))
                holder.btnToggle.setImageResource(R.drawable.ic_unlock)
                holder.btnToggle.setColorFilter(context.getColor(R.color.mint_green))
            }

            holder.btnToggle.setOnClickListener { onBlockClick(item) }
            holder.btnDelete.setOnClickListener { onDeleteClick(item) }
        }

        override fun getItemCount() = items.size
    }
}

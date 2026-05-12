package com.urmyfood.user.presentation.main.home

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.urmyfood.user.databinding.LayoutShareBottomSheetBinding
import com.urmyfood.user.presentation.model.Friend

class ShareBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: LayoutShareBottomSheetBinding? = null
    private val binding get() = _binding!!

    private val allFriends = listOf(
        Friend(1, "Nguyễn Vân Anh", null),
        Friend(2, "Trần Hoàng Nam", null),
        Friend(3, "Lê Thị Mai", null),
        Friend(4, "Phạm Quốc Bảo", null),
        Friend(5, "Đặng Thu Thảo", null),
        Friend(6, "Vũ Đức Trọng", null),
        Friend(7, "Bùi Minh Tuấn", null)
    )

    private var filteredFriends = allFriends.toMutableList()
    private lateinit var adapter: FriendAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LayoutShareBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearch()
    }

    private fun setupRecyclerView() {
        adapter = FriendAdapter(filteredFriends)
        binding.rvFriends.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFriends.adapter = adapter
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filter(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filter(query: String) {
        filteredFriends.clear()
        if (query.isEmpty()) {
            filteredFriends.addAll(allFriends)
        } else {
            filteredFriends.addAll(allFriends.filter { 
                it.name.contains(query, ignoreCase = true) 
            })
        }
        adapter.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "ShareBottomSheetFragment"
    }

    // Inner Adapter Class
    inner class FriendAdapter(private val friends: List<Friend>) :
        androidx.recyclerview.widget.RecyclerView.Adapter<FriendAdapter.ViewHolder>() {

        inner class ViewHolder(private val itemBinding: com.urmyfood.user.databinding.ItemShareFriendBinding) :
            androidx.recyclerview.widget.RecyclerView.ViewHolder(itemBinding.root) {
            
            fun bind(friend: Friend) {
                itemBinding.tvFriendName.text = friend.name
                itemBinding.ivFriendAvatar.setImageResource(com.urmyfood.user.R.drawable.ic_person_placeholder)
                
                updateSendButton(friend)

                itemBinding.btnSend.setOnClickListener {
                    if (!friend.isSent) {
                        friend.isSent = true
                        updateSendButton(friend)
                    }
                }
            }

            private fun updateSendButton(friend: Friend) {
                if (friend.isSent) {
                    itemBinding.btnSend.text = "Đã gửi"
                    itemBinding.btnSend.isEnabled = false
                    itemBinding.btnSend.backgroundTintList = 
                        android.content.res.ColorStateList.valueOf(
                            androidx.core.content.ContextCompat.getColor(
                                itemView.context, com.urmyfood.user.R.color.text_secondary
                            )
                        )
                } else {
                    itemBinding.btnSend.text = "Gửi"
                    itemBinding.btnSend.isEnabled = true
                    itemBinding.btnSend.backgroundTintList = 
                        android.content.res.ColorStateList.valueOf(
                            androidx.core.content.ContextCompat.getColor(
                                itemView.context, com.urmyfood.user.R.color.primary
                            )
                        )
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = com.urmyfood.user.databinding.ItemShareFriendBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(friends[position])
        }

        override fun getItemCount() = friends.size
    }
}

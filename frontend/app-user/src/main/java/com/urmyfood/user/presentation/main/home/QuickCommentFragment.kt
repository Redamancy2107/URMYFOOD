package com.urmyfood.user.presentation.main.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.urmyfood.user.databinding.LayoutQuickCommentSheetBinding

class QuickCommentFragment : BottomSheetDialogFragment() {

    private var _binding: LayoutQuickCommentSheetBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LayoutQuickCommentSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Ensure keyboard doesn't hide input
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        setupRecyclerView()
        setupClickListeners()
        setupBottomSheetBehavior()
    }

    private fun setupBottomSheetBehavior() {
        val behavior = (dialog as? BottomSheetDialog)?.behavior
        behavior?.apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            // Force 2/3 height
            val displayMetrics = resources.displayMetrics
            peekHeight = (displayMetrics.heightPixels * 0.67).toInt()
            isHideable = true
        }
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog as? BottomSheetDialog
        val bottomSheet = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.layoutParams?.height = (resources.displayMetrics.heightPixels * 0.67).toInt()
    }

    private fun setupRecyclerView() {
        val mockComments = listOf(
            MockComment("Thanh Hằng", "Nhìn thèm quá, có ship KTX khu B không ạ?", "2 phút trước"),
            MockComment("Minh Tuấn", "Sườn ở đây mềm, nước mắm ngon đỉnh luôn!", "15 phút trước"),
            MockComment("Ngọc Mai", "Mới đặt xong, giao hàng siêu nhanh.", "1 giờ trước"),
            MockComment("Quốc Bảo", "Shop ơi, combo 2 người có tặng kèm nước không?", "3 giờ trước"),
            MockComment("Thu Thảo", "Trà sữa trân châu đường đen ở đây béo ngậy, rất vừa vị.", "Vừa xong"),
            MockComment("Hoàng Nam", "Đã ăn nhiều lần, chất lượng vẫn ổn định như ngày đầu.", "5 giờ trước"),
            MockComment("Bích Phương", "Giao tới KTX khu A vẫn còn nóng hổi, vote 5 sao!", "2 ngày trước"),
            MockComment("Đức Trọng", "Giá hơi cao tí nhưng tiền nào của nấy, rất đáng thử.", "1 ngày trước"),
            MockComment("Minh Thư", "Mong shop có thêm nhiều mã giảm giá cho sinh viên.", "4 giờ trước"),
            MockComment("Anh Quân", "Bún bò Huế ở đây chuẩn vị cố đô luôn, tuyệt vời!", "10 phút trước")
        )
        
        binding.rvComments.apply {
            layoutManager = LinearLayoutManager(requireContext())
            val commentAdapter = CommentAdapter(mockComments)
            adapter = commentAdapter
            commentAdapter.notifyDataSetChanged()
            visibility = View.VISIBLE
        }
    }

    private fun setupClickListeners() {
        binding.btnSend.setOnClickListener {
            val text = binding.etComment.text.toString()
            if (text.isNotBlank()) {
                binding.etComment.text.clear()
                // In real app, we would send this to ViewModel
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    data class MockComment(val name: String, val content: String, val time: String)

    inner class CommentAdapter(private val comments: List<MockComment>) :
        androidx.recyclerview.widget.RecyclerView.Adapter<CommentAdapter.ViewHolder>() {

        inner class ViewHolder(private val itemBinding: com.urmyfood.user.databinding.ItemCommentBinding) :
            androidx.recyclerview.widget.RecyclerView.ViewHolder(itemBinding.root) {
            fun bind(comment: MockComment) {
                itemBinding.tvUserName.text = comment.name
                itemBinding.tvComment.text = comment.content
                itemBinding.tvTime.text = comment.time
                itemBinding.ivAvatar.setImageResource(com.urmyfood.user.R.drawable.ic_person_placeholder)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = com.urmyfood.user.databinding.ItemCommentBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(comments[position])
        }

        override fun getItemCount() = comments.size
    }

    companion object {
        const val TAG = "QuickCommentFragment"
    }
}

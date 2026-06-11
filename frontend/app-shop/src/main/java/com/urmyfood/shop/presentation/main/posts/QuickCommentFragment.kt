package com.urmyfood.shop.presentation.main.posts

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.urmyfood.shop.R
import com.urmyfood.shop.databinding.ItemCommentBinding
import com.urmyfood.shop.databinding.LayoutQuickCommentSheetBinding
import com.urmyfood.shop.domain.model.Comment

class QuickCommentFragment : BottomSheetDialogFragment() {

    private var _binding: LayoutQuickCommentSheetBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CommentViewModel by viewModels()

    private var activeReplyParentId: String? = null
    private var onCommentAddedListener: (() -> Unit)? = null

    private val commentAdapter = CommentListAdapter { parentComment ->
        activeReplyParentId = parentComment.commentId
        binding.replyIndicatorLayout.visibility = View.VISIBLE
        binding.tvReplyingTo.text = "Đang trả lời ${parentComment.authorName}..."
        binding.etComment.hint = "Phản hồi ${parentComment.authorName}..."
        binding.etComment.requestFocus()
        showKeyboard(binding.etComment)
    }
    private lateinit var postId: String

    fun setOnCommentAddedListener(listener: () -> Unit) {
        onCommentAddedListener = listener
    }

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

        postId = arguments?.getString("POST_ID") ?: ""

        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        setupRecyclerView()
        setupClickListeners()
        setupBottomSheetBehavior()
        observeUiState()
        observeSendResult()

        viewModel.loadComments(postId)
    }

    private fun setupBottomSheetBehavior() {
        val behavior = (dialog as? BottomSheetDialog)?.behavior
        behavior?.apply {
            state = BottomSheetBehavior.STATE_EXPANDED
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
        binding.rvComments.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = commentAdapter
        }
    }

    private fun setupClickListeners() {
        binding.btnSend.setOnClickListener {
            val text = binding.etComment.text.toString().trim()
            if (text.isNotBlank()) {
                viewModel.postComment(postId, text, activeReplyParentId)
                binding.etComment.text.clear()
                resetReplyState()
                hideKeyboard()
                onCommentAddedListener?.invoke()
            }
        }
        binding.btnCancelReply.setOnClickListener {
            resetReplyState()
        }
    }

    private fun resetReplyState() {
        activeReplyParentId = null
        binding.replyIndicatorLayout.visibility = View.GONE
        binding.etComment.hint = "Viết bình luận..."
    }

    private fun showKeyboard(view: View) {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        view.postDelayed({
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }, 100)
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.etComment.windowToken, 0)
    }

    private fun observeUiState() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is CommentUiState.Loading -> binding.rvComments.visibility = View.GONE
                is CommentUiState.Success -> {
                    binding.rvComments.visibility = View.VISIBLE
                    commentAdapter.submitList(state.comments)
                }
                is CommentUiState.Error -> {
                    binding.rvComments.visibility = View.VISIBLE
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun observeSendResult() {
        viewModel.sendResult.observe(viewLifecycleOwner) { result ->
            when {
                result == null -> { /* success */ }
                result != "CLEARED" -> Toast.makeText(requireContext(), result, Toast.LENGTH_SHORT).show()
            }
            if (result != null) viewModel.clearSendResult()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "QuickCommentFragment"

        fun newInstance(postId: String) = QuickCommentFragment().apply {
            arguments = Bundle().apply { putString("POST_ID", postId) }
        }
    }
}

private class CommentListAdapter(private val onReplyClicked: (Comment) -> Unit) :
    ListAdapter<Comment, CommentListAdapter.ViewHolder>(DiffCallback()) {

    private val commentAuthorMap = mutableMapOf<String, String>()
    private var density = -1f

    override fun submitList(list: List<Comment>?) {
        if (list == null || list.isEmpty()) {
            commentAuthorMap.clear()
        } else {
            list.forEach { commentAuthorMap[it.commentId] = it.authorName }
        }
        super.submitList(list)
    }

    inner class ViewHolder(private val binding: ItemCommentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(comment: Comment) {
            binding.tvUserName.text = comment.authorName
            binding.tvComment.text = comment.content
            binding.tvTime.text = comment.createdAt

            val rootLayout = binding.commentRootLayout

            if (comment.parentId != null) {
                rootLayout.setPadding(
                    (36 * density).toInt(),
                    rootLayout.paddingTop,
                    rootLayout.paddingRight,
                    rootLayout.paddingBottom
                )
                binding.btnReply.visibility = View.GONE

                val parentName = commentAuthorMap[comment.parentId]
                if (parentName != null) {
                    binding.tvReplyLabel.text = "↩ Phản hồi $parentName"
                    binding.tvReplyLabel.visibility = View.VISIBLE
                } else {
                    binding.tvReplyLabel.visibility = View.GONE
                }
            } else {
                rootLayout.setPadding(
                    0,
                    rootLayout.paddingTop,
                    rootLayout.paddingRight,
                    rootLayout.paddingBottom
                )
                binding.btnReply.visibility = View.VISIBLE
                binding.tvReplyLabel.visibility = View.GONE
            }

            binding.btnReply.setOnClickListener {
                onReplyClicked(comment)
            }

            Glide.with(binding.ivAvatar)
                .load(comment.authorAvatarUrl)
                .placeholder(R.drawable.ic_person_placeholder)
                .error(R.drawable.ic_person_placeholder)
                .circleCrop()
                .into(binding.ivAvatar)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        if (density < 0) {
            density = parent.context.resources.displayMetrics.density
        }
        val binding = ItemCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    private class DiffCallback : DiffUtil.ItemCallback<Comment>() {
        override fun areItemsTheSame(oldItem: Comment, newItem: Comment) =
            oldItem.commentId == newItem.commentId

        override fun areContentsTheSame(oldItem: Comment, newItem: Comment) =
            oldItem == newItem
    }
}

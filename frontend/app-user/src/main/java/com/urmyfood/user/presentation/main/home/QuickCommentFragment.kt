package com.urmyfood.user.presentation.main.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
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
import com.urmyfood.user.R
import com.urmyfood.user.databinding.ItemCommentBinding
import com.urmyfood.user.databinding.LayoutQuickCommentSheetBinding
import com.urmyfood.user.di.ServiceLocator
import com.urmyfood.user.domain.model.Comment

class QuickCommentFragment : BottomSheetDialogFragment() {

    private var _binding: LayoutQuickCommentSheetBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CommentViewModel by viewModels {
        ServiceLocator.provideCommentViewModelFactory()
    }

    private val commentAdapter = CommentListAdapter()
    private lateinit var postId: String

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
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                    val lm = rv.layoutManager as? LinearLayoutManager ?: return
                    val lastVisible = lm.findLastVisibleItemPosition()
                    if (lastVisible >= (rv.adapter?.itemCount ?: 0) - 3) {
                        viewModel.loadMore(postId)
                    }
                }
            })
        }
    }

    private fun setupClickListeners() {
        binding.btnSend.setOnClickListener {
            val text = binding.etComment.text.toString().trim()
            if (text.isNotBlank()) {
                viewModel.postComment(postId, text)
            }
        }
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
                result == null -> binding.etComment.text.clear()
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

private class CommentListAdapter :
    ListAdapter<Comment, CommentListAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(private val binding: ItemCommentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(comment: Comment) {
            binding.tvUserName.text = comment.authorName
            binding.tvComment.text = comment.content
            binding.tvTime.text = comment.createdAt
            Glide.with(binding.ivAvatar)
                .load(comment.authorAvatarUrl)
                .placeholder(R.drawable.ic_person_placeholder)
                .error(R.drawable.ic_person_placeholder)
                .circleCrop()
                .into(binding.ivAvatar)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
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

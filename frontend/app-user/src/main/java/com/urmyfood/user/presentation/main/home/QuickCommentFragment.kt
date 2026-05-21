package com.urmyfood.user.presentation.main.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.urmyfood.user.databinding.LayoutQuickCommentSheetBinding
import com.urmyfood.user.di.ServiceLocator

class QuickCommentFragment : BottomSheetDialogFragment() {

    private var _binding: LayoutQuickCommentSheetBinding? = null
    private val binding get() = _binding!!

    private val postId: String get() = requireArguments().getString(ARG_POST_ID)!!

    private val viewModel: CommentViewModel by viewModels {
        ServiceLocator.provideCommentViewModelFactory()
    }

    private val commentAdapter = CommentAdapter()

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

        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        setupRecyclerView()
        setupClickListeners()
        setupBottomSheetBehavior()
        observeViewModel()

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

    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is CommentViewModel.UiState.Loading -> Unit
                is CommentViewModel.UiState.Success -> {
                    commentAdapter.submitList(state.comments)
                    if (state.comments.isNotEmpty()) {
                        binding.rvComments.scrollToPosition(state.comments.size - 1)
                    }
                }
                is CommentViewModel.UiState.Error -> {
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
        viewModel.sendError.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnSend.setOnClickListener {
            val text = binding.etComment.text.toString().trim()
            if (text.isNotBlank()) {
                viewModel.sendComment(postId, text, "Bạn")
                binding.etComment.text.clear()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "QuickCommentFragment"
        private const val ARG_POST_ID = "post_id"

        fun newInstance(postId: String): QuickCommentFragment {
            return QuickCommentFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_POST_ID, postId)
                }
            }
        }
    }
}

package com.urmyfood.shop.presentation.main.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.urmyfood.shop.R
import com.urmyfood.shop.databinding.FragmentMainHomeBinding
import com.urmyfood.shop.presentation.common.safeNavigate
import com.urmyfood.shop.presentation.main.home.adapter.ActiveOrdersAdapter

import com.urmyfood.shop.presentation.main.MainContainerFragment

class HomeFragment : Fragment() {

    private var _binding: FragmentMainHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()

    private lateinit var activeOrdersAdapter: ActiveOrdersAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        activeOrdersAdapter = ActiveOrdersAdapter(
            onActionClick = { order ->
                viewModel.advanceOrderStatus(order.orderId)
            },
            onOrderClick = { order ->
                val args = Bundle().apply {
                    putString("orderId", order.orderId)
                }
                findNavController().safeNavigate(
                    R.id.action_home_to_orderDetail,
                    args
                )
            }
        )
        binding.rvActiveOrders.adapter = activeOrdersAdapter
    }

    private fun observeViewModel() {
        viewModel.salesAmount.observe(viewLifecycleOwner) { amount ->
            binding.tvSalesAmount.text = amount
        }

        viewModel.orderCount.observe(viewLifecycleOwner) { count ->
            binding.tvOrderCountLabel.text =
                getString(R.string.home_order_count_format, count)
        }

        viewModel.activeOrders.observe(viewLifecycleOwner) { orders ->
            activeOrdersAdapter.submitList(orders)
        }
    }

    private fun setupClickListeners() {
        // Create new post
        binding.btnCreatePost.setOnClickListener {
            findNavController().safeNavigate(R.id.action_home_to_createPost)
        }

        // View all orders → switch to Orders tab
        binding.tvViewAll.setOnClickListener {
            val mainContainer = requireParentFragment().parentFragment as? MainContainerFragment
            mainContainer?.view
                ?.findViewById<BottomNavigationView>(R.id.bottom_navigation)
                ?.selectedItemId = R.id.ordersFragment
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

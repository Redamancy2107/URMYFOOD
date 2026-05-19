package com.urmyfood.user.presentation.main.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.urmyfood.user.R
import com.urmyfood.user.data.local.CartManager
import com.urmyfood.user.databinding.LayoutOrderBottomSheetBinding
import com.urmyfood.user.domain.model.CartItem
import com.urmyfood.user.domain.model.FoodPost
import java.text.NumberFormat
import java.util.Locale

/**
 * Cửa sổ Đặt ngay (Order Bottom Sheet) trượt từ dưới lên.
 * Cho phép chọn option mức cay, tăng giảm số lượng, tính giá động và thêm vào giỏ hàng.
 */
class OrderBottomSheetFragment(private val post: FoodPost) : BottomSheetDialogFragment() {

    private var _binding: LayoutOrderBottomSheetBinding? = null
    private val binding get() = _binding!!

    private val currencyFormat = NumberFormat.getNumberInstance(Locale("vi", "VN"))
    private var quantity = 1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LayoutOrderBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupBottomSheetBehavior()
        bindFoodDetails()
        setupQuantityControls()
        setupClickListeners()
    }

    private fun setupBottomSheetBehavior() {
        val behavior = (dialog as? BottomSheetDialog)?.behavior
        behavior?.apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            isHideable = true
        }
    }

    private fun bindFoodDetails() {
        with(binding) {
            tvDishName.text = post.dishName
            tvShopName.text = post.shopName
            tvUnitPrice.text = "${currencyFormat.format(post.price)}đ"
            
            // Initial price calculate
            updateTotalPrice()

            // Load dish image
            Glide.with(ivFoodImage)
                .load(post.imageUrl)
                .placeholder(R.drawable.ic_image_placeholder)
                .error(R.drawable.bg_food_banner)
                .into(ivFoodImage)
        }
    }

    private fun setupQuantityControls() {
        with(binding) {
            tvQuantity.text = quantity.toString()

            btnPlus.setOnClickListener {
                val maxLimit = if (post.remainingQuantity > 0) post.remainingQuantity else 99
                if (quantity < maxLimit) {
                    quantity++
                    tvQuantity.text = quantity.toString()
                    updateTotalPrice()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Số lượng đạt giới hạn còn lại của cửa hàng ($maxLimit)!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            btnMinus.setOnClickListener {
                if (quantity > 1) {
                    quantity--
                    tvQuantity.text = quantity.toString()
                    updateTotalPrice()
                }
            }
        }
    }

    private fun updateTotalPrice() {
        val totalPrice = post.price * quantity
        binding.tvTotalPrice.text = "${currencyFormat.format(totalPrice)}đ"
    }

    private fun setupClickListeners() {
        binding.btnClose.setOnClickListener {
            dismiss()
        }

        binding.btnAddToCart.setOnClickListener {
            // Mock option selection - remove when BE ready
            val selectedOption = when (binding.cgSpicyOptions.checkedChipId) {
                R.id.chipSpicyMedium -> "Vị: Cay vừa"
                R.id.chipSpicyHigh -> "Vị: Cay nhiều"
                else -> "Vị: Không cay"
            }

            // Create CartItem with named parameters
            val cartItem = CartItem(
                postId = post.postId,
                dishName = post.dishName,
                price = post.price,
                imageUrl = post.imageUrl,
                shopName = post.shopName,
                quantity = quantity,
                selectedOption = selectedOption
            )

            // Save via CartManager
            CartManager(requireContext()).addCartItem(cartItem)

            Toast.makeText(
                requireContext(),
                "Đã thêm $quantity x ${post.dishName} vào giỏ hàng thành công!",
                Toast.LENGTH_SHORT
            ).show()

            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "OrderBottomSheetFragment"
    }
}

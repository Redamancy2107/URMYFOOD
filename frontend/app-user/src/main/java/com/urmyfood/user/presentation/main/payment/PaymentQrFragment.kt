package com.urmyfood.user.presentation.main.payment

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.urmyfood.user.R
import com.urmyfood.user.databinding.FragmentPaymentQrBinding
import com.urmyfood.user.di.ServiceLocator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.Locale

import com.urmyfood.user.domain.model.Result

class PaymentQrFragment : Fragment() {

    private var _binding: FragmentPaymentQrBinding? = null
    private val binding get() = _binding!!

    private var qrCodeString: String? = null
    private var amount: Long = 0
    private var orderId: String? = null

    private val getOrdersUseCase = ServiceLocator.getOrdersUseCase
    private var pollingJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPaymentQrBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        qrCodeString = arguments?.getString("qrCode")
        amount = arguments?.getLong("amount") ?: 0
        orderId = arguments?.getString("orderId")

        setupUI()
        startPollingPaymentStatus()
    }

    private fun setupUI() {
        val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
        binding.tvAmount.text = formatter.format(amount)

        binding.btnCancel.setOnClickListener {
            findNavController().popBackStack()
        }

        if (!qrCodeString.isNullOrBlank()) {
            val qrUrl = "https://quickchart.io/qr?text=" + Uri.encode(qrCodeString) + "&size=400"
            Glide.with(this)
                .load(qrUrl)
                .into(binding.ivQrCode)
            binding.pbQrLoading.visibility = View.GONE
        } else {
            binding.pbQrLoading.visibility = View.GONE
            Toast.makeText(requireContext(), "Không tìm thấy mã QR", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startPollingPaymentStatus() {
        if (orderId == null) return
        
        pollingJob = CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                delay(3000) // Poll every 3 seconds
                try {
                    val result = getOrdersUseCase()
                    if (result is Result.Success) {
                        val order = result.data.find { it.orderId == orderId }
                        if (order != null && order.paymentStatus == "PAID") {
                            withContext(Dispatchers.Main) {
                                handlePaymentSuccess()
                            }
                            break
                        }
                    }
                } catch (e: Exception) {
                    // Ignore errors during polling
                }
            }
        }
    }

    private fun handlePaymentSuccess() {
        Toast.makeText(requireContext(), "Thanh toán thành công!", Toast.LENGTH_LONG).show()
        // Navigate to Order History
        findNavController().navigate(R.id.orderHistoryFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        pollingJob?.cancel()
        _binding = null
    }
}

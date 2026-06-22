package com.urmyfood.user.presentation.main.payment

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
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

    private val checkPayOsStatusUseCase = ServiceLocator.checkPayOsStatusUseCase
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
            runCatching {
                binding.ivQrCode.setImageBitmap(createQrBitmap(qrCodeString.orEmpty()))
            }.onFailure {
                Toast.makeText(requireContext(), "Không thể hiển thị mã QR", Toast.LENGTH_SHORT).show()
            }
            binding.pbQrLoading.visibility = View.GONE
        } else {
            binding.pbQrLoading.visibility = View.GONE
            Toast.makeText(requireContext(), "Không tìm thấy mã QR", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createQrBitmap(payload: String): Bitmap {
        val matrix = QRCodeWriter().encode(payload, BarcodeFormat.QR_CODE, QR_SIZE, QR_SIZE)
        val bitmap = Bitmap.createBitmap(QR_SIZE, QR_SIZE, Bitmap.Config.RGB_565)
        val pixels = IntArray(QR_SIZE * QR_SIZE) { index ->
            val x = index % QR_SIZE
            val y = index / QR_SIZE
            if (matrix[x, y]) Color.BLACK else Color.WHITE
        }
        bitmap.setPixels(pixels, 0, QR_SIZE, 0, 0, QR_SIZE, QR_SIZE)
        return bitmap
    }

    private fun startPollingPaymentStatus() {
        if (orderId == null) return
        
        val currentOrderId = orderId ?: return
        pollingJob = CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                delay(3000) 
                try {
                    val result = checkPayOsStatusUseCase(currentOrderId)
                    if (result is Result.Success && result.data.paymentStatus == "PAID") {
                        withContext(Dispatchers.Main) {
                            handlePaymentSuccess()
                        }
                        break
                    }
                } catch (e: Exception) {
                    // Bỏ qua lỗi tạm thời khi polling
                }
            }
        }
    }

    private fun handlePaymentSuccess() {
        Toast.makeText(requireContext(), "Thanh toán thành công!", Toast.LENGTH_LONG).show()
        findNavController().navigate(R.id.orderHistoryFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        pollingJob?.cancel()
        _binding = null
    }

    companion object {
        private const val QR_SIZE = 400
    }
}

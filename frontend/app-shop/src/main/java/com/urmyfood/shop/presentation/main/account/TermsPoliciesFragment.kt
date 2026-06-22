package com.urmyfood.shop.presentation.main.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.urmyfood.shop.databinding.FragmentTermsPoliciesBinding
import com.urmyfood.shop.presentation.common.TermsDialogFragment

class TermsPoliciesFragment : Fragment() {

    private var _binding: FragmentTermsPoliciesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TermsPoliciesViewModel by viewModels {
        TermsPoliciesViewModel.Factory()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTermsPoliciesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.menuTermsUsage.setOnClickListener {
            showTermsDialog(null, null)
        }

        binding.menuPrivacyPolicy.setOnClickListener {
            val title = "Chính sách bảo mật"
            val content = """
                <b>1. Thu thập thông tin:</b> URMYFOOD thu thập các thông tin cá nhân của bạn như Họ tên, Số điện thoại, Địa chỉ giao hàng và Email khi bạn đăng ký tài khoản.<br><br>
                <b>2. Sử dụng thông tin:</b> Chúng tôi sử dụng thông tin thu thập được để xử lý đơn hàng, cung cấp dịch vụ giao đồ ăn, hỗ trợ khách hàng và cải thiện trải nghiệm người dùng trên ứng dụng.<br><br>
                <b>3. Bảo mật thông tin:</b> Dữ liệu của bạn được bảo mật tuyệt đối trên hệ thống máy chủ mã hóa của URMYFOOD. Chúng tôi cam kết không chia sẻ hoặc bán dữ liệu của bạn cho bất kỳ bên thứ ba nào.<br><br>
                <b>4. Quyền của khách hàng:</b> Bạn có quyền thay đổi thông tin cá nhân của mình bất cứ lúc nào trong mục chỉnh sửa hồ sơ hoặc yêu cầu xóa tài khoản vĩnh viễn.
            """.trimIndent()
            showTermsDialog(title, content)
        }

        binding.menuRefundPolicy.setOnClickListener {
            val title = "Chính sách hoàn tiền"
            val content = """
                <b>1. Điều kiện hoàn tiền:</b> Khách hàng sẽ được hoàn tiền 100% giá trị đơn hàng trong các trường hợp sau: Cửa hàng chuẩn bị sai món ăn so với mô tả, món ăn bị hư hỏng do quá trình vận chuyển, hoặc đơn hàng bị hủy bởi hệ thống do hết hàng.<br><br>
                <b>2. Thời hạn yêu cầu:</b> Vui lòng chụp ảnh món ăn lỗi và gửi yêu cầu hỗ trợ hoàn tiền qua ứng dụng trong vòng 30 phút kể từ thời điểm nhận hàng thành công.<br><br>
                <b>3. Thời gian xử lý:</b> Yêu cầu hoàn tiền sẽ được đội ngũ hỗ trợ duyệt trong vòng 24 giờ. Tiền sẽ được hoàn trả lại tài khoản ví điện tử hoặc tài khoản ngân hàng của bạn từ 2-5 ngày làm việc tùy thuộc vào ngân hàng liên kết.<br><br>
                <b>4. Trường hợp không hỗ trợ:</b> Chúng tôi không hỗ trợ hoàn tiền trong trường hợp tài xế không liên lạc được với bạn sau 3 cuộc gọi (mỗi cuộc gọi cách nhau 5 phút) dẫn đến việc đơn hàng phải hủy bỏ.
            """.trimIndent()
            showTermsDialog(title, content)
        }

        binding.menuRegulations.setOnClickListener {
            val title = "Quy chế hoạt động"
            val content = """
                <b>1. Quy định chung:</b> Ứng dụng URMYFOOD là nền tảng kết nối trực tuyến giữa Khách hàng, Cửa hàng đối tác và Tài xế giao hàng. Mọi thành viên tham gia hoạt động trên hệ thống phải tuân thủ pháp luật hiện hành và các điều khoản ứng xử văn minh của ứng dụng.<br><br>
                <b>2. Đối với khách hàng:</b> Khách hàng cần cung cấp thông tin liên hệ và địa chỉ chính xác, nhận hàng đúng giờ và thanh toán đầy đủ giá trị đơn hàng (đối với phương thức thanh toán tiền mặt COD).<br><br>
                <b>3. Đối với cửa hàng:</b> Các quán ăn đối tác cam kết đảm bảo vệ sinh an toàn thực phẩm, chế biến đúng định lượng và đúng thời gian chuẩn bị món ăn đã đăng tải.<br><br>
                <b>4. Giải quyết tranh chấp:</b> Mọi khiếu nại phát sinh giữa các bên sẽ được URMYFOOD đóng vai trò trung gian đứng ra phân tích dữ liệu hệ thống, hình ảnh minh chứng để đưa ra phán quyết công bằng nhất cho cả ba bên.
            """.trimIndent()
            showTermsDialog(title, content)
        }
    }

    private fun showTermsDialog(title: String?, content: String?) {
        val termsDialog = if (title != null && content != null) {
            TermsDialogFragment.newInstance(title, content)
        } else {
            TermsDialogFragment()
        }
        termsDialog.show(parentFragmentManager, TermsDialogFragment.TAG)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

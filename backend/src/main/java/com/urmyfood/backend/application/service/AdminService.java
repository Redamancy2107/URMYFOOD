package com.urmyfood.backend.application.service;

import com.urmyfood.backend.application.dto.*;
import com.urmyfood.backend.domain.model.*;
import com.urmyfood.backend.domain.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final AdminRepository adminRepository;
    private final AccountRepository accountRepository;
    private final PostRepository postRepository;
    private final VoucherRepository voucherRepository;
    private final ShopVerificationRepository shopVerificationRepository;
    private final ShopProfileRepository shopProfileRepository;
    private final ProfileImageStorageClient profileImageStorageClient;
    private final NamedParameterJdbcTemplate jdbc;

    public AdminProfileDto getAdminProfile(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found for id: " + accountId));
        
        Admin admin = adminRepository.findByAccountId(accountId)
                .orElseGet(() -> {
                    Admin emptyAdmin = new Admin();
                    emptyAdmin.setAccountId(accountId);
                    return emptyAdmin;
                });
                
        return mapToDto(admin, account);
    }

    @Transactional
    public AdminProfileDto updateAdminProfile(Long accountId, AdminProfileUpdateDto updates) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found for id: " + accountId));

        Admin admin = adminRepository.findByAccountId(accountId)
                .orElseGet(() -> {
                    // Create new if not exists
                    Admin newAdmin = new Admin();
                    newAdmin.setAccountId(accountId);
                    return newAdmin;
                });

        if (updates.getFullName() != null) account.setFullName(updates.getFullName());
        if (updates.getWorkEmail() != null) account.setEmail(updates.getWorkEmail());
        if (updates.getPhoneNumber() != null) account.setPhone(updates.getPhoneNumber());

        if (updates.getPosition() != null) admin.setPosition(updates.getPosition());
        if (updates.getShortBio() != null) admin.setShortBio(updates.getShortBio());

        accountRepository.save(account);
        Admin savedAdmin = adminRepository.save(admin);
        
        return mapToDto(savedAdmin, account);
    }

    private AdminProfileDto mapToDto(Admin admin, Account account) {
        return AdminProfileDto.builder()
                .id(admin.getId())
                .accountId(account.getId())
                .fullName(account.getFullName())
                .workEmail(account.getEmail())
                .phoneNumber(account.getPhone())
                .position(admin.getPosition())
                .shortBio(admin.getShortBio())
                .is2FaEnabled(admin.is2FaEnabled())
                .avatarUrl(account.getAvatarUrl())
                .build();
    }

    @Transactional
    public AdminProfileDto updateAdminAvatar(Long accountId, MultipartFile file) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản admin"));
        
        String oldAvatarUrl = account.getAvatarUrl();
        String newAvatarUrl = profileImageStorageClient.uploadAdminAvatar(accountId, file);
        
        account.setAvatarUrl(newAvatarUrl);
        accountRepository.save(account);

        if (oldAvatarUrl != null) {
            try {
                profileImageStorageClient.deleteAdminAvatar(accountId, oldAvatarUrl);
            } catch (Exception e) {
                log.warn("Failed to delete old admin avatar", e);
            }
        }

        Admin admin = adminRepository.findByAccountId(accountId)
                .orElseGet(() -> {
                    Admin emptyAdmin = new Admin();
                    emptyAdmin.setAccountId(accountId);
                    return emptyAdmin;
                });

        return mapToDto(admin, account);
    }

    public DashboardOverviewResponse getDashboardOverview() {
        BigDecimal totalRevenue = jdbc.queryForObject(
                "SELECT COALESCE(SUM(final_amount), 0) FROM orders WHERE order_status = 'COMPLETED'",
                new MapSqlParameterSource(), BigDecimal.class);

        Long newOrders = jdbc.queryForObject(
                "SELECT COUNT(*) FROM orders WHERE created_at >= CURRENT_DATE",
                new MapSqlParameterSource(), Long.class);

        Long newUsers = jdbc.queryForObject(
                "SELECT COUNT(*) FROM accounts WHERE created_at >= CURRENT_DATE AND role = 'CUSTOMER'",
                new MapSqlParameterSource(), Long.class);

        Long activeShops = jdbc.queryForObject(
                "SELECT COUNT(DISTINCT a.id) FROM accounts a JOIN shop_profiles sp ON sp.shop_id = a.id WHERE a.role = 'SHOP'",
                new MapSqlParameterSource(), Long.class);

        List<DashboardOverviewResponse.MonthlyRevenue> growth = jdbc.query(
                "SELECT EXTRACT(MONTH FROM created_at)::int as month, SUM(final_amount) as revenue " +
                "FROM orders " +
                "WHERE order_status = 'COMPLETED' AND EXTRACT(YEAR FROM created_at) = EXTRACT(YEAR FROM CURRENT_DATE) " +
                "GROUP BY month " +
                "ORDER BY month",
                new MapSqlParameterSource(),
                (rs, rowNum) -> new DashboardOverviewResponse.MonthlyRevenue(rs.getInt("month"), rs.getBigDecimal("revenue"))
        );

        List<DashboardOverviewResponse.RecentActivity> activities = jdbc.query(
                "(SELECT 'NEW_USER' as type, 'Người dùng ' || full_name || ' đăng ký tài khoản' as description, created_at FROM accounts WHERE role = 'CUSTOMER' ORDER BY created_at DESC LIMIT 5) " +
                "UNION ALL " +
                "(SELECT 'NEW_ORDER' as type, 'Đơn hàng mới trị giá ' || final_amount || 'đ được tạo' as description, created_at FROM orders ORDER BY created_at DESC LIMIT 5) " +
                "UNION ALL " +
                "(SELECT 'NEW_VERIFICATION' as type, 'Cửa hàng ' || shop_name || ' gửi yêu cầu xét duyệt' as description, created_at FROM shop_verifications ORDER BY created_at DESC LIMIT 5) " +
                "ORDER BY created_at DESC LIMIT 10",
                new MapSqlParameterSource(),
                (rs, rowNum) -> new DashboardOverviewResponse.RecentActivity(rs.getString("type"), rs.getString("description"), rs.getTimestamp("created_at").toInstant().toString())
        );

        List<DashboardOverviewResponse.LatestShop> latestShops = jdbc.query(
                "SELECT a.id, COALESCE(sv.shop_name, a.full_name) as shop_name, a.email, COALESCE(sv.status, 'NOT_SUBMITTED') as status, a.created_at " +
                "FROM accounts a " +
                "LEFT JOIN shop_verifications sv ON sv.shop_id = a.id " +
                "WHERE a.role = 'SHOP' " +
                "ORDER BY a.created_at DESC LIMIT 5",
                new MapSqlParameterSource(),
                (rs, rowNum) -> new DashboardOverviewResponse.LatestShop(
                        rs.getLong("id"),
                        rs.getString("shop_name"),
                        rs.getString("email"),
                        rs.getString("status"),
                        rs.getTimestamp("created_at").toInstant().toString()
                )
        );

        return DashboardOverviewResponse.builder()
                .totalRevenue(totalRevenue)
                .newOrders(newOrders != null ? newOrders : 0)
                .newUsers(newUsers != null ? newUsers : 0)
                .activeShops(activeShops != null ? activeShops : 0)
                .monthlyRevenueGrowth(growth)
                .recentActivities(activities)
                .latestShops(latestShops)
                .build();
    }

    public List<ShopVerificationResponse> getPendingShopVerifications() {
        return shopVerificationRepository.findPending().stream()
                .map(v -> ShopVerificationResponse.builder()
                        .id(v.getId())
                        .shopId(v.getShop().getId())
                        .shopName(v.getShopName())
                        .category(v.getCategory())
                        .address(v.getAddress())
                        .latitude(v.getLatitude())
                        .longitude(v.getLongitude())
                        .cccdFrontUrl(v.getCccdFrontUrl())
                        .cccdBackUrl(v.getCccdBackUrl())
                        .shopPhotoUrls(v.getShopPhotoUrls())
                        .status(v.getStatus().name())
                        .rejectReason(v.getRejectReason())
                        .createdAt(v.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public void approveShopVerification(Long id) {
        ShopVerification verification = shopVerificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy yêu cầu xét duyệt"));
        verification.setStatus(ShopVerificationStatus.APPROVED);
        shopVerificationRepository.save(verification);

        Account shop = accountRepository.findById(verification.getShop().getId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản cửa hàng"));
        if (!"SHOP".equals(shop.getRole())) {
            shop.setRole("SHOP");
            accountRepository.save(shop);
        }

        Optional<ShopProfile> profileOpt = shopProfileRepository.findByShopId(shop.getId());
        if (profileOpt.isEmpty()) {
            ShopProfile newProfile = ShopProfile.builder()
                    .shop(shop)
                    .shopName(verification.getShopName())
                    .category(verification.getCategory())
                    .address(verification.getAddress())
                    .latitude(verification.getLatitude())
                    .longitude(verification.getLongitude())
                    .isOpen(true)
                    .openingHours("08:00 - 22:00")
                    .build();
            shopProfileRepository.save(newProfile);
        }
    }

    @Transactional
    public void rejectShopVerification(Long id, String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Lý do từ chối không được để trống");
        }
        ShopVerification verification = shopVerificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy yêu cầu xét duyệt"));
        verification.setStatus(ShopVerificationStatus.REJECTED);
        verification.setRejectReason(reason.trim());
        shopVerificationRepository.save(verification);
    }

    public PageResponse<AccountProfileDto> getAllAccounts(int page, int size, String role) {
        List<Account> accounts = accountRepository.findAll(page, size, role);
        long total = accountRepository.count(role);
        List<AccountProfileDto> content = accounts.stream()
                .map(a -> AccountProfileDto.builder()
                        .id(a.getId())
                        .fullName(a.getFullName())
                        .email(a.getEmail())
                        .phone(a.getPhone())
                        .role(a.getRole())
                        .avatarUrl(a.getAvatarUrl())
                        .isActive(a.isActive())
                        .build())
                .collect(Collectors.toList());
        return PageResponse.ofAnchored(content, page, size, total, null);
    }

    @Transactional
    public void lockUnlockAccount(Long id, boolean active) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản"));
        account.setActive(active);
        accountRepository.save(account);
    }

    public List<VoucherDto> getAllVouchers() {
        return voucherRepository.findAll().stream()
                .filter(Voucher::isActive)
                .map(v -> VoucherDto.builder()
                        .id(v.getId())
                        .code(v.getCode())
                        .title(v.getTitle())
                        .description(v.getDescription())
                        .discountValue(v.getDiscountValue())
                        .minOrderValue(v.getMinOrderValue())
                        .expiryDate(v.getExpiryDate())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public VoucherDto createVoucher(VoucherDto dto) {
        if (voucherRepository.findByCode(dto.getCode().trim().toUpperCase()).isPresent()) {
            throw new IllegalArgumentException("Mã giảm giá này đã tồn tại");
        }
        Voucher v = Voucher.builder()
                .code(dto.getCode().trim().toUpperCase())
                .title(dto.getTitle().trim())
                .description(dto.getDescription())
                .discountValue(dto.getDiscountValue())
                .minOrderValue(dto.getMinOrderValue())
                .expiryDate(dto.getExpiryDate())
                .isActive(true)
                .build();
        Voucher saved = voucherRepository.save(v);
        return VoucherDto.builder()
                .id(saved.getId())
                .code(saved.getCode())
                .title(saved.getTitle())
                .description(saved.getDescription())
                .discountValue(saved.getDiscountValue())
                .minOrderValue(saved.getMinOrderValue())
                .expiryDate(saved.getExpiryDate())
                .build();
    }

    @Transactional
    public void deleteVoucher(Long id) {
        voucherRepository.findById(id).ifPresent(v -> {
            v.setActive(false);
            voucherRepository.save(v);
        });
    }

    public PageResponse<PostResponse> getAllPosts(int page, int size) {
        List<Post> posts = postRepository.findAll(page, size);
        long total = postRepository.countAll();
        List<PostResponse> content = posts.stream()
                .map(p -> PostResponse.builder()
                        .postId(p.getPostId())
                        .dishName(p.getDishName())
                        .price(p.getPrice())
                        .originalPrice(p.getOriginalPrice())
                        .maxQuantity(p.getMaxQuantity())
                        .remainingQuantity(p.getRemainingQuantity())
                        .endTime(p.getEndTime())
                        .isFlashSale(p.isFlashSale())
                        .status(p.getStatus().name())
                        .content(p.getContent())
                        .imageUrl(p.getImageUrl())
                        .category(p.getCategory())
                        .shopAccountId(p.getAuthor().getId())
                        .shopName(p.getAuthor().getFullName())
                        .shopAvatarUrl(p.getAuthor().getAvatarUrl())
                        .createdAt(p.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
        return PageResponse.ofAnchored(content, page, size, total, null);
    }

    @Transactional
    public void moderatePostStatus(UUID postId, String statusStr) {
        PostStatus status;
        try {
            status = PostStatus.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Trạng thái bài viết không hợp lệ");
        }
        postRepository.adminUpdatePostStatus(postId, status);
    }

    @Transactional
    public void moderateDeletePost(UUID postId) {
        postRepository.adminDeletePost(postId);
    }

    public List<StoreReportItem> getStoreReports() {
        String sql = "SELECT a.id, COALESCE(sp.shop_name, a.full_name) as shop_name, a.email, a.phone, " +
                     "COALESCE(SUM(CASE WHEN o.order_status = 'COMPLETED' THEN o.final_amount ELSE 0 END), 0) as total_revenue, " +
                     "COALESCE(COUNT(o.order_id), 0) as total_orders, " +
                     "COALESCE(COUNT(CASE WHEN o.order_status = 'COMPLETED' THEN 1 END), 0) as completed_orders, " +
                     "COALESCE(COUNT(CASE WHEN o.order_status = 'CANCELLED' THEN 1 END), 0) as cancelled_orders " +
                     "FROM accounts a " +
                     "LEFT JOIN shop_profiles sp ON sp.shop_id = a.id " +
                     "LEFT JOIN orders o ON o.shop_id = a.id " +
                     "WHERE a.role = 'SHOP' " +
                     "GROUP BY a.id, sp.shop_name, a.email, a.phone " +
                     "ORDER BY total_revenue DESC";
        return jdbc.query(sql, new MapSqlParameterSource(),
                (rs, rowNum) -> StoreReportItem.builder()
                        .id(rs.getLong("id"))
                        .shopName(rs.getString("shop_name"))
                        .email(rs.getString("email"))
                        .phone(rs.getString("phone"))
                        .totalRevenue(rs.getBigDecimal("total_revenue"))
                        .totalOrders(rs.getLong("total_orders"))
                        .completedOrders(rs.getLong("completed_orders"))
                        .cancelledOrders(rs.getLong("cancelled_orders"))
                        .build());
    }

    public List<CustomerReportItem> getCustomerReports() {
        String sql = "SELECT a.id, a.full_name, a.email, a.phone, " +
                     "COALESCE(SUM(CASE WHEN o.order_status = 'COMPLETED' THEN o.final_amount ELSE 0 END), 0) as total_spent, " +
                     "COALESCE(COUNT(o.order_id), 0) as total_orders, " +
                     "COALESCE(COUNT(CASE WHEN o.order_status = 'COMPLETED' THEN 1 END), 0) as completed_orders, " +
                     "a.created_at " +
                     "FROM accounts a " +
                     "LEFT JOIN orders o ON o.customer_id = a.id " +
                     "WHERE a.role = 'CUSTOMER' " +
                     "GROUP BY a.id, a.full_name, a.email, a.phone, a.created_at " +
                     "ORDER BY total_spent DESC";
        return jdbc.query(sql, new MapSqlParameterSource(),
                (rs, rowNum) -> CustomerReportItem.builder()
                        .id(rs.getLong("id"))
                        .fullName(rs.getString("full_name")) 
                        .email(rs.getString("email"))
                        .phone(rs.getString("phone"))
                        .totalSpent(rs.getBigDecimal("total_spent"))
                        .totalOrders(rs.getLong("total_orders"))
                        .completedOrders(rs.getLong("completed_orders"))
                        .createdAt(rs.getTimestamp("created_at").toInstant().toString())
                        .build());
    }

    public String exportStoreReportsToCsv() {
        List<StoreReportItem> items = getStoreReports();
        StringBuilder csv = new StringBuilder();
        csv.append("ID,Ten cua hang,Email,So dien thoai,Tong doanh thu,Tong don hang,Don hoan thanh,Don huy\n");
        for (StoreReportItem item : items) {
            csv.append(item.getId()).append(",")
               .append("\"").append(item.getShopName() != null ? item.getShopName().replace("\"", "\"\"") : "").append("\",")
               .append("\"").append(item.getEmail() != null ? item.getEmail().replace("\"", "\"\"") : "").append("\",")
               .append("\"").append(item.getPhone() != null ? item.getPhone().replace("\"", "\"\"") : "").append("\",")
               .append(item.getTotalRevenue()).append(",")
               .append(item.getTotalOrders()).append(",")
               .append(item.getCompletedOrders()).append(",")
               .append(item.getCancelledOrders()).append("\n");
        }
        return csv.toString();
    }

    public String exportCustomerReportsToCsv() {
        List<CustomerReportItem> items = getCustomerReports();
        StringBuilder csv = new StringBuilder();
        csv.append("ID,Ho va ten,Email,So dien thoai,Tong chi tieu,Tong don hang,Don hoan thanh,Ngay tham gia\n");
        for (CustomerReportItem item : items) {
            csv.append(item.getId()).append(",")
               .append("\"").append(item.getFullName() != null ? item.getFullName().replace("\"", "\"\"") : "").append("\",")
               .append("\"").append(item.getEmail() != null ? item.getEmail().replace("\"", "\"\"") : "").append("\",")
               .append("\"").append(item.getPhone() != null ? item.getPhone().replace("\"", "\"\"") : "").append("\",")
               .append(item.getTotalSpent()).append(",")
               .append(item.getTotalOrders()).append(",")
               .append(item.getCompletedOrders()).append(",")
               .append("\"").append(item.getCreatedAt()).append("\"\n");
        }
        return csv.toString();
    }
}

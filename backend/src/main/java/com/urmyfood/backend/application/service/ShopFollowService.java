package com.urmyfood.backend.application.service;

import com.urmyfood.backend.application.dto.ShopFollowResponse;
import com.urmyfood.backend.domain.model.Account;
import com.urmyfood.backend.domain.repository.AccountRepository;
import com.urmyfood.backend.domain.repository.ShopFollowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShopFollowService {

    private static final String ROLE_CUSTOMER = "CUSTOMER";
    private static final String ROLE_SHOP = "SHOP";

    private final AccountRepository accountRepository;
    private final ShopFollowRepository shopFollowRepository;

    @Transactional(readOnly = true)
    public ShopFollowResponse getFollowState(Long customerId, Long shopId) {
        requireCustomer(customerId);
        requireShop(shopId);
        return response(customerId, shopId);
    }

    @Transactional
    public ShopFollowResponse follow(Long customerId, Long shopId) {
        requireCustomer(customerId);
        requireShop(shopId);
        if (customerId.equals(shopId)) {
            throw new IllegalArgumentException("Khong the theo doi chinh minh");
        }
        shopFollowRepository.follow(customerId, shopId);
        return response(customerId, shopId);
    }

    @Transactional
    public ShopFollowResponse unfollow(Long customerId, Long shopId) {
        requireCustomer(customerId);
        requireShop(shopId);
        shopFollowRepository.unfollow(customerId, shopId);
        return response(customerId, shopId);
    }

    private ShopFollowResponse response(Long customerId, Long shopId) {
        return ShopFollowResponse.builder()
                .shopId(shopId)
                .isFollowing(shopFollowRepository.isFollowing(customerId, shopId))
                .followerCount(shopFollowRepository.countByShopId(shopId))
                .build();
    }

    private Account requireCustomer(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay tai khoan"));
        if (!ROLE_CUSTOMER.equals(account.getRole())) {
            throw new AccessDeniedException("Chi tai khoan khach hang moi duoc theo doi shop");
        }
        return account;
    }

    private Account requireShop(Long shopId) {
        Account account = accountRepository.findById(shopId)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay shop"));
        if (!ROLE_SHOP.equals(account.getRole())) {
            throw new IllegalArgumentException("Tai khoan nay khong phai shop");
        }
        return account;
    }
}

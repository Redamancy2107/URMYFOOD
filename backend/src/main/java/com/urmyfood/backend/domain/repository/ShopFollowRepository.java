package com.urmyfood.backend.domain.repository;

public interface ShopFollowRepository {
    void follow(Long customerId, Long shopId);
    void unfollow(Long customerId, Long shopId);
    boolean isFollowing(Long customerId, Long shopId);
    long countByShopId(Long shopId);
}

package com.urmyfood.backend.application.service;

import com.urmyfood.backend.domain.model.ShopProfileImageType;
import org.springframework.web.multipart.MultipartFile;

public interface ProfileImageStorageClient {
    String uploadShopProfileImage(Long shopId, ShopProfileImageType type, MultipartFile file);

    void deleteShopProfileImage(Long shopId, String imageUrl);

    String uploadAdminAvatar(Long accountId, MultipartFile file);

    void deleteAdminAvatar(Long accountId, String imageUrl);

    String uploadUserAvatar(Long accountId, MultipartFile file);

    void deleteUserAvatar(Long accountId, String imageUrl);
}

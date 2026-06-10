package com.urmyfood.backend.application.service;

import com.urmyfood.backend.domain.model.ShopProfileImageType;
import org.springframework.web.multipart.MultipartFile;

public interface ProfileImageStorageClient {
    String uploadShopProfileImage(Long shopId, ShopProfileImageType type, MultipartFile file);

    void deleteShopProfileImage(Long shopId, String imageUrl);
}

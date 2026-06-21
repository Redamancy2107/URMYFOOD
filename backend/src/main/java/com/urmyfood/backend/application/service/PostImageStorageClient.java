package com.urmyfood.backend.application.service;

import org.springframework.web.multipart.MultipartFile;

public interface PostImageStorageClient {
    String uploadPostImage(Long shopId, MultipartFile file);
}

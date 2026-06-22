package com.urmyfood.backend.application.service;

import org.springframework.web.multipart.MultipartFile;

public interface ChatImageStorageClient {
    String uploadChatImage(Long sessionId, MultipartFile file);
}

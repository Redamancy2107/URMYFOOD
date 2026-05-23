package com.urmyfood.backend.presentation.controller;

import com.urmyfood.backend.application.dto.ApiResponse;
import com.urmyfood.backend.application.dto.LikeToggleResult;
import com.urmyfood.backend.application.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/posts/{postId}/like")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    @PostMapping
    public ResponseEntity<ApiResponse<LikeToggleResult>> likePost(@PathVariable UUID postId) {
        LikeToggleResult result = likeService.toggleLike(postId);
        String message = result.isLiked() ? "Đã thích bài viết" : "Đã bỏ thích bài viết";
        return ResponseEntity.ok(ApiResponse.success(message, result));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<LikeToggleResult>> unlikePost(@PathVariable UUID postId) {
        LikeToggleResult result = likeService.toggleLike(postId);
        String message = result.isLiked() ? "Đã thích bài viết" : "Đã bỏ thích bài viết";
        return ResponseEntity.ok(ApiResponse.success(message, result));
    }
}

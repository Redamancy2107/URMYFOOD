package com.urmyfood.backend.presentation.controller;

import com.urmyfood.backend.application.dto.ApiResponse;
import com.urmyfood.backend.application.dto.CommentResponse;
import com.urmyfood.backend.application.dto.CreateCommentRequest;
import com.urmyfood.backend.application.dto.CreatePostRequest;
import com.urmyfood.backend.application.dto.LikeToggleResponse;
import com.urmyfood.backend.application.dto.PageResponse;
import com.urmyfood.backend.application.dto.PostResponse;
import com.urmyfood.backend.application.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PostResponse>>> getNewsfeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageResponse<PostResponse> result = postService.getNewsfeed(page, size);
        return ResponseEntity.ok(ApiResponse.success("Tải bài viết thành công", result));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<PostResponse>>> searchPosts(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageResponse<PostResponse> result = postService.searchPosts(query, page, size);
        return ResponseEntity.ok(ApiResponse.success("Tìm kiếm thành công", result));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PostResponse>> createPost(@Valid @RequestBody CreatePostRequest request) {
        PostResponse response = postService.createPost(request);
        return ResponseEntity.ok(ApiResponse.success("Tạo bài viết thành công", response));
    }

    @PostMapping("/{postId}/like")
    public ResponseEntity<ApiResponse<LikeToggleResponse>> likePost(@PathVariable UUID postId) {
        LikeToggleResponse response = postService.likePost(postId);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật lượt thích thành công", response));
    }

    @DeleteMapping("/{postId}/like")
    public ResponseEntity<ApiResponse<LikeToggleResponse>> unlikePost(@PathVariable UUID postId) {
        LikeToggleResponse response = postService.unlikePost(postId);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật lượt thích thành công", response));
    }

    @GetMapping("/{postId}/comments")
    public ResponseEntity<ApiResponse<PageResponse<CommentResponse>>> getComments(
            @PathVariable UUID postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageResponse<CommentResponse> response = postService.getComments(postId, page, size);
        return ResponseEntity.ok(ApiResponse.success("Tải bình luận thành công", response));
    }

    @PostMapping("/{postId}/comments")
    public ResponseEntity<ApiResponse<CommentResponse>> createComment(
            @PathVariable UUID postId,
            @Valid @RequestBody CreateCommentRequest request
    ) {
        CommentResponse response = postService.createComment(postId, request);
        return ResponseEntity.ok(ApiResponse.success("Đăng bình luận thành công", response));
    }
}

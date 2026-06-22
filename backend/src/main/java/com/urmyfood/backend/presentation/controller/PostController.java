package com.urmyfood.backend.presentation.controller;

import com.urmyfood.backend.application.dto.ApiResponse;
import com.urmyfood.backend.application.dto.CommentResponse;
import com.urmyfood.backend.application.dto.CreateCommentRequest;
import com.urmyfood.backend.application.dto.CreatePostRequest;
import com.urmyfood.backend.application.dto.LikeToggleResponse;
import com.urmyfood.backend.application.dto.PageResponse;
import com.urmyfood.backend.application.dto.PostImageUploadResponse;
import com.urmyfood.backend.application.dto.PostResponse;
import com.urmyfood.backend.application.dto.SavedPostResponse;
import com.urmyfood.backend.application.dto.UpdatePostRequest;
import com.urmyfood.backend.application.dto.UpdatePostStatusRequest;
import com.urmyfood.backend.application.dto.UpdateRemainingQuantityRequest;
import com.urmyfood.backend.application.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PostResponse>>> getNewsfeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String anchor
    ) {
        OffsetDateTime anchorDt = anchor != null ? OffsetDateTime.parse(anchor) : null;
        PageResponse<PostResponse> result = postService.getNewsfeed(page, size, anchorDt);
        return ResponseEntity.ok(ApiResponse.success("Tải bài viết thành công", result));
    }

    @GetMapping("/mine")
    public ResponseEntity<ApiResponse<PageResponse<PostResponse>>> getMyPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageResponse<PostResponse> result = postService.getMyPosts(page, size);
        return ResponseEntity.ok(ApiResponse.success("Tải bài viết của bạn thành công", result));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<PostResponse>>> searchPosts(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String anchor
    ) {
        OffsetDateTime anchorDt = anchor != null ? OffsetDateTime.parse(anchor) : null;
        PageResponse<PostResponse> result = postService.searchPosts(query, page, size, anchorDt);
        return ResponseEntity.ok(ApiResponse.success("Tìm kiếm thành công", result));
    }

    @GetMapping("/saved")
    public ResponseEntity<ApiResponse<PageResponse<PostResponse>>> getSavedPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageResponse<PostResponse> result = postService.getSavedPosts(page, size);
        return ResponseEntity.ok(ApiResponse.success("Saved posts loaded", result));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostResponse>> getPost(@PathVariable UUID postId) {
        PostResponse response = postService.getPost(postId);
        return ResponseEntity.ok(ApiResponse.success("Tải bài viết thành công", response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PostResponse>> createPost(@Valid @RequestBody CreatePostRequest request) {
        PostResponse response = postService.createPost(request);
        return ResponseEntity.ok(ApiResponse.success("Tạo bài viết thành công", response));
    }

    @PutMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostResponse>> updatePost(
            @PathVariable UUID postId,
            @Valid @RequestBody UpdatePostRequest request
    ) {
        PostResponse response = postService.updatePost(postId, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật bài viết thành công", response));
    }

    @PatchMapping("/{postId}/remaining-quantity")
    public ResponseEntity<ApiResponse<PostResponse>> updateRemainingQuantity(
            @PathVariable UUID postId,
            @Valid @RequestBody UpdateRemainingQuantityRequest request
    ) {
        PostResponse response = postService.updateRemainingQuantity(postId, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật số suất còn lại thành công", response));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse<Void>> deletePost(@PathVariable UUID postId) {
        postService.deletePost(postId);
        return ResponseEntity.ok(ApiResponse.success("Xóa bài viết thành công", null));
    }

    @PatchMapping("/{postId}/status")
    public ResponseEntity<ApiResponse<PostResponse>> updatePostStatus(
            @PathVariable UUID postId,
            @Valid @RequestBody UpdatePostStatusRequest request
    ) {
        PostResponse response = postService.updatePostStatus(postId, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái thành công", response));
    }

    @PostMapping("/images")
    public ResponseEntity<ApiResponse<PostImageUploadResponse>> uploadPostImage(
            @RequestParam("file") MultipartFile file
    ) {
        PostImageUploadResponse response = postService.uploadPostImage(file);
        return ResponseEntity.ok(ApiResponse.success("Tải ảnh lên thành công", response));
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

    @GetMapping("/{postId}/saved")
    public ResponseEntity<ApiResponse<SavedPostResponse>> getSavedState(@PathVariable UUID postId) {
        SavedPostResponse response = postService.getSavedState(postId);
        return ResponseEntity.ok(ApiResponse.success("Saved state loaded", response));
    }

    @PostMapping("/{postId}/saved")
    public ResponseEntity<ApiResponse<SavedPostResponse>> savePost(@PathVariable UUID postId) {
        SavedPostResponse response = postService.savePost(postId);
        return ResponseEntity.ok(ApiResponse.success("Post saved", response));
    }

    @DeleteMapping("/{postId}/saved")
    public ResponseEntity<ApiResponse<SavedPostResponse>> unsavePost(@PathVariable UUID postId) {
        SavedPostResponse response = postService.unsavePost(postId);
        return ResponseEntity.ok(ApiResponse.success("Post unsaved", response));
    }

    @GetMapping("/{postId}/comments")
    public ResponseEntity<ApiResponse<PageResponse<CommentResponse>>> getComments(
            @PathVariable UUID postId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageResponse<CommentResponse> response = postService.getComments(postId, cursor, size);
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

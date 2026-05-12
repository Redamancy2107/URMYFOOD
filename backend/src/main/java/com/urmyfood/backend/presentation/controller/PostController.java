package com.urmyfood.backend.presentation.controller;

import com.urmyfood.backend.application.dto.ApiResponse;
import com.urmyfood.backend.application.dto.CreatePostRequest;
import com.urmyfood.backend.application.dto.PostResponse;
import com.urmyfood.backend.application.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PostResponse>>> getNewsfeed() {
        List<PostResponse> posts = postService.getNewsfeed();
        return ResponseEntity.ok(ApiResponse.success("Tải bài viết thành công", posts));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PostResponse>> createPost(@Valid @RequestBody CreatePostRequest request) {
        PostResponse response = postService.createPost(request);
        return ResponseEntity.ok(ApiResponse.success("Tạo bài viết thành công", response));
    }
}

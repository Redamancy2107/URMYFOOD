package com.urmyfood.backend.infrastructure.storage;

import com.urmyfood.backend.application.service.PostImageStorageClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class SupabasePostImageStorageClient implements PostImageStorageClient {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    @Value("${supabase.url:}")
    private String supabaseUrl;

    @Value("${supabase.anon-key:}")
    private String anonKey;

    @Value("${supabase.storage.bucket:}")
    private String bucket;

    @Value("${supabase.storage.profile-image-max-size:5242880}")
    private long maxSize;

    private final RestClient.Builder restClientBuilder;

    @Override
    public String uploadPostImage(Long shopId, MultipartFile file) {
        validateConfig();
        validateFile(file);

        String ext = toExtension(file.getContentType());
        String objectPath = "post-images/%d/%s.%s".formatted(shopId, UUID.randomUUID(), ext);
        String uploadUrl = normalizeUrl(supabaseUrl) + "/storage/v1/object/" + bucket + "/" + objectPath;

        try {
            restClientBuilder.build()
                    .post()
                    .uri(uploadUrl)
                    .header("apikey", anonKey)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + anonKey)
                    .contentType(MediaType.parseMediaType(file.getContentType()))
                    .body(file.getBytes())
                    .retrieve()
                    .toBodilessEntity();
        } catch (IOException e) {
            throw new IllegalArgumentException("Không thể đọc file ảnh");
        } catch (RestClientResponseException e) {
            log.warn("Supabase post image upload failed. status={}, body={}", e.getStatusCode().value(),
                    sanitize(e.getResponseBodyAsString()));
            throw new IllegalStateException(toUserMessage(e));
        } catch (Exception e) {
            log.warn("Supabase post image upload failed unexpectedly", e);
            throw new IllegalStateException("Không thể tải ảnh lên Supabase Storage");
        }

        return normalizeUrl(supabaseUrl) + "/storage/v1/object/public/" + bucket + "/" + objectPath;
    }

    private void validateConfig() {
        if (isBlank(supabaseUrl) || isBlank(anonKey) || isBlank(bucket)) {
            throw new IllegalStateException("Thiếu cấu hình Supabase Storage");
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File ảnh không được để trống");
        }
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("File ảnh vượt quá dung lượng cho phép");
        }
        String ct = file.getContentType();
        if (ct == null || !ALLOWED_CONTENT_TYPES.contains(ct.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("Định dạng ảnh không hợp lệ");
        }
    }

    private String toExtension(String contentType) {
        return switch (contentType.toLowerCase(Locale.ROOT)) {
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            default -> "jpg";
        };
    }

    private String normalizeUrl(String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    private String toUserMessage(RestClientResponseException e) {
        return switch (e.getStatusCode().value()) {
            case 401, 403 -> "Supabase Storage từ chối upload. Vui lòng kiểm tra SUPABASE_ANON_KEY hoặc policy của bucket";
            case 404 -> "Không tìm thấy bucket Supabase Storage";
            case 413 -> "File ảnh vượt quá dung lượng Supabase cho phép";
            default -> "Không thể tải ảnh lên Supabase Storage (HTTP " + e.getStatusCode().value() + ")";
        };
    }

    private String sanitize(String body) {
        if (body == null || body.isBlank()) return "";
        String c = body.replaceAll("\\s+", " ").trim();
        return c.length() > 500 ? c.substring(0, 500) + "..." : c;
    }

    private boolean isBlank(String v) {
        return v == null || v.trim().isEmpty();
    }
}

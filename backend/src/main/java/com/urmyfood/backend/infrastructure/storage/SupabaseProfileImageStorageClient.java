package com.urmyfood.backend.infrastructure.storage;

import com.urmyfood.backend.application.service.ProfileImageStorageClient;
import com.urmyfood.backend.domain.model.ShopProfileImageType;
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
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class SupabaseProfileImageStorageClient implements ProfileImageStorageClient {

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
    public String uploadShopProfileImage(Long shopId, ShopProfileImageType type, MultipartFile file) {
        validateConfig();
        validateFile(file);

        String objectPath = buildObjectPath(shopId, type, file.getContentType());
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
            log.warn(
                    "Supabase Storage upload failed. status={}, body={}",
                    e.getStatusCode().value(),
                    sanitizeResponseBody(e.getResponseBodyAsString())
            );
            throw new IllegalStateException(toUserMessage(e));
        } catch (Exception e) {
            log.warn("Supabase Storage upload failed unexpectedly", e);
            throw new IllegalStateException("Không thể tải ảnh lên Supabase Storage");
        }

        return normalizeUrl(supabaseUrl) + "/storage/v1/object/public/" + bucket + "/" + objectPath;
    }

    @Override
    public void deleteShopProfileImage(Long shopId, String imageUrl) {
        Optional<String> objectPath = extractObjectPath(shopId, imageUrl);
        if (objectPath.isEmpty()) {
            return;
        }

        try {
            String deleteUrl = normalizeUrl(supabaseUrl) + "/storage/v1/object/" + bucket + "/" + objectPath.get();
            restClientBuilder.build()
                    .delete()
                    .uri(deleteUrl)
                    .header("apikey", anonKey)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + anonKey)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException e) {
            log.warn(
                    "Supabase Storage delete failed. shopId={}, status={}, body={}",
                    shopId,
                    e.getStatusCode().value(),
                    sanitizeResponseBody(e.getResponseBodyAsString())
            );
        } catch (Exception e) {
            log.warn("Supabase Storage delete failed unexpectedly. shopId={}", shopId, e);
        }
    }

    @Override
    public String uploadAdminAvatar(Long accountId, MultipartFile file) {
        validateConfig();
        validateFile(file);

        String extension = switch (file.getContentType().toLowerCase(Locale.ROOT)) {
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            default -> "jpg";
        };
        String objectPath = "admin-avatars/%d/%s.%s".formatted(accountId, UUID.randomUUID(), extension);
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
            log.warn(
                    "Supabase Storage admin avatar upload failed. status={}, body={}",
                    e.getStatusCode().value(),
                    sanitizeResponseBody(e.getResponseBodyAsString())
            );
            throw new IllegalStateException(toUserMessage(e));
        } catch (Exception e) {
            log.warn("Supabase Storage admin avatar upload failed unexpectedly", e);
            throw new IllegalStateException("Không thể tải ảnh lên Supabase Storage");
        }

        return normalizeUrl(supabaseUrl) + "/storage/v1/object/public/" + bucket + "/" + objectPath;
    }

    @Override
    public void deleteAdminAvatar(Long accountId, String imageUrl) {
        if (accountId == null || isBlank(imageUrl) || isBlank(supabaseUrl) || isBlank(bucket)) {
            return;
        }
        String publicPrefix = normalizeUrl(supabaseUrl) + "/storage/v1/object/public/" + bucket + "/";
        if (!imageUrl.startsWith(publicPrefix)) {
            return;
        }
        String objectPath = imageUrl.substring(publicPrefix.length());
        String adminPrefix = "admin-avatars/" + accountId + "/";
        if (!objectPath.startsWith(adminPrefix)) {
            return;
        }

        try {
            String deleteUrl = normalizeUrl(supabaseUrl) + "/storage/v1/object/" + bucket + "/" + objectPath;
            restClientBuilder.build()
                    .delete()
                    .uri(deleteUrl)
                    .header("apikey", anonKey)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + anonKey)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException e) {
            log.warn(
                    "Supabase Storage admin avatar delete failed. accountId={}, status={}, body={}",
                    accountId,
                    e.getStatusCode().value(),
                    sanitizeResponseBody(e.getResponseBodyAsString())
            );
        } catch (Exception e) {
            log.warn("Supabase Storage admin avatar delete failed unexpectedly. accountId={}", accountId, e);
        }
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
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("Định dạng ảnh không hợp lệ");
        }
    }

    private String buildObjectPath(Long shopId, ShopProfileImageType type, String contentType) {
        String extension = switch (contentType.toLowerCase(Locale.ROOT)) {
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            default -> "jpg";
        };
        return "shop-profiles/%d/%s-%s.%s".formatted(
                shopId,
                type.getPathPrefix(),
                UUID.randomUUID(),
                extension
        );
    }

    private Optional<String> extractObjectPath(Long shopId, String imageUrl) {
        if (shopId == null || isBlank(imageUrl) || isBlank(supabaseUrl) || isBlank(bucket)) {
            return Optional.empty();
        }
        String publicPrefix = normalizeUrl(supabaseUrl) + "/storage/v1/object/public/" + bucket + "/";
        if (!imageUrl.startsWith(publicPrefix)) {
            return Optional.empty();
        }
        String objectPath = imageUrl.substring(publicPrefix.length());
        String shopPrefix = "shop-profiles/" + shopId + "/";
        if (!objectPath.startsWith(shopPrefix)) {
            return Optional.empty();
        }
        return Optional.of(objectPath);
    }

    private String normalizeUrl(String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    private String toUserMessage(RestClientResponseException exception) {
        int statusCode = exception.getStatusCode().value();
        return switch (statusCode) {
            case 401, 403 -> "Supabase Storage từ chối upload. Vui lòng kiểm tra SUPABASE_ANON_KEY hoặc policy upload của bucket";
            case 404 -> "Không tìm thấy bucket Supabase Storage";
            case 413 -> "File ảnh vượt quá dung lượng Supabase cho phép";
            default -> "Không thể tải ảnh lên Supabase Storage (HTTP " + statusCode + ")";
        };
    }

    private String sanitizeResponseBody(String body) {
        if (body == null || body.isBlank()) {
            return "";
        }
        String compact = body.replaceAll("\\s+", " ").trim();
        return compact.length() > 500 ? compact.substring(0, 500) + "..." : compact;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}

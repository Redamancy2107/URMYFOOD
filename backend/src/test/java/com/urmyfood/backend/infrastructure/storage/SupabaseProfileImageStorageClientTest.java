package com.urmyfood.backend.infrastructure.storage;

import com.urmyfood.backend.domain.model.ShopProfileImageType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.headerDoesNotExist;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class SupabaseProfileImageStorageClientTest {

    private SupabaseProfileImageStorageClient client;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        client = new SupabaseProfileImageStorageClient(builder);
        ReflectionTestUtils.setField(client, "supabaseUrl", "https://project.supabase.co");
        ReflectionTestUtils.setField(client, "anonKey", "anon-key");
        ReflectionTestUtils.setField(client, "bucket", "profile-images");
        ReflectionTestUtils.setField(client, "maxSize", 1024L);
    }

    @Test
    void uploadShopProfileImageUploadsToSupabaseAndReturnsPublicUrl() {
        String expectedPrefix = "https://project.supabase.co/storage/v1/object/profile-images/shop-profiles/2/logo-";
        server.expect(requestTo(startsWith(expectedPrefix)))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("apikey", "anon-key"))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer anon-key"))
                .andExpect(headerDoesNotExist("x-upsert"))
                .andRespond(withSuccess());

        String imageUrl = client.uploadShopProfileImage(
                2L,
                ShopProfileImageType.LOGO,
                new MockMultipartFile("file", "logo.png", "image/png", "image".getBytes())
        );

        assertThat(imageUrl).startsWith("https://project.supabase.co/storage/v1/object/public/profile-images/shop-profiles/2/logo-");
        server.verify();
    }

    @Test
    void uploadShopProfileImageRejectsInvalidContentType() {
        MockMultipartFile file = new MockMultipartFile("file", "logo.txt", "text/plain", "text".getBytes());

        assertThatThrownBy(() -> client.uploadShopProfileImage(2L, ShopProfileImageType.LOGO, file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Định dạng ảnh không hợp lệ");
    }

    @Test
    void uploadShopProfileImageRejectsMissingConfig() {
        ReflectionTestUtils.setField(client, "anonKey", "");
        MockMultipartFile file = new MockMultipartFile("file", "logo.png", "image/png", "image".getBytes());

        assertThatThrownBy(() -> client.uploadShopProfileImage(2L, ShopProfileImageType.LOGO, file))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Thiếu cấu hình Supabase Storage");
    }

    @Test
    void uploadShopProfileImageMapsForbiddenFromSupabase() {
        expectSupabaseError(HttpStatus.FORBIDDEN, "{\"message\":\"new row violates row-level security policy\"}");

        assertThatThrownBy(() -> client.uploadShopProfileImage(2L, ShopProfileImageType.LOGO, validImage()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Supabase Storage từ chối upload. Vui lòng kiểm tra SUPABASE_ANON_KEY hoặc policy upload của bucket");

        server.verify();
    }

    @Test
    void uploadShopProfileImageMapsMissingBucketFromSupabase() {
        expectSupabaseError(HttpStatus.NOT_FOUND, "{\"message\":\"Bucket not found\"}");

        assertThatThrownBy(() -> client.uploadShopProfileImage(2L, ShopProfileImageType.LOGO, validImage()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Không tìm thấy bucket Supabase Storage");

        server.verify();
    }

    @Test
    void uploadShopProfileImageMapsPayloadTooLargeFromSupabase() {
        expectSupabaseError(HttpStatus.PAYLOAD_TOO_LARGE, "{\"message\":\"Payload too large\"}");

        assertThatThrownBy(() -> client.uploadShopProfileImage(2L, ShopProfileImageType.LOGO, validImage()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("File ảnh vượt quá dung lượng Supabase cho phép");

        server.verify();
    }

    @Test
    void uploadShopProfileImageMapsOtherSupabaseErrorWithStatusOnly() {
        expectSupabaseError(HttpStatus.INTERNAL_SERVER_ERROR, "{\"message\":\"anon-key should not leak\"}");

        assertThatThrownBy(() -> client.uploadShopProfileImage(2L, ShopProfileImageType.LOGO, validImage()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Không thể tải ảnh lên Supabase Storage (HTTP 500)");

        server.verify();
    }

    @Test
    void uploadShopProfileImageRejectsOversizedFile() {
        ReflectionTestUtils.setField(client, "maxSize", 2L);
        MockMultipartFile file = new MockMultipartFile("file", "logo.png", "image/png", "image".getBytes());

        assertThatThrownBy(() -> client.uploadShopProfileImage(2L, ShopProfileImageType.LOGO, file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("File ảnh vượt quá dung lượng cho phép");
    }

    @Test
    void deleteShopProfileImageDeletesMatchingObject() {
        String imageUrl = "https://project.supabase.co/storage/v1/object/public/profile-images/shop-profiles/2/logo-old.png";

        server.expect(requestTo("https://project.supabase.co/storage/v1/object/profile-images/shop-profiles/2/logo-old.png"))
                .andExpect(method(HttpMethod.DELETE))
                .andExpect(header("apikey", "anon-key"))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer anon-key"))
                .andExpect(headerDoesNotExist("x-upsert"))
                .andRespond(withSuccess());

        client.deleteShopProfileImage(2L, imageUrl);

        server.verify();
    }

    @Test
    void deleteShopProfileImageIgnoresExternalUrl() {
        client.deleteShopProfileImage(2L, "https://cdn.example.com/logo-old.png");

        server.verify();
    }

    @Test
    void deleteShopProfileImageIgnoresDifferentShopPath() {
        String imageUrl = "https://project.supabase.co/storage/v1/object/public/profile-images/shop-profiles/3/logo-old.png";

        client.deleteShopProfileImage(2L, imageUrl);

        server.verify();
    }

    @Test
    void deleteShopProfileImageDoesNotThrowWhenSupabaseRejectsDelete() {
        String imageUrl = "https://project.supabase.co/storage/v1/object/public/profile-images/shop-profiles/2/logo-old.png";

        server.expect(requestTo("https://project.supabase.co/storage/v1/object/profile-images/shop-profiles/2/logo-old.png"))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withStatus(HttpStatus.FORBIDDEN).body("{\"message\":\"delete policy denied\"}"));

        client.deleteShopProfileImage(2L, imageUrl);

        server.verify();
    }

    private void expectSupabaseError(HttpStatus status, String body) {
        String expectedPrefix = "https://project.supabase.co/storage/v1/object/profile-images/shop-profiles/2/logo-";
        server.expect(requestTo(startsWith(expectedPrefix)))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(status).body(body));
    }

    private MockMultipartFile validImage() {
        return new MockMultipartFile("file", "logo.png", "image/png", "image".getBytes());
    }
}

package com.urmyfood.backend.infrastructure.storage;

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
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class SupabaseChatImageStorageClientTest {

    private SupabaseChatImageStorageClient client;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        client = new SupabaseChatImageStorageClient(builder);
        ReflectionTestUtils.setField(client, "supabaseUrl", "https://project.supabase.co");
        ReflectionTestUtils.setField(client, "anonKey", "anon-key");
        ReflectionTestUtils.setField(client, "serviceRoleKey", "");
        ReflectionTestUtils.setField(client, "bucket", "chat-images-bucket");
        ReflectionTestUtils.setField(client, "maxSize", 1024L);
    }

    @Test
    void uploadChatImageUploadsToSupabaseAndReturnsPublicUrl() {
        String expectedPrefix = "https://project.supabase.co/storage/v1/object/chat-images-bucket/chat-images/1/";
        server.expect(requestTo(startsWith(expectedPrefix)))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("apikey", "anon-key"))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer anon-key"))
                .andRespond(withSuccess());

        String imageUrl = client.uploadChatImage(1L, validImage());

        assertThat(imageUrl).startsWith(
                "https://project.supabase.co/storage/v1/object/public/chat-images-bucket/chat-images/1/"
        );
        server.verify();
    }

    @Test
    void uploadChatImagePrefersServiceRoleKeyWhenConfigured() {
        ReflectionTestUtils.setField(client, "serviceRoleKey", "service-role-key");
        String expectedPrefix = "https://project.supabase.co/storage/v1/object/chat-images-bucket/chat-images/1/";
        server.expect(requestTo(startsWith(expectedPrefix)))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("apikey", "service-role-key"))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer service-role-key"))
                .andRespond(withSuccess());

        String imageUrl = client.uploadChatImage(1L, validImage());

        assertThat(imageUrl).startsWith(
                "https://project.supabase.co/storage/v1/object/public/chat-images-bucket/chat-images/1/"
        );
        server.verify();
    }

    @Test
    void uploadChatImageMapsBadRequestMessageFromSupabase() {
        expectSupabaseError(HttpStatus.BAD_REQUEST, "{\"message\":\"new row violates row-level security policy\"}");

        assertThatThrownBy(() -> client.uploadChatImage(1L, validImage()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("new row violates row-level security policy");

        server.verify();
    }

    @Test
    void uploadChatImageRejectsInvalidContentTypeBeforeCallingSupabase() {
        MockMultipartFile file = new MockMultipartFile("file", "chat.txt", "text/plain", "text".getBytes());

        assertThatThrownBy(() -> client.uploadChatImage(1L, file))
                .isInstanceOf(IllegalArgumentException.class);

        server.verify();
    }

    private void expectSupabaseError(HttpStatus status, String body) {
        String expectedPrefix = "https://project.supabase.co/storage/v1/object/chat-images-bucket/chat-images/1/";
        server.expect(requestTo(startsWith(expectedPrefix)))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(status).body(body));
    }

    private MockMultipartFile validImage() {
        return new MockMultipartFile("file", "chat.png", "image/png", "image".getBytes());
    }
}

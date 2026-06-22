package com.urmyfood.backend.application.service;

import com.urmyfood.backend.application.dto.ShopProfileRequest;
import com.urmyfood.backend.application.dto.ShopProfileResponse;
import com.urmyfood.backend.domain.model.Account;
import com.urmyfood.backend.domain.model.ShopProfile;
import com.urmyfood.backend.domain.model.ShopProfileImageType;
import com.urmyfood.backend.domain.model.ShopVerification;
import com.urmyfood.backend.domain.model.ShopVerificationStatus;
import com.urmyfood.backend.domain.repository.AccountRepository;
import com.urmyfood.backend.domain.repository.ShopProfileRepository;
import com.urmyfood.backend.domain.repository.ShopVerificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShopProfileServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private ShopProfileRepository shopProfileRepository;

    @Mock
    private ShopVerificationRepository shopVerificationRepository;

    @Mock
    private ProfileImageStorageClient profileImageStorageClient;

    @InjectMocks
    private ShopProfileService shopProfileService;

    @Test
    void getMyProfileReturnsExistingProfile() {
        Account shop = account(2L, "SHOP");
        when(accountRepository.findById(shop.getId())).thenReturn(Optional.of(shop));
        when(shopVerificationRepository.findByShopId(shop.getId())).thenReturn(Optional.of(verification(shop)));
        when(shopProfileRepository.findByShopId(shop.getId())).thenReturn(Optional.of(profile(shop)));

        ShopProfileResponse response = shopProfileService.getMyProfile(shop.getId());

        assertThat(response.getShopName()).isEqualTo("Bếp Nhà A");
        assertThat(response.getVerificationStatus()).isEqualTo("APPROVED");
        assertThat(response.getIsOpen()).isTrue();
    }

    @Test
    void getMyProfileFallsBackToVerificationWhenProfileDoesNotExist() {
        Account shop = account(2L, "SHOP");
        when(accountRepository.findById(shop.getId())).thenReturn(Optional.of(shop));
        when(shopVerificationRepository.findByShopId(shop.getId())).thenReturn(Optional.of(verification(shop)));
        when(shopProfileRepository.findByShopId(shop.getId())).thenReturn(Optional.empty());

        ShopProfileResponse response = shopProfileService.getMyProfile(shop.getId());

        assertThat(response.getShopName()).isEqualTo("Quán Cơm Xác Minh");
        assertThat(response.getCategory()).isEqualTo("COM");
        assertThat(response.getAddress()).isEqualTo("123 Lê Lợi");
        assertThat(response.getLatitude()).isEqualTo(10.77);
        assertThat(response.getLongitude()).isEqualTo(106.7);
    }

    @Test
    void updateMyProfileSavesValidProfile() {
        Account shop = account(2L, "SHOP");
        when(accountRepository.findById(shop.getId())).thenReturn(Optional.of(shop));
        when(shopVerificationRepository.findByShopId(shop.getId())).thenReturn(Optional.empty());
        when(shopProfileRepository.findByShopId(shop.getId())).thenReturn(Optional.empty());
        when(shopProfileRepository.save(any(ShopProfile.class))).thenAnswer(invocation -> {
            ShopProfile saved = invocation.getArgument(0);
            saved.setId(9L);
            return saved;
        });

        ShopProfileResponse response = shopProfileService.updateMyProfile(shop.getId(), request());

        ArgumentCaptor<ShopProfile> captor = ArgumentCaptor.forClass(ShopProfile.class);
        verify(shopProfileRepository).save(captor.capture());
        assertThat(captor.getValue().getShopName()).isEqualTo("Bếp Nhà B");
        assertThat(captor.getValue().getDescription()).isEqualTo("Cơm trưa sinh viên");
        assertThat(captor.getValue().getLatitude()).isEqualTo(10.78);
        assertThat(captor.getValue().getLongitude()).isEqualTo(106.71);
        assertThat(response.getId()).isEqualTo(9L);
        verify(profileImageStorageClient, never()).deleteShopProfileImage(any(), any());
    }

    @Test
    void updateMyProfileDeletesOldImagesWhenUrlsChange() {
        Account shop = account(2L, "SHOP");
        ShopProfile existing = profile(shop);
        existing.setLogoUrl("https://project.supabase.co/storage/v1/object/public/profile-images/shop-profiles/2/logo-old.png");
        existing.setCoverUrl("https://project.supabase.co/storage/v1/object/public/profile-images/shop-profiles/2/cover-old.png");
        when(accountRepository.findById(shop.getId())).thenReturn(Optional.of(shop));
        when(shopVerificationRepository.findByShopId(shop.getId())).thenReturn(Optional.empty());
        when(shopProfileRepository.findByShopId(shop.getId())).thenReturn(Optional.of(existing));
        when(shopProfileRepository.save(any(ShopProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        shopProfileService.updateMyProfile(shop.getId(), request());

        verify(profileImageStorageClient).deleteShopProfileImage(shop.getId(), existing.getLogoUrl());
        verify(profileImageStorageClient).deleteShopProfileImage(shop.getId(), existing.getCoverUrl());
    }

    @Test
    void updateMyProfileDoesNotDeleteImagesWhenUrlsAreUnchanged() {
        Account shop = account(2L, "SHOP");
        ShopProfile existing = profile(shop);
        existing.setLogoUrl("https://example.com/logo.png");
        existing.setCoverUrl("https://example.com/cover.png");
        when(accountRepository.findById(shop.getId())).thenReturn(Optional.of(shop));
        when(shopVerificationRepository.findByShopId(shop.getId())).thenReturn(Optional.empty());
        when(shopProfileRepository.findByShopId(shop.getId())).thenReturn(Optional.of(existing));
        when(shopProfileRepository.save(any(ShopProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        shopProfileService.updateMyProfile(shop.getId(), request());

        verify(profileImageStorageClient, never()).deleteShopProfileImage(any(), any());
    }

    @Test
    void updateMyProfileRejectsInvalidCategory() {
        Account shop = account(2L, "SHOP");
        ShopProfileRequest request = request();
        request.setCategory("Bún");
        when(accountRepository.findById(shop.getId())).thenReturn(Optional.of(shop));
        when(shopVerificationRepository.findByShopId(shop.getId())).thenReturn(Optional.empty());
        when(shopProfileRepository.findByShopId(shop.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> shopProfileService.updateMyProfile(shop.getId(), request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Danh mục quán không hợp lệ");

        verify(shopProfileRepository, never()).save(any());
    }

    @Test
    void updateMyProfileRejectsBlankShopName() {
        Account shop = account(2L, "SHOP");
        ShopProfileRequest request = request();
        request.setShopName(" ");
        when(accountRepository.findById(shop.getId())).thenReturn(Optional.of(shop));
        when(shopVerificationRepository.findByShopId(shop.getId())).thenReturn(Optional.empty());
        when(shopProfileRepository.findByShopId(shop.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> shopProfileService.updateMyProfile(shop.getId(), request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Tên quán không được để trống");

        verify(shopProfileRepository, never()).save(any());
    }

    @Test
    void updateMyProfileRejectsIncompleteCoordinates() {
        Account shop = account(2L, "SHOP");
        ShopProfileRequest request = request();
        request.setLongitude(null);
        when(accountRepository.findById(shop.getId())).thenReturn(Optional.of(shop));
        when(shopVerificationRepository.findByShopId(shop.getId())).thenReturn(Optional.empty());
        when(shopProfileRepository.findByShopId(shop.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> shopProfileService.updateMyProfile(shop.getId(), request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Vui lòng cung cấp đầy đủ tọa độ quán");

        verify(shopProfileRepository, never()).save(any());
    }

    @Test
    void updateMyProfileRejectsInvalidLatitude() {
        Account shop = account(2L, "SHOP");
        ShopProfileRequest request = request();
        request.setLatitude(91.0);
        when(accountRepository.findById(shop.getId())).thenReturn(Optional.of(shop));
        when(shopVerificationRepository.findByShopId(shop.getId())).thenReturn(Optional.empty());
        when(shopProfileRepository.findByShopId(shop.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> shopProfileService.updateMyProfile(shop.getId(), request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Vĩ độ quán không hợp lệ");
    }

    @Test
    void uploadProfileImageReturnsPersistentUrl() {
        Account shop = account(2L, "SHOP");
        MockMultipartFile file = new MockMultipartFile("file", "logo.png", "image/png", "data".getBytes());
        when(accountRepository.findById(shop.getId())).thenReturn(Optional.of(shop));
        when(profileImageStorageClient.uploadShopProfileImage(shop.getId(), ShopProfileImageType.LOGO, file))
                .thenReturn("https://project.supabase.co/storage/v1/object/public/bucket/logo.png");

        var response = shopProfileService.uploadProfileImage(shop.getId(), ShopProfileImageType.LOGO, file);

        assertThat(response.getImageUrl()).isEqualTo("https://project.supabase.co/storage/v1/object/public/bucket/logo.png");
    }

    @Test
    void getPublicProfileReturnsExistingProfile() {
        Account shop = account(2L, "SHOP");
        when(accountRepository.findById(shop.getId())).thenReturn(Optional.of(shop));
        when(shopVerificationRepository.findByShopId(shop.getId())).thenReturn(Optional.of(verification(shop)));
        when(shopProfileRepository.findByShopId(shop.getId())).thenReturn(Optional.of(profile(shop)));

        ShopProfileResponse response = shopProfileService.getPublicProfile(shop.getId());

        assertThat(response.getShopName()).isEqualTo("Bếp Nhà A");
        assertThat(response.getVerificationStatus()).isEqualTo("APPROVED");
    }

    @Test
    void updateMyProfileRejectsCustomerAccount() {
        Account customer = account(1L, "CUSTOMER");
        when(accountRepository.findById(customer.getId())).thenReturn(Optional.of(customer));

        assertThatThrownBy(() -> shopProfileService.updateMyProfile(customer.getId(), request()))
                .isInstanceOf(AccessDeniedException.class);
    }

    private ShopProfileRequest request() {
        return ShopProfileRequest.builder()
                .shopName("Bếp Nhà B")
                .logoUrl("https://example.com/logo.png")
                .coverUrl("https://example.com/cover.png")
                .category("COM")
                .address("456 Nguyễn Huệ")
                .latitude(10.78)
                .longitude(106.71)
                .description("Cơm trưa sinh viên")
                .openingHours("09:00 - 21:00")
                .isOpen(true)
                .build();
    }

    private ShopProfile profile(Account shop) {
        return ShopProfile.builder()
                .id(7L)
                .shop(shop)
                .shopName("Bếp Nhà A")
                .category("COM")
                .address("123 Lê Lợi")
                .latitude(10.77)
                .longitude(106.7)
                .openingHours("08:00 - 22:00")
                .isOpen(true)
                .build();
    }

    private ShopVerification verification(Account shop) {
        return ShopVerification.builder()
                .shop(shop)
                .shopName("Quán Cơm Xác Minh")
                .category("COM")
                .address("123 Lê Lợi")
                .latitude(10.77)
                .longitude(106.7)
                .status(ShopVerificationStatus.APPROVED)
                .build();
    }

    private Account account(Long id, String role) {
        return Account.builder()
                .id(id)
                .fullName("Chủ Quán")
                .avatarUrl("https://example.com/avatar.png")
                .role(role)
                .build();
    }
}

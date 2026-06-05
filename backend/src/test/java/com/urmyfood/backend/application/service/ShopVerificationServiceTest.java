package com.urmyfood.backend.application.service;

import com.urmyfood.backend.application.dto.ShopVerificationRequest;
import com.urmyfood.backend.application.dto.ShopVerificationResponse;
import com.urmyfood.backend.domain.model.Account;
import com.urmyfood.backend.domain.model.ShopVerification;
import com.urmyfood.backend.domain.model.ShopVerificationStatus;
import com.urmyfood.backend.domain.repository.AccountRepository;
import com.urmyfood.backend.domain.repository.ShopVerificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShopVerificationServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private ShopVerificationRepository shopVerificationRepository;

    @InjectMocks
    private ShopVerificationService shopVerificationService;

    @Test
    void submitCreatesPendingVerificationForShop() {
        Account shop = account(2L, "SHOP");
        when(accountRepository.findById(shop.getId())).thenReturn(Optional.of(shop));
        when(shopVerificationRepository.findByShopId(shop.getId())).thenReturn(Optional.empty());
        when(shopVerificationRepository.save(any(ShopVerification.class))).thenAnswer(invocation -> {
            ShopVerification verification = invocation.getArgument(0);
            verification.setId(10L);
            return verification;
        });

        ShopVerificationResponse response = shopVerificationService.submit(shop.getId(), request());

        ArgumentCaptor<ShopVerification> captor = ArgumentCaptor.forClass(ShopVerification.class);
        verify(shopVerificationRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(ShopVerificationStatus.PENDING);
        assertThat(captor.getValue().getShop().getId()).isEqualTo(shop.getId());
        assertThat(response.getStatus()).isEqualTo("PENDING");
    }

    @Test
    void submitUpdatesExistingVerificationAndResetsRejectReason() {
        Account shop = account(2L, "SHOP");
        ShopVerification existing = ShopVerification.builder()
                .id(8L)
                .shop(shop)
                .status(ShopVerificationStatus.REJECTED)
                .rejectReason("Thiếu ảnh")
                .build();
        when(accountRepository.findById(shop.getId())).thenReturn(Optional.of(shop));
        when(shopVerificationRepository.findByShopId(shop.getId())).thenReturn(Optional.of(existing));
        when(shopVerificationRepository.save(any(ShopVerification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        shopVerificationService.submit(shop.getId(), request());

        ArgumentCaptor<ShopVerification> captor = ArgumentCaptor.forClass(ShopVerification.class);
        verify(shopVerificationRepository).save(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(8L);
        assertThat(captor.getValue().getStatus()).isEqualTo(ShopVerificationStatus.PENDING);
        assertThat(captor.getValue().getRejectReason()).isNull();
    }

    @Test
    void submitRejectsCustomerAccount() {
        Account customer = account(1L, "CUSTOMER");
        when(accountRepository.findById(customer.getId())).thenReturn(Optional.of(customer));

        assertThatThrownBy(() -> shopVerificationService.submit(customer.getId(), request()))
                .isInstanceOf(AccessDeniedException.class);

        verify(shopVerificationRepository, never()).save(any(ShopVerification.class));
    }

    @Test
    void submitRejectsEmptyPhotoListAfterTrim() {
        Account shop = account(2L, "SHOP");
        ShopVerificationRequest request = request();
        request.setShopPhotoUrls(List.of(" "));
        when(accountRepository.findById(shop.getId())).thenReturn(Optional.of(shop));
        when(shopVerificationRepository.findByShopId(shop.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> shopVerificationService.submit(shop.getId(), request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Vui lòng thêm ít nhất 1 ảnh quán");
    }

    private ShopVerificationRequest request() {
        return ShopVerificationRequest.builder()
                .shopName("Quán Cơm")
                .category("COM")
                .address("123 Lê Lợi")
                .latitude(10.77)
                .longitude(106.7)
                .cccdFrontUrl("content://front")
                .cccdBackUrl("content://back")
                .shopPhotoUrls(List.of("content://photo"))
                .build();
    }

    private Account account(Long id, String role) {
        return Account.builder()
                .id(id)
                .fullName("Test")
                .role(role)
                .build();
    }
}

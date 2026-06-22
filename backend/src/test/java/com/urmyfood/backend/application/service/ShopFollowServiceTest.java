package com.urmyfood.backend.application.service;

import com.urmyfood.backend.application.dto.ShopFollowResponse;
import com.urmyfood.backend.domain.model.Account;
import com.urmyfood.backend.domain.repository.AccountRepository;
import com.urmyfood.backend.domain.repository.ShopFollowRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShopFollowServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private ShopFollowRepository shopFollowRepository;

    @InjectMocks
    private ShopFollowService shopFollowService;

    @Test
    void followReturnsPersistedStateAndFollowerCount() {
        Account customer = account(1L, "CUSTOMER");
        Account shop = account(2L, "SHOP");

        when(accountRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
        when(accountRepository.findById(shop.getId())).thenReturn(Optional.of(shop));
        when(shopFollowRepository.isFollowing(customer.getId(), shop.getId())).thenReturn(true);
        when(shopFollowRepository.countByShopId(shop.getId())).thenReturn(7L);

        ShopFollowResponse response = shopFollowService.follow(customer.getId(), shop.getId());

        verify(shopFollowRepository).follow(customer.getId(), shop.getId());
        assertThat(response.isFollowing()).isTrue();
        assertThat(response.getFollowerCount()).isEqualTo(7L);
    }

    @Test
    void unfollowReturnsPersistedStateAndFollowerCount() {
        Account customer = account(1L, "CUSTOMER");
        Account shop = account(2L, "SHOP");

        when(accountRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
        when(accountRepository.findById(shop.getId())).thenReturn(Optional.of(shop));
        when(shopFollowRepository.isFollowing(customer.getId(), shop.getId())).thenReturn(false);
        when(shopFollowRepository.countByShopId(shop.getId())).thenReturn(6L);

        ShopFollowResponse response = shopFollowService.unfollow(customer.getId(), shop.getId());

        verify(shopFollowRepository).unfollow(customer.getId(), shop.getId());
        assertThat(response.isFollowing()).isFalse();
        assertThat(response.getFollowerCount()).isEqualTo(6L);
    }

    @Test
    void followRejectsNonCustomerActor() {
        Account shopActor = account(2L, "SHOP");

        when(accountRepository.findById(shopActor.getId())).thenReturn(Optional.of(shopActor));

        assertThatThrownBy(() -> shopFollowService.follow(shopActor.getId(), 3L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Chi tai khoan khach hang moi duoc theo doi shop");

        verifyNoInteractions(shopFollowRepository);
    }

    @Test
    void followRejectsNonShopTarget() {
        Account customer = account(1L, "CUSTOMER");
        Account otherCustomer = account(3L, "CUSTOMER");

        when(accountRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
        when(accountRepository.findById(otherCustomer.getId())).thenReturn(Optional.of(otherCustomer));

        assertThatThrownBy(() -> shopFollowService.follow(customer.getId(), otherCustomer.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Tai khoan nay khong phai shop");

        verify(shopFollowRepository, never()).follow(customer.getId(), otherCustomer.getId());
    }

    @Test
    void followRejectsSelfFollow() {
        Account customer = account(1L, "CUSTOMER");

        when(accountRepository.findById(customer.getId())).thenReturn(Optional.of(customer));

        assertThatThrownBy(() -> shopFollowService.follow(customer.getId(), customer.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Tai khoan nay khong phai shop");

        verify(shopFollowRepository, never()).follow(customer.getId(), customer.getId());
    }

    private Account account(Long id, String role) {
        return Account.builder()
                .id(id)
                .role(role)
                .fullName(role + " " + id)
                .build();
    }
}

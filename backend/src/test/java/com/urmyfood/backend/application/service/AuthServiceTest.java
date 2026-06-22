package com.urmyfood.backend.application.service;

import com.urmyfood.backend.application.dto.RegisterRequest;
import com.urmyfood.backend.domain.model.Account;
import com.urmyfood.backend.domain.repository.AccountRepository;
import com.urmyfood.backend.infrastructure.security.CustomAccountDetails;
import com.urmyfood.backend.infrastructure.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private OtpService otpService;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerWithShopRoleSavesShopAccount() {
        RegisterRequest request = registerRequest("SHOP");
        when(accountRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(accountRepository.findByPhone(request.getPhone())).thenReturn(Optional.empty());
        when(otpService.verifyOtp(request.getEmail(), request.getOtpCode())).thenReturn(true);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("hashed");
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateToken(any(CustomAccountDetails.class))).thenReturn("access");
        when(jwtService.generateRefreshToken(any(CustomAccountDetails.class))).thenReturn("refresh");

        authService.register(request);

        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(accountCaptor.capture());
        assertThat(accountCaptor.getValue().getRole()).isEqualTo("SHOP");
    }

    @Test
    void registerWithoutRoleDefaultsToCustomer() {
        RegisterRequest request = registerRequest(null);
        when(accountRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(accountRepository.findByPhone(request.getPhone())).thenReturn(Optional.empty());
        when(otpService.verifyOtp(request.getEmail(), request.getOtpCode())).thenReturn(true);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("hashed");
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateToken(any(CustomAccountDetails.class))).thenReturn("access");
        when(jwtService.generateRefreshToken(any(CustomAccountDetails.class))).thenReturn("refresh");

        authService.register(request);

        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(accountCaptor.capture());
        assertThat(accountCaptor.getValue().getRole()).isEqualTo("CUSTOMER");
    }

    @Test
    void registerRejectsAdminRole() {
        RegisterRequest request = registerRequest("ADMIN");
        when(accountRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(accountRepository.findByPhone(request.getPhone())).thenReturn(Optional.empty());
        when(otpService.verifyOtp(request.getEmail(), request.getOtpCode())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Vai trò đăng ký không hợp lệ");

        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void registerReturnsVietnameseMessageWhenEmailExists() {
        RegisterRequest request = registerRequest("SHOP");
        when(accountRepository.findByEmail(request.getEmail()))
                .thenReturn(Optional.of(Account.builder().id(1L).build()));

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Email đã được sử dụng bởi tài khoản khác");
    }

    private RegisterRequest registerRequest(String role) {
        return RegisterRequest.builder()
                .fullName("Shop A")
                .email("shop@test.com")
                .phone("0123456789")
                .password("secret1")
                .otpCode("123456")
                .role(role)
                .build();
    }
}

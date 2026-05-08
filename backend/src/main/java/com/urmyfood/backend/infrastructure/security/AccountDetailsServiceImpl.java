package com.urmyfood.backend.infrastructure.security;

import com.urmyfood.backend.domain.model.Account;
import com.urmyfood.backend.domain.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountDetailsServiceImpl implements UserDetailsService {

    private final AccountRepository accountRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = accountRepository.findByEmail(username)
                .or(() -> accountRepository.findByPhone(username))
                .orElseThrow(() -> new UsernameNotFoundException("Account not found: " + username));
        
        return new CustomAccountDetails(account);
    }
}

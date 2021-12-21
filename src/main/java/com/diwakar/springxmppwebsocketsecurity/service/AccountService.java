package com.diwakar.springxmppwebsocketsecurity.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.diwakar.springxmppwebsocketsecurity.model.Account;
import com.diwakar.springxmppwebsocketsecurity.repository.AccountRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    public Optional<Account> getAccount(String username) {
        return accountRepository.findById(username);
    }

    public void saveAccount(Account account) {
        accountRepository.save(account);
    }
}

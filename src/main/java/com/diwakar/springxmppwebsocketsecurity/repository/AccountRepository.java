package com.diwakar.springxmppwebsocketsecurity.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.diwakar.springxmppwebsocketsecurity.model.Account;

@Repository
public interface AccountRepository extends MongoRepository<Account, String> {
}

package com.vulcanium.springsecurityauth.repository;

import com.vulcanium.springsecurityauth.model.DbUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DbUserRepository extends JpaRepository<DbUser, Integer> {
    public DbUser findByUsername(String username);
}

package com.sovon9.authentication_service.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sovon9.authentication_service.entities.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Lookup by login name (not PK)
    Optional<User> findByUsername(String username);

}

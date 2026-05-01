package com.normocontrol.domain.port;

import com.normocontrol.domain.model.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    User save(User user);
    Optional<User> findById(UUID id);
}

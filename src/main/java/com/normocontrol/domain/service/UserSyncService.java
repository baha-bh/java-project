package com.normocontrol.domain.service;

import com.normocontrol.domain.model.Role;
import com.normocontrol.domain.model.User;
import com.normocontrol.domain.port.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserSyncService {

    private final UserRepository userRepository;

    @Transactional
    public void syncUser(Jwt jwt) {
        String subject = jwt.getSubject();
        if (subject == null) return;

        try {
            UUID userId = UUID.fromString(subject);
            String email = jwt.getClaimAsString("email");

            // Check if user exists by ID
            if (userRepository.findById(userId).isPresent()) {
                return; // Already synced
            }

            // Check if user exists by Email (mismatched ID case)
            if (userRepository.findByEmail(email).isPresent()) {
                log.info("User with email {} exists but with a different ID. Using existing record.", email);
                return;
            }

            // Brand new user
            log.info("Creating new user from JWT: {} ({})", userId, email);
            User newUser = User.builder()
                    .id(userId)
                    .email(email)
                    .username(email != null ? email : userId.toString())
                    .role(Role.USER)
                    .build();
            userRepository.save(newUser);
            userRepository.flush();

        } catch (Exception e) {
            log.error("Error during user sync from JWT", e);
        }
    }
}

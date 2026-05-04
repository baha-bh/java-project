package com.normocontrol.infrastructure.persistence.adapter;

import com.normocontrol.domain.model.User;
import com.normocontrol.domain.port.UserRepository;
import com.normocontrol.infrastructure.persistence.mapper.UserMapper;
import com.normocontrol.infrastructure.persistence.repository.SpringDataUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {

    private final SpringDataUserRepository springDataUserRepository;
    private final UserMapper userMapper;

    @Override
    public Optional<User> findByUsername(String username) {
        return springDataUserRepository.findByUsername(username)
                .map(userMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return springDataUserRepository.findByEmail(email)
                .map(userMapper::toDomain);
    }

    @Override
    public User save(User user) {
        var entity = userMapper.toEntity(user);
        var savedEntity = springDataUserRepository.save(entity);
        return userMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return springDataUserRepository.findById(id)
                .map(userMapper::toDomain);
    }
 
    @Override
    public void deleteById(UUID id) {
        springDataUserRepository.deleteById(id);
    }

    @Override
    public void flush() {
        springDataUserRepository.flush();
    }
}

package com.splitforlater.user.service;

import com.splitforlater.user.dto.UserDto;
import com.splitforlater.user.exceptionhandling.UserNotFoundException;
import com.splitforlater.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Cacheable(value = "users", key = "#id")
    public UserDto getUserById(UUID id) {
        // 1. Check Local JVM Cache (0.01ms)
        // 2. Check Global Redis Cache (1ms)
        // 3. Check PostgreSQL (20ms)
        log.info("Full cache miss for user: {}", id);
        return userRepository.findById(id)
                .map(UserMapper::toDto)
                .orElseThrow(() -> new UserNotFoundException("User with id " + id + " not found"));
    }
}

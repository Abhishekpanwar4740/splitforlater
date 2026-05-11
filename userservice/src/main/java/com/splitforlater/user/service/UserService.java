package com.splitforlater.user.service;

import com.splitforlater.user.dto.UserDto;
import com.splitforlater.user.entity.User;
import com.splitforlater.common.exceptionhandling.UserNotFoundException;
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
        log.info("Full cache miss for user: {}", id);
        return userRepository.findById(id)
                .map(UserMapper::toDto)
                .orElseThrow(() -> new UserNotFoundException("User with id " + id + " not found"));
    }

    public UserDto getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(UserMapper::toDto)
                .orElseThrow(() -> new UserNotFoundException("User with email " + email + " not found"));
    }

    public UserDto createNewUser(String email,String name) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        return UserMapper.toDto(userRepository.save(user));
    }

    public UserDto updateUser(UUID id, UserDto userDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with id " + id + " not found"));
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());
        user.setImgUrl(userDto.getImgUrl());
        return UserMapper.toDto(userRepository.save(user));
    }

    public void deleteUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with id " + id + " not found"));
        userRepository.deleteById(id);

    }
}

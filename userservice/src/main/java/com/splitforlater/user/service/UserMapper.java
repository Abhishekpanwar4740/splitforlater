package com.splitforlater.user.service;

import com.splitforlater.user.dto.UserDto;
import com.splitforlater.user.entity.User;
import jakarta.validation.constraints.NotNull;

public class UserMapper {
    public static UserDto toDto(@NotNull User user) {
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .imgUrl(user.getImgUrl())
                .build();
    }
}

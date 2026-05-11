package com.splitforlater.common.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.UUID;
@Data
public class UserDto implements Serializable {
    private UUID userId;
    private String name;
    private String email;
}

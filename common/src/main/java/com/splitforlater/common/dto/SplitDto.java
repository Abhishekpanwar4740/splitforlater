package com.splitforlater.common.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.UUID;

@Data
public class SplitDto implements Serializable {
    @NotNull
    private UserDto user;
    @Min(1) private Long amount;
}

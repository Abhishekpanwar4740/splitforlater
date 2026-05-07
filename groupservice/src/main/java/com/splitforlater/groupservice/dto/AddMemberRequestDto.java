package com.splitforlater.groupservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Data
@Builder
public class AddMemberRequestDto {
    @NotNull
    private UUID userId;
}

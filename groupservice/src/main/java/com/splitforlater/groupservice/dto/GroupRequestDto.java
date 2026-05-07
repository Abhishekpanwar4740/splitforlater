package com.splitforlater.groupservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
public class GroupRequestDto {
    @NotBlank
    private String name;
    private String description;
}



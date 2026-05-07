package com.splitforlater.common.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Data
public class ExpenseRequestDto implements Serializable {
    @NotBlank
    private String description;
    @Min(1) private Long totalAmount;
    @NotNull
    private UUID groupId;
    @NotEmpty
    private List<SplitDto> splits;
}



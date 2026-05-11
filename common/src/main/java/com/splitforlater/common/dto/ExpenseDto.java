package com.splitforlater.common.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Data
public class ExpenseDto implements Serializable {
    @NotBlank
    private String description;
    @Min(1) private Long totalAmount;
    @NotNull
    private GroupDto group;
    @NotNull
    private UserDto user;
    @NotNull
    private ExpenseEnum expenseType;
    @NotEmpty
    private List<SplitDto> splits;
}



package com.splitforlater.settlementservice.dto;

import com.splitforlater.common.dto.SplitDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseResponseDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private UUID id;
    private String description;
    private Long totalAmount;
    private UUID groupId;
    private UUID payerId;
    private LocalDateTime createdAt;

    // The list of all users involved and their specific shares
    private List<SplitDto> splits;
}

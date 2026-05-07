package com.splitforlater.settlementservice.dto;

import lombok.*;

import java.util.UUID;

@Data
@AllArgsConstructor
public class UserBalance {
    private UUID userId;
    private Long balance;
}
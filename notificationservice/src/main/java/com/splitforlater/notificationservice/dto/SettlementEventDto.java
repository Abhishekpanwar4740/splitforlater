package com.splitforlater.notificationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettlementEventDto implements Serializable {
    private UUID userId;
    private String userEmail;
    private String creditorName;
    private Long amount;
}

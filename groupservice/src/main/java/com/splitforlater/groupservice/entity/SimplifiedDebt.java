package com.splitforlater.groupservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "simplified_debts")
@Data
@Builder @NoArgsConstructor @AllArgsConstructor
public class SimplifiedDebt {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private UUID groupId;
    private UUID debtorId;
    private UUID creditorId;
    private Long amount;
    private LocalDateTime createdAt;
}



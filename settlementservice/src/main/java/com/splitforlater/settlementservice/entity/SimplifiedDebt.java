package com.splitforlater.settlementservice.entity;

import jakarta.persistence.*;
import lombok.*;

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
    private UUID debtorId;  // Person who pays
    private UUID creditorId; // Person who receives
    private Long amount;
}



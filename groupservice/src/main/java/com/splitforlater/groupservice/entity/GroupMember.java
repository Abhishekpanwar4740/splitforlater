package com.splitforlater.groupservice.entity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;
@Entity
@Table(name = "group_members", indexes = {
        @Index(name = "idx_user_groups", columnList = "userId")
})
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class GroupMember {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private UUID groupId;
    private UUID userId;

    // Stored in minor units (cents) to avoid floating point errors
    private Long pendingBalance;

    private LocalDateTime joinedAt;
}

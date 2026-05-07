package com.splitforlater.groupservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "groups")
@Data @Builder
@NoArgsConstructor @AllArgsConstructor
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    private String name;
    private String description;
    private UUID createdBy;
    private LocalDateTime createdAt;
}


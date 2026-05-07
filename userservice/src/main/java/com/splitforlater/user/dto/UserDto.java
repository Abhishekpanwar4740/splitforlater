package com.splitforlater.user.dto;

import lombok.*;

import java.util.UUID;

@Data // Combines @Getter, @Setter, @ToString, @EqualsAndHashCode
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto{
    private UUID id;
    private String name;
    private String email;
    private String imgUrl;
    private String defaultCurrency;
}
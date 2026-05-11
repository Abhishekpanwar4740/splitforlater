package com.splitforlater.common.dto;

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
    private UserDto payeeUser;
    private GroupDto group;
    private UserDto creditorUser;
    private Long amount;
}

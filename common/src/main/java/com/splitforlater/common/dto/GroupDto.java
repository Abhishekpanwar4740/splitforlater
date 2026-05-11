package com.splitforlater.common.dto;

import lombok.Data;

import java.util.List;
import java.util.UUID;
@Data
public class GroupDto {
    UUID groupId;
    String groupName;
    String groupType;
    List<UUID> users;
    List<UUID> expenses;
}

package com.splitforlater.groupservice.controller;

import com.splitforlater.groupservice.dto.AddMemberRequestDto;
import com.splitforlater.groupservice.dto.GroupRequestDto;
import com.splitforlater.groupservice.entity.Group;
import com.splitforlater.groupservice.service.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {
    private final GroupService groupService;

    @PostMapping
    public ResponseEntity<Group> create(@Valid @RequestBody GroupRequestDto dto,
                                        @RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(groupService.createGroup(dto, userId));
    }

    @PostMapping("/{groupId}/members")
    public ResponseEntity<Void> addMember(@PathVariable UUID groupId,
                                          @Valid @RequestBody AddMemberRequestDto dto) {
        groupService.addMemberToGroup(groupId, dto.getUserId());
        return ResponseEntity.ok().build();
    }
}

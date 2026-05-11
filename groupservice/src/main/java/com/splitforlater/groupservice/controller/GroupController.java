package com.splitforlater.groupservice.controller;

import com.splitforlater.groupservice.dto.AddMemberRequestDto;
import com.splitforlater.groupservice.dto.GroupRequestDto;
import com.splitforlater.groupservice.entity.Group;
import com.splitforlater.groupservice.service.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
@Tag(name = "Group Management", description = "APIs for managing groups and their members")
public class GroupController {
    private final GroupService groupService;
    @Operation(summary = "Create a new group", description = "Creates a new group with the provided details. The user creating the group will be set as the owner.")
    @PostMapping
    public ResponseEntity<Group> create(@Valid @RequestBody GroupRequestDto dto,
                                        @RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(groupService.createGroup(dto, userId));
    }
    @Operation(summary = "Add a member to a group")
    @PostMapping("/{groupId}/members")
    public ResponseEntity<Void> addMember(
            @PathVariable UUID groupId,
            @Valid @RequestBody AddMemberRequestDto dto,
            @RequestHeader("X-User-Id") UUID requesterId) { // ADDED THIS

        // Ensure you pass requesterId to the service so it can check if the requester is allowed to add people
        groupService.addMemberToGroup(groupId, dto.getUserId(), requesterId);
        return ResponseEntity.ok().build();
    }
    @Operation(summary = "Get group details", description = "Retrieves the details of a specific group. Only group members can view the group.")
    @GetMapping("/{groupId}")
    public ResponseEntity<Group> getGroup(@PathVariable UUID groupId,
                                          @RequestHeader("X-User-Id") UUID requesterId) {
        // Passing requesterId to ensure only group members can view the group
        return ResponseEntity.ok(groupService.getGroupById(groupId, requesterId));
    }
    @Operation(summary = "Update group details", description = "Updates the name and description of the group. Only the group owner can update the group.")
    @PutMapping("/{groupId}")
    public ResponseEntity<Group> updateGroup(@PathVariable UUID groupId,
                                             @Valid @RequestBody GroupRequestDto dto,
                                             @RequestHeader("X-User-Id") UUID requesterId) {
        return ResponseEntity.ok(groupService.updateGroup(groupId, dto, requesterId));
    }
    @Operation(summary = "Delete a group", description = "Deletes the specified group. Only the group owner can delete the group.")
    @DeleteMapping("/{groupId}")
    public ResponseEntity<Void> deleteGroup(@PathVariable UUID groupId,
                                            @RequestHeader("X-User-Id") UUID requesterId) {
        try{
            groupService.deleteGroup(groupId, requesterId);
            return ResponseEntity.noContent().build();
        } catch (SecurityException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.METHOD_NOT_ALLOWED);
        }
    }
    @Operation(summary = "Remove a member from a group", description = "Removes a user from the specified group. Only the group owner can remove members.")
    @DeleteMapping("/{groupId}/members/{memberId}")
    public ResponseEntity<Void> removeMember(@PathVariable UUID groupId,
                                             @PathVariable UUID memberId,
                                             @RequestHeader("X-User-Id") UUID requesterId) {
        try {
            groupService.removeMemberFromGroup(groupId, memberId, requesterId);
            return ResponseEntity.noContent().build();
        } catch (SecurityException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.METHOD_NOT_ALLOWED);
        }
    }
}

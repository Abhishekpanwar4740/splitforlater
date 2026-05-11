package com.splitforlater.user.controller;

import com.splitforlater.user.dto.UserDto;
import com.splitforlater.common.exceptionhandling.UserNotFoundException;
import com.splitforlater.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

        import java.util.UUID;

@RestController
@RequestMapping("/api/user")
@Tag(name = "User Service", description = "Endpoints for managing users")
public class UserController {

    @Autowired
    private UserService userService;

    @Operation(summary = "Get current user info or register if new")
    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader("X-User-Email") String email,
            @RequestHeader(value = "X-User-Name", required = false) String name) {

        try {
            UserDto user = userService.getUserByEmail(email);
            return ResponseEntity.ok(user);
        } catch (UserNotFoundException e) {
            // First time login: Create the user using headers passed from Gateway
            UserDto newUser = userService.createNewUser(email, name);
            return ResponseEntity.ok(newUser);
        }
    }

    @Operation(summary = "Update user info. User can only update their own info.")
    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(
            @PathVariable UUID id,
            @RequestBody UserDto userDetails,
            @RequestHeader("X-User-Id") UUID requesterId) { // Added Header!

        // Simple security check without needing the DB!
        if (!id.equals(requesterId)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        try {
            UserDto updatedUser = userService.updateUser(id, userDetails);
            return new ResponseEntity<>(updatedUser, HttpStatus.OK);
        } catch (UserNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "Delete user. User can only delete their own account.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable UUID id,
            @RequestHeader("X-User-Id") UUID requesterId) { // Added Header!

        if (!id.equals(requesterId)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        try {
            userService.deleteUser(id);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (UserNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
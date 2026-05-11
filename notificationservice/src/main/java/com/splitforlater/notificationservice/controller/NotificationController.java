package com.splitforlater.notificationservice.controller;

import com.splitforlater.notificationservice.entity.NotificationLog;
import com.splitforlater.notificationservice.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification Service", description = "Endpoints for managing notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "Get notifications for a specific group")
    public ResponseEntity<List<NotificationLog>> getNotificationForGroup(
            @RequestParam(name = "groupId") UUID groupId,
            @RequestHeader("X-User-Id") UUID requesterId) { // Added Security Header

        // In the service layer, check if 'requesterId' is a member of 'groupId'
        // before returning the notification logs!
        return new ResponseEntity<>(notificationService.getNotificationForGroup(groupId, requesterId), HttpStatus.OK);
    }
}
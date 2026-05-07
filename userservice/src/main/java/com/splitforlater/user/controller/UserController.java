package com.splitforlater.user.controller;

import com.splitforlater.user.entity.User;
import com.splitforlater.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(@AuthenticationPrincipal OAuth2User principal) {
        String email = principal.getAttribute("email");
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> createNewUser(principal)); // Automatic registration on first login
        return ResponseEntity.ok(user);
    }

    private User createNewUser(OAuth2User principal) {
        User user = new User();
        user.setName(principal.getAttribute("name"));
        user.setEmail(principal.getAttribute("email"));
        user.setImgUrl(principal.getAttribute("picture"));
        return userRepository.save(user);
    }
}

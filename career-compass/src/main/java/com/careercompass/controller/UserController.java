package com.careercompass.controller;

import com.careercompass.dto.UpdateUsernameRequest;
import com.careercompass.dto.UserResponse;
import com.careercompass.entity.User;
import com.careercompass.repository.UserRepository;
import com.careercompass.security.CurrentUserProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Account management (distinct from AuthController, which only handles
 * login/register/password-reset). Deleting a user cascades automatically
 * to their resumes, job descriptions, matches, roadmaps, and chat messages
 * via the ON DELETE CASCADE foreign keys already in place since V1/V7 —
 * no manual cleanup needed here.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;

    public UserController(UserRepository userRepository, CurrentUserProvider currentUserProvider) {
        this.userRepository = userRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @PutMapping("/username")
    public ResponseEntity<UserResponse> updateUsername(@RequestBody UpdateUsernameRequest request) {
        User user = userRepository.findById(currentUserProvider.getUserId())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));

        if (!request.username().equals(user.getUsername())
                && userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("Username already taken: " + request.username());
        }

        user.setUsername(request.username());
        User saved = userRepository.save(user);

        return ResponseEntity.ok(new UserResponse(saved.getId(), saved.getEmail(), saved.getUsername()));
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteAccount() {
        userRepository.deleteById(currentUserProvider.getUserId());
        return ResponseEntity.noContent().build();
    }
}
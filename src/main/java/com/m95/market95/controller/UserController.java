package com.m95.market95.controller;

import com.m95.market95.model.User;
import com.m95.market95.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // Register new user
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            return ResponseEntity.badRequest().body("Username is already taken");
        }
        // Hash password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return ResponseEntity.ok("User registered successfully");
    }

    // Login user (simple version without JWT/session)
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody User loginUser) {
        Optional<User> userOptional = userRepository.findByUsername(loginUser.getUsername());
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(401).body("Invalid username or password");
        }
        User user = userOptional.get();
        if (!passwordEncoder.matches(loginUser.getPassword(), user.getPassword())) {
            return ResponseEntity.status(401).body("Invalid username or password");
        }
        // In production, return a JWT or session token here
        return ResponseEntity.ok(user);
    }

    // Get user by username (example of protected endpoint)
    @GetMapping("/{username}")
    public ResponseEntity<?> getUser(@PathVariable String username) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        return userOptional
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}

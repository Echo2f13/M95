package com.m95.market95.controller;

import com.m95.market95.model.User;
import com.m95.market95.payload.JwtResponse;
import com.m95.market95.payload.LoginRequest;
import com.m95.market95.payload.RegisterRequest;
import com.m95.market95.repository.UserRepository;
import com.m95.market95.security.JwtUtils;
import com.m95.market95.security.MyUserDetails;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authManager;
    private final UserRepository userRepo;
    private final PasswordEncoder encoder;
    private final JwtUtils jwtUtils;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        if (userRepo.findByUsername(req.getUsername()).isPresent()) {
            return ResponseEntity
                .badRequest()
                .body("Error: Username is already taken!");
        }
        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(encoder.encode(req.getPassword()));
        user.setRoles(List.of("ROLE_USER"));
        userRepo.save(user);
        return ResponseEntity.ok("User registered successfully!");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        try {
            Authentication authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword())
            );
            MyUserDetails userDetails = (MyUserDetails) authentication.getPrincipal();
            String token = jwtUtils.generateToken(userDetails);
            JwtResponse resp = new JwtResponse(
                token,
                userDetails.getUsername(),
                userDetails.getAuthorities()
            );
            return ResponseEntity.ok(resp);

        } catch (BadCredentialsException | UsernameNotFoundException ex) {
            log.warn("Authentication failed for user '{}': {}", req.getUsername(), ex.getMessage());
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body("Invalid username or password");
        } catch (Exception ex) {
            log.error("Unexpected error during login for user '{}'", req.getUsername(), ex);
            // Return the exception message so we can see what’s happening:
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An unexpected error occurred: " + ex.getMessage());
        }
    }
}

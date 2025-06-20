package com.m95.market95.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;
import java.util.List;

@Document(collection = "users")
public class User {
    @Id
    private String userId;            // Unique user ID
    private String username;          // Username for login
    private String password;          // Encrypted password
    private String email;             // Optional email field
    private List<String> favStocks;   // List of favorite stocks (symbols)
    private Instant createdAt;        // Timestamp for account creation

    public User() {}

    public User(String username, String password, String email, List<String> favStocks) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.favStocks = favStocks;
        this.createdAt = Instant.now();
    }

    // Getters and setters

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public List<String> getFavStocks() { return favStocks; }
    public void setFavStocks(List<String> favStocks) { this.favStocks = favStocks; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}

package com.m95.market95.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtils {

    // Base64-encoded 256-bit key from application.properties
    @Value("${jwt.secret}")
    private String jwtSecret;

    // Expiration time (ms)
    @Value("${jwt.expirationMs}")
    private int jwtExpirationMs;

    // The decoded key
    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        // Decode the Base64 secret to raw bytes, then build an HS256 key
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generate a JWT for the given user.
     */
    public String generateToken(UserDetails userDetails) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + jwtExpirationMs))
                .signWith(secretKey)
                .compact();
    }

    /**
     * Extract the username (subject) from the token.
     */
    public String getUsernameFromJwt(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    /**
     * Validate the token against the given user details.
     */
    public boolean validateJwt(String token, UserDetails userDetails) {
        try {
            String username = getUsernameFromJwt(token);
            Date expiration = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getExpiration();
            return username.equals(userDetails.getUsername())
                    && expiration.after(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            // invalid signature, malformed, expired, etc.
            return false;
        }
    }
}

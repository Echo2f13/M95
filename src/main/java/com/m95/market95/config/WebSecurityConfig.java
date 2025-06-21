// src/main/java/com/m95/market95/config/WebSecurityConfig.java
package com.m95.market95.config;

import com.m95.market95.repository.UserRepository;
import com.m95.market95.security.JwtAuthenticationFilter;
import com.m95.market95.security.MyUserDetails;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
public class WebSecurityConfig {

    // 1) Expose a UserDetailsService bean backed by Mongo
    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepo) {
        return username -> userRepo.findByUsername(username)
            .map(MyUserDetails::new)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    // 2) Build the single SecurityFilterChain, injecting your JwtAuthenticationFilter
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           JwtAuthenticationFilter jwtAuthFilter) throws Exception {

        http
          // enable CORS
          .cors(cors -> cors.configurationSource(corsConfigurationSource()))
          // disable CSRF since we're stateless
          .csrf(csrf -> csrf.disable())
          // URL rules
          .authorizeHttpRequests(auth -> auth
              // public: auth and static content
              .requestMatchers("/api/auth/**", "/", "/index.html", "/static/**").permitAll()
              // only ADMIN role can hit /api/admin/**
              .requestMatchers("/api/admin/**").hasRole("ADMIN")
              // only authenticated users can hit favorites endpoints
              .requestMatchers("/api/fav-stocks/**").authenticated()
              // all other /api/** also require auth
              .requestMatchers("/api/**").authenticated()
              // everything else is open
              .anyRequest().permitAll()
          )
          // stateless session (no cookie-based sessions)
          .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
          // our JWT filter runs before the UsernamePasswordAuthenticationFilter
          .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // 3) Expose the AuthenticationManager
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }

    // 4) BCrypt PasswordEncoder
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 5) CORS configuration to allow your React front-end
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:3000"));  // React dev URL
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}

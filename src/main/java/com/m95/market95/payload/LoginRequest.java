package com.m95.market95.payload;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
}

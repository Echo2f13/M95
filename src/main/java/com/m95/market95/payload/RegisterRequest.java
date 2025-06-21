package com.m95.market95.payload;

import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String password;
}

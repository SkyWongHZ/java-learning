package com.example.springbootdemo.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class IssuedTokenDTO {

    private String token;
    private LocalDateTime expiresAt;
}

package com.example.springbootdemo.web.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CurrentUser {

    private Long id;
    private String username;
    private String displayName;
    private String token;
    private Integer systemType;
}

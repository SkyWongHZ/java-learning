package com.example.springbootdemo.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StudentPageRowDTO {

    private Long id;
    private String studentNo;
    private String name;
    private Integer gender;
    private String phone;
    private Long classId;
    private String classCode;
    private String className;
    private LocalDateTime gmtCreate;
    private LocalDateTime gmtModify;
}

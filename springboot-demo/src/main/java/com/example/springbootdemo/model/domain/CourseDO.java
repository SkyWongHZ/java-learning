package com.example.springbootdemo.model.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("course")
public class CourseDO {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String courseCode;
    private String courseName;
    private LocalDateTime gmtCreate;
    private LocalDateTime gmtModify;

    @TableLogic
    private Integer deleted;
}

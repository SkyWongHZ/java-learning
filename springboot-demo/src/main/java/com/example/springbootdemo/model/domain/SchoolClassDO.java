package com.example.springbootdemo.model.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("school_class")
public class SchoolClassDO {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String classCode;
    private String className;
    private LocalDateTime gmtCreate;
    private LocalDateTime gmtModify;

    @TableLogic
    private Integer deleted;
}

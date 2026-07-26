package com.example.springbootdemo.model.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("student")
public class StudentDO {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String studentNo;
    private String name;
    private Integer gender;
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String phone;
    private Long classId;
    private LocalDateTime gmtCreate;
    private LocalDateTime gmtModify;

    @TableLogic
    private Integer deleted;
}

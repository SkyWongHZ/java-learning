package com.example.springbootdemo.model.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("demo_user")
public class UserDO {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String displayName;
    private LocalDateTime gmtModify;
    private LocalDateTime gmtCreate;

    @TableLogic
    private Integer deleted;
}

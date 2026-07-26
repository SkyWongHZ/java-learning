package com.example.springbootdemo.model.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_auth_token")
public class AuthTokenDO {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String token;
    private Integer systemType;
    private LocalDateTime gmtModify;
    private LocalDateTime gmtCreate;

    @TableLogic
    private Integer deleted;
}

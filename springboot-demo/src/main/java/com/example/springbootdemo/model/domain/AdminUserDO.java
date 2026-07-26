package com.example.springbootdemo.model.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_admin_user")
public class AdminUserDO {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String displayName;
    private String passwordHash;
    private Integer status;
    private Integer failedLoginCount;
    private LocalDateTime lastFailedLoginTime;
    private LocalDateTime lockedUntil;
    private String lastLoginIp;
    private LocalDateTime lastLoginTime;
    private LocalDateTime gmtModify;
    private LocalDateTime gmtCreate;

    @TableLogic
    private Integer deleted;
}

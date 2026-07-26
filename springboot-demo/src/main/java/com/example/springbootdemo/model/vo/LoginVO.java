package com.example.springbootdemo.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@ApiModel("管理员登录结果")
public class LoginVO {

    @ApiModelProperty("鉴权令牌")
    private String token;

    @ApiModelProperty("管理员 ID")
    private Long uid;

    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("展示名称")
    private String displayName;

    @ApiModelProperty("客户端系统类型")
    private Integer systemType;

    @ApiModelProperty("令牌过期时间")
    private LocalDateTime expiresAt;
}

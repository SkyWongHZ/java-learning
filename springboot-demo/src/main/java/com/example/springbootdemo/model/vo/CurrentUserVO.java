package com.example.springbootdemo.model.vo;

import com.example.springbootdemo.web.auth.CurrentUser;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@ApiModel("当前登录管理员")
public class CurrentUserVO {

    @ApiModelProperty("管理员 ID")
    private Long id;

    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("展示名称")
    private String displayName;

    @ApiModelProperty("当前客户端系统类型")
    private Integer systemType;

    public static CurrentUserVO from(CurrentUser currentUser) {
        return new CurrentUserVO(
                currentUser.getId(),
                currentUser.getUsername(),
                currentUser.getDisplayName(),
                currentUser.getSystemType());
    }
}

package com.example.springbootdemo.model.vo;

import com.example.springbootdemo.model.domain.UserDO;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@ApiModel("用户信息")
public class UserVO {

    @ApiModelProperty("用户 ID")
    private Long id;
    @ApiModelProperty("用户名")
    private String username;
    @ApiModelProperty("展示名称")
    private String displayName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    @ApiModelProperty("创建时间")
    private LocalDateTime gmtCreate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    @ApiModelProperty("更新时间")
    private LocalDateTime gmtModify;

    public static UserVO from(UserDO user) {
        return UserVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .displayName(user.getDisplayName())
                .gmtCreate(user.getGmtCreate())
                .gmtModify(user.getGmtModify())
                .build();
    }
}

package com.example.springbootdemo.model.form;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@ApiModel("管理员登录请求")
public class LoginForm {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 2, max = 50, message = "用户名长度必须为 2 到 50 个字符")
    @ApiModelProperty(value = "管理员用户名", required = true, example = "admin")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(max = 128, message = "密码长度不能超过 128 个字符")
    @ApiModelProperty(value = "管理员密码", required = true, example = "仅在本地输入，不要写入代码")
    private String password;
}

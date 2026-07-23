package com.example.springbootdemo.model.form;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@ApiModel("创建用户请求")
public class CreateUserForm {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 2, max = 50, message = "用户名长度必须为 2 到 50 个字符")
    @ApiModelProperty(value = "用户名", required = true, example = "sky")
    private String username;

    @NotBlank(message = "展示名称不能为空")
    @Size(max = 100, message = "展示名称长度不能超过 100 个字符")
    @ApiModelProperty(value = "展示名称", required = true, example = "Sky Wang")
    private String displayName;
}

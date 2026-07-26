package com.example.springbootdemo.model.form;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@ApiModel("新增班级请求")
public class CreateClassForm {

    @NotBlank(message = "班级编码不能为空")
    @Size(min = 2, max = 32, message = "班级编码长度必须为 2 到 32 个字符")
    @ApiModelProperty(value = "班级编码", required = true, example = "CS-2026-01")
    private String classCode;

    @NotBlank(message = "班级名称不能为空")
    @Size(max = 100, message = "班级名称长度不能超过 100 个字符")
    @ApiModelProperty(value = "班级名称", required = true, example = "2026级计算机1班")
    private String className;
}

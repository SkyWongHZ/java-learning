package com.example.springbootdemo.model.form;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@ApiModel("修改课程请求")
public class UpdateCourseForm {

    @NotBlank(message = "课程编码不能为空")
    @Size(min = 2, max = 32, message = "课程编码长度必须为 2 到 32 个字符")
    @ApiModelProperty(value = "课程编码", required = true, example = "JAVA-101")
    private String courseCode;

    @NotBlank(message = "课程名称不能为空")
    @Size(max = 100, message = "课程名称长度不能超过 100 个字符")
    @ApiModelProperty(value = "课程名称", required = true, example = "Java程序设计")
    private String courseName;
}

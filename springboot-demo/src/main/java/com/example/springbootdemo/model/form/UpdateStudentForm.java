package com.example.springbootdemo.model.form;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Data
@ApiModel("修改学生请求")
public class UpdateStudentForm {

    @NotBlank(message = "学生姓名不能为空")
    @Size(max = 50, message = "学生姓名长度不能超过 50 个字符")
    @ApiModelProperty(value = "学生姓名", required = true, example = "张三")
    private String name;

    @NotNull(message = "性别不能为空")
    @Min(value = 0, message = "性别只能为 0、1、2")
    @Max(value = 2, message = "性别只能为 0、1、2")
    @ApiModelProperty(value = "性别：0 未知，1 男，2 女", required = true, example = "1")
    private Integer gender;

    @Size(max = 20, message = "手机号长度不能超过 20 个字符")
    @ApiModelProperty(value = "中国大陆手机号", example = "13800138000")
    private String phone;

    @NotNull(message = "班级 ID 不能为空")
    @Min(value = 1, message = "班级 ID 必须大于 0")
    @ApiModelProperty(value = "班级 ID", required = true, example = "1")
    private Long classId;

    @Valid
    @NotNull(message = "课程 ID 集合不能为空")
    @ApiModelProperty(value = "课程 ID 集合", required = true)
    private List<@NotNull(message = "课程 ID 不能为空")
            @Min(value = 1, message = "课程 ID 必须大于 0") Long> courseIds;
}

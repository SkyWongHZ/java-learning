package com.example.springbootdemo.model.query;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Size;

@Data
@ApiModel("课程查询参数")
public class CourseQuery {

    @Size(max = 100, message = "关键字长度不能超过 100 个字符")
    @ApiModelProperty("课程编码或课程名称关键字")
    private String keyword;
}

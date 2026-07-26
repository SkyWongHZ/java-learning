package com.example.springbootdemo.model.query;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Size;

@Data
@ApiModel("班级查询参数")
public class ClassQuery {

    @Size(max = 100, message = "关键字长度不能超过 100 个字符")
    @ApiModelProperty("班级编码或班级名称关键字")
    private String keyword;
}

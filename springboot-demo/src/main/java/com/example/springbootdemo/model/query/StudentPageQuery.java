package com.example.springbootdemo.model.query;

import com.example.springbootdemo.model.common.BaseQuery;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel("学生分页查询参数")
public class StudentPageQuery extends BaseQuery {

    @Size(max = 100, message = "关键字长度不能超过 100 个字符")
    @ApiModelProperty("学号或姓名关键字")
    private String keyword;

    @Min(value = 1, message = "班级 ID 必须大于 0")
    @ApiModelProperty("班级 ID")
    private Long classId;

    @Min(value = 1, message = "课程 ID 必须大于 0")
    @ApiModelProperty("课程 ID")
    private Long courseId;
}

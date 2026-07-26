package com.example.springbootdemo.model.vo;

import com.example.springbootdemo.model.domain.CourseDO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@ApiModel("课程精简信息")
public class CourseSimpleVO {

    @ApiModelProperty("课程 ID")
    private Long id;
    @ApiModelProperty("课程编码")
    private String courseCode;
    @ApiModelProperty("课程名称")
    private String courseName;

    public static CourseSimpleVO from(CourseDO course) {
        return CourseSimpleVO.builder()
                .id(course.getId())
                .courseCode(course.getCourseCode())
                .courseName(course.getCourseName())
                .build();
    }
}

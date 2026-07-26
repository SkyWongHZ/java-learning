package com.example.springbootdemo.model.vo;

import com.example.springbootdemo.model.domain.CourseDO;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@ApiModel("课程信息")
public class CourseVO {

    @ApiModelProperty("课程 ID")
    private Long id;
    @ApiModelProperty("课程编码")
    private String courseCode;
    @ApiModelProperty("课程名称")
    private String courseName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    @ApiModelProperty("创建时间")
    private LocalDateTime gmtCreate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    @ApiModelProperty("更新时间")
    private LocalDateTime gmtModify;

    public static CourseVO from(CourseDO course) {
        return CourseVO.builder()
                .id(course.getId())
                .courseCode(course.getCourseCode())
                .courseName(course.getCourseName())
                .gmtCreate(course.getGmtCreate())
                .gmtModify(course.getGmtModify())
                .build();
    }
}

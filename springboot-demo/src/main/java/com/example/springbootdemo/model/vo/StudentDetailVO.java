package com.example.springbootdemo.model.vo;

import com.example.springbootdemo.model.domain.StudentDO;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@ApiModel("学生详情")
public class StudentDetailVO {

    @ApiModelProperty("学生 ID")
    private Long id;
    @ApiModelProperty("学号")
    private String studentNo;
    @ApiModelProperty("学生姓名")
    private String name;
    @ApiModelProperty("性别：0 未知，1 男，2 女")
    private Integer gender;
    @ApiModelProperty("手机号")
    private String phone;
    @ApiModelProperty("所属班级")
    private ClassSimpleVO classInfo;
    @ApiModelProperty("已选课程")
    private List<CourseSimpleVO> courses;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    @ApiModelProperty("创建时间")
    private LocalDateTime gmtCreate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    @ApiModelProperty("更新时间")
    private LocalDateTime gmtModify;

    public static StudentDetailVO from(
            StudentDO student,
            ClassSimpleVO classInfo,
            List<CourseSimpleVO> courses) {
        return StudentDetailVO.builder()
                .id(student.getId())
                .studentNo(student.getStudentNo())
                .name(student.getName())
                .gender(student.getGender())
                .phone(student.getPhone())
                .classInfo(classInfo)
                .courses(courses)
                .gmtCreate(student.getGmtCreate())
                .gmtModify(student.getGmtModify())
                .build();
    }
}

package com.example.springbootdemo.model.vo;

import com.example.springbootdemo.model.dto.StudentPageRowDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@ApiModel("学生分页列表项")
public class StudentPageItemVO {

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

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    @ApiModelProperty("创建时间")
    private LocalDateTime gmtCreate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    @ApiModelProperty("更新时间")
    private LocalDateTime gmtModify;

    public static StudentPageItemVO from(StudentPageRowDTO row) {
        return StudentPageItemVO.builder()
                .id(row.getId())
                .studentNo(row.getStudentNo())
                .name(row.getName())
                .gender(row.getGender())
                .phone(row.getPhone())
                .classInfo(ClassSimpleVO.builder()
                        .id(row.getClassId())
                        .classCode(row.getClassCode())
                        .className(row.getClassName())
                        .build())
                .gmtCreate(row.getGmtCreate())
                .gmtModify(row.getGmtModify())
                .build();
    }
}

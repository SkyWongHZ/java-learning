package com.example.springbootdemo.model.vo;

import com.example.springbootdemo.model.domain.SchoolClassDO;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@ApiModel("班级信息")
public class ClassVO {

    @ApiModelProperty("班级 ID")
    private Long id;
    @ApiModelProperty("班级编码")
    private String classCode;
    @ApiModelProperty("班级名称")
    private String className;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    @ApiModelProperty("创建时间")
    private LocalDateTime gmtCreate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    @ApiModelProperty("更新时间")
    private LocalDateTime gmtModify;

    public static ClassVO from(SchoolClassDO schoolClass) {
        return ClassVO.builder()
                .id(schoolClass.getId())
                .classCode(schoolClass.getClassCode())
                .className(schoolClass.getClassName())
                .gmtCreate(schoolClass.getGmtCreate())
                .gmtModify(schoolClass.getGmtModify())
                .build();
    }
}

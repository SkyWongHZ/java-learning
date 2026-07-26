package com.example.springbootdemo.model.vo;

import com.example.springbootdemo.model.domain.SchoolClassDO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@ApiModel("班级精简信息")
public class ClassSimpleVO {

    @ApiModelProperty("班级 ID")
    private Long id;
    @ApiModelProperty("班级编码")
    private String classCode;
    @ApiModelProperty("班级名称")
    private String className;

    public static ClassSimpleVO from(SchoolClassDO schoolClass) {
        return ClassSimpleVO.builder()
                .id(schoolClass.getId())
                .classCode(schoolClass.getClassCode())
                .className(schoolClass.getClassName())
                .build();
    }
}

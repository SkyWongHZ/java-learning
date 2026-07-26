package com.example.springbootdemo.model.common;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@ApiModel("分页查询公共参数")
public class BaseQuery implements Serializable {

    @NotNull(message = "页码不能为空")
    @Min(value = 1, message = "页码不能小于 1")
    @ApiModelProperty(value = "页码", required = true, example = "1")
    private Integer pageNum;

    @NotNull(message = "页面尺寸不能为空")
    @Min(value = 1, message = "页面尺寸不能小于 1")
    @ApiModelProperty(value = "页面尺寸", required = true, example = "10")
    private Integer pageSize;
}

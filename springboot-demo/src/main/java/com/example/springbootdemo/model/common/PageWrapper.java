package com.example.springbootdemo.model.common;

import com.github.pagehelper.PageInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("分页响应")
public class PageWrapper<T> implements Serializable {

    @ApiModelProperty("当前页")
    private int pageNum;

    @ApiModelProperty("每页数量")
    private int pageSize;

    @ApiModelProperty("总记录数")
    private long total;

    @ApiModelProperty("总页数")
    private int pages;

    @ApiModelProperty("当前页数据")
    private List<T> list;

    @ApiModelProperty("是否为最后一页")
    private Boolean boolLastPage;

    public static <V> PageWrapper<V> convert(PageInfo<?> pageInfo, List<V> list) {
        return new PageWrapper<>(
                pageInfo.getPageNum(),
                pageInfo.getPageSize(),
                pageInfo.getTotal(),
                pageInfo.getPages(),
                list == null ? new ArrayList<>() : list,
                pageInfo.isIsLastPage());
    }
}

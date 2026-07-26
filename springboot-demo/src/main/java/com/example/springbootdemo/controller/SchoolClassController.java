package com.example.springbootdemo.controller;

import com.example.springbootdemo.model.common.Response;
import com.example.springbootdemo.model.form.CreateClassForm;
import com.example.springbootdemo.model.form.UpdateClassForm;
import com.example.springbootdemo.model.query.ClassQuery;
import com.example.springbootdemo.model.vo.ClassVO;
import com.example.springbootdemo.service.SchoolClassService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;

@Api(tags = "班级管理")
@Validated
@RestController
@RequestMapping("/api/v1/classes")
public class SchoolClassController {

    private final SchoolClassService schoolClassService;

    public SchoolClassController(SchoolClassService schoolClassService) {
        this.schoolClassService = schoolClassService;
    }

    @ApiOperation("查询全部班级")
    @GetMapping
    public Response<List<ClassVO>> list(@Valid ClassQuery query) {
        return Response.success(schoolClassService.list(query));
    }

    @ApiOperation("新增班级")
    @PostMapping
    public Response<ClassVO> create(@Valid @RequestBody CreateClassForm form) {
        return Response.success(schoolClassService.create(form));
    }

    @ApiOperation("修改班级")
    @PutMapping("/{id}")
    public Response<ClassVO> update(
            @PathVariable @Min(value = 1, message = "班级 ID 必须大于 0") long id,
            @Valid @RequestBody UpdateClassForm form) {
        return Response.success(schoolClassService.update(id, form));
    }

    @ApiOperation("删除班级")
    @DeleteMapping("/{id}")
    public Response<Boolean> delete(
            @PathVariable @Min(value = 1, message = "班级 ID 必须大于 0") long id) {
        return Response.success(schoolClassService.delete(id));
    }
}

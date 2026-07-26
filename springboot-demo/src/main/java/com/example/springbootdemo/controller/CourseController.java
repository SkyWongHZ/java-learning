package com.example.springbootdemo.controller;

import com.example.springbootdemo.model.common.Response;
import com.example.springbootdemo.model.form.CreateCourseForm;
import com.example.springbootdemo.model.form.UpdateCourseForm;
import com.example.springbootdemo.model.query.CourseQuery;
import com.example.springbootdemo.model.vo.CourseVO;
import com.example.springbootdemo.service.CourseService;
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

@Api(tags = "课程管理")
@Validated
@RestController
@RequestMapping("/api/v1/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @ApiOperation("查询全部课程")
    @GetMapping
    public Response<List<CourseVO>> list(@Valid CourseQuery query) {
        return Response.success(courseService.list(query));
    }

    @ApiOperation("新增课程")
    @PostMapping
    public Response<CourseVO> create(@Valid @RequestBody CreateCourseForm form) {
        return Response.success(courseService.create(form));
    }

    @ApiOperation("修改课程")
    @PutMapping("/{id}")
    public Response<CourseVO> update(
            @PathVariable @Min(value = 1, message = "课程 ID 必须大于 0") long id,
            @Valid @RequestBody UpdateCourseForm form) {
        return Response.success(courseService.update(id, form));
    }

    @ApiOperation("删除课程")
    @DeleteMapping("/{id}")
    public Response<Boolean> delete(
            @PathVariable @Min(value = 1, message = "课程 ID 必须大于 0") long id) {
        return Response.success(courseService.delete(id));
    }
}

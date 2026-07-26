package com.example.springbootdemo.controller;

import com.example.springbootdemo.model.common.PageWrapper;
import com.example.springbootdemo.model.common.Response;
import com.example.springbootdemo.model.form.CreateStudentForm;
import com.example.springbootdemo.model.form.UpdateStudentForm;
import com.example.springbootdemo.model.query.StudentPageQuery;
import com.example.springbootdemo.model.vo.StudentDetailVO;
import com.example.springbootdemo.model.vo.StudentPageItemVO;
import com.example.springbootdemo.service.StudentService;
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

@Api(tags = "学生管理")
@Validated
@RestController
@RequestMapping("/api/v1/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @ApiOperation("分页查询学生")
    @GetMapping
    public Response<PageWrapper<StudentPageItemVO>> pageList(
            @Valid StudentPageQuery query) {
        return Response.success(studentService.pageList(query));
    }

    @ApiOperation("查询学生详情")
    @GetMapping("/{id}")
    public Response<StudentDetailVO> get(
            @PathVariable @Min(value = 1, message = "学生 ID 必须大于 0") long id) {
        return Response.success(studentService.get(id));
    }

    @ApiOperation("新增学生")
    @PostMapping
    public Response<StudentDetailVO> create(
            @Valid @RequestBody CreateStudentForm form) {
        return Response.success(studentService.create(form));
    }

    @ApiOperation("修改学生")
    @PutMapping("/{id}")
    public Response<StudentDetailVO> update(
            @PathVariable @Min(value = 1, message = "学生 ID 必须大于 0") long id,
            @Valid @RequestBody UpdateStudentForm form) {
        return Response.success(studentService.update(id, form));
    }

    @ApiOperation("删除学生")
    @DeleteMapping("/{id}")
    public Response<Boolean> delete(
            @PathVariable @Min(value = 1, message = "学生 ID 必须大于 0") long id) {
        return Response.success(studentService.delete(id));
    }
}

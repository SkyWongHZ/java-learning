package com.example.springbootdemo.controller;

import com.example.springbootdemo.model.common.Response;
import com.example.springbootdemo.model.form.CreateUserForm;
import com.example.springbootdemo.model.form.UpdateUserForm;
import com.example.springbootdemo.model.vo.UserVO;
import com.example.springbootdemo.service.UserService;
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

@Api(tags = "用户管理")
@Validated
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @ApiOperation("创建用户")
    @PostMapping
    public Response<UserVO> createUser(@Valid @RequestBody CreateUserForm form) {
        return Response.success(userService.createUser(form));
    }

    @ApiOperation("查询用户")
    @GetMapping("/{id}")
    public Response<UserVO> getUser(
            @PathVariable @Min(value = 1, message = "用户 ID 必须大于 0") long id) {
        return Response.success(userService.getUser(id));
    }

    @ApiOperation("修改用户")
    @PutMapping("/{id}")
    public Response<UserVO> updateUser(
            @PathVariable @Min(value = 1, message = "用户 ID 必须大于 0") long id,
            @Valid @RequestBody UpdateUserForm form) {
        return Response.success(userService.updateUser(id, form));
    }

    @ApiOperation("删除用户")
    @DeleteMapping("/{id}")
    public Response<Boolean> deleteUser(
            @PathVariable @Min(value = 1, message = "用户 ID 必须大于 0") long id) {
        return Response.success(userService.deleteUser(id));
    }
}

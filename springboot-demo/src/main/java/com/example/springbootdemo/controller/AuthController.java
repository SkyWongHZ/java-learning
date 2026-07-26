package com.example.springbootdemo.controller;

import com.example.springbootdemo.model.common.Response;
import com.example.springbootdemo.model.form.LoginForm;
import com.example.springbootdemo.model.vo.CurrentUserVO;
import com.example.springbootdemo.model.vo.LoginVO;
import com.example.springbootdemo.service.AuthService;
import com.example.springbootdemo.web.auth.ClientSystemResolver;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@Api(tags = "登录鉴权")
@Validated
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final ClientSystemResolver clientSystemResolver;

    public AuthController(
            AuthService authService,
            ClientSystemResolver clientSystemResolver) {
        this.authService = authService;
        this.clientSystemResolver = clientSystemResolver;
    }

    @ApiOperation("管理员登录")
    @PostMapping("/login")
    public Response<LoginVO> login(
            @Valid @RequestBody LoginForm form,
            HttpServletRequest request) {
        return Response.success(authService.login(
                form,
                request.getRemoteAddr(),
                clientSystemResolver.resolve(request)));
    }

    @ApiOperation("查询当前登录管理员")
    @GetMapping("/me")
    public Response<CurrentUserVO> currentUser() {
        return Response.success(authService.currentUser());
    }

    @ApiOperation("退出登录")
    @PostMapping("/logout")
    public Response<Boolean> logout() {
        return Response.success(authService.logout());
    }
}

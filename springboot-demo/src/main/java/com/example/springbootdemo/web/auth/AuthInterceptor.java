package com.example.springbootdemo.web.auth;

import com.example.springbootdemo.service.AuthTokenService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    public static final String TOKEN_HEADER = "token";
    private static final String BEARER_PREFIX = "Bearer ";

    private final AuthTokenService authTokenService;
    private final ClientSystemResolver clientSystemResolver;

    public AuthInterceptor(
            AuthTokenService authTokenService,
            ClientSystemResolver clientSystemResolver) {
        this.authTokenService = authTokenService;
        this.clientSystemResolver = clientSystemResolver;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) {
        AuthContext.clear();
        if (HttpMethod.OPTIONS.matches(request.getMethod()) || !(handler instanceof HandlerMethod)) {
            return true;
        }

        CurrentUser currentUser = authTokenService.authenticate(
                resolveToken(request),
                clientSystemResolver.resolve(request));
        AuthContext.setCurrentUser(currentUser);
        return true;
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception exception) {
        AuthContext.clear();
    }

    private String resolveToken(HttpServletRequest request) {
        String token = request.getHeader(TOKEN_HEADER);
        if (token != null && !token.trim().isEmpty()) {
            return token.trim();
        }

        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization != null
                && authorization.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
            return authorization.substring(BEARER_PREFIX.length()).trim();
        }
        return null;
    }
}

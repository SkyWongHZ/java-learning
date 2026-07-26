package com.example.springbootdemo.web.auth;

import com.example.springbootdemo.enums.BaseStatusCodeEnum;
import com.example.springbootdemo.exception.BaseException;

public final class AuthContext {

    private static final ThreadLocal<CurrentUser> CURRENT_USER = new ThreadLocal<>();

    private AuthContext() {
    }

    public static void setCurrentUser(CurrentUser currentUser) {
        CURRENT_USER.set(currentUser);
    }

    public static CurrentUser getCurrentUser() {
        return CURRENT_USER.get();
    }

    public static CurrentUser requireCurrentUser() {
        CurrentUser currentUser = CURRENT_USER.get();
        if (currentUser == null) {
            throw new BaseException(BaseStatusCodeEnum.USER_NOT_LOGGED_IN);
        }
        return currentUser;
    }

    public static void clear() {
        CURRENT_USER.remove();
    }
}

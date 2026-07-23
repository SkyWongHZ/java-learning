package com.example.springbootdemo.service;

import cn.hutool.core.lang.Assert;
import com.example.springbootdemo.dao.inter.UserDao;
import com.example.springbootdemo.enums.BaseStatusCodeEnum;
import com.example.springbootdemo.exception.BaseException;
import com.example.springbootdemo.model.domain.UserDO;
import com.example.springbootdemo.model.form.CreateUserForm;
import com.example.springbootdemo.model.form.UpdateUserForm;
import com.example.springbootdemo.model.vo.UserVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class UserService {

    private final UserDao userDao;

    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    @Transactional
    public UserVO createUser(CreateUserForm form) {
        String username = form.getUsername().trim();
        String displayName = form.getDisplayName().trim();
        assertUsernameAvailable(username, null);

        LocalDateTime now = LocalDateTime.now();
        UserDO user = new UserDO();
        user.setUsername(username);
        user.setDisplayName(displayName);
        user.setGmtCreate(now);
        user.setGmtModify(now);
        user.setDeleted(0);
        userDao.save(user);
        return UserVO.from(user);
    }

    public UserVO getUser(long id) {
        return UserVO.from(requireUser(id));
    }

    @Transactional
    public UserVO updateUser(long id, UpdateUserForm form) {
        UserDO user = requireUser(id);
        String username = form.getUsername().trim();
        String displayName = form.getDisplayName().trim();
        assertUsernameAvailable(username, id);

        user.setUsername(username);
        user.setDisplayName(displayName);
        user.setGmtModify(LocalDateTime.now());
        userDao.updateById(user);
        return UserVO.from(user);
    }

    @Transactional
    public Boolean deleteUser(long id) {
        requireUser(id);
        userDao.removeById(id);
        return Boolean.TRUE;
    }

    private UserDO requireUser(long id) {
        return Assert.notNull(
                userDao.getById(id),
                () -> new BaseException(BaseStatusCodeEnum.USER_DOES_NOT_EXIST));
    }

    private void assertUsernameAvailable(String username, Long excludeId) {
        Assert.isTrue(
                !userDao.existsUsernameIncludingDeleted(username, excludeId),
                () -> new BaseException(BaseStatusCodeEnum.BUSINESS_ERROR, "用户名已存在"));
    }
}

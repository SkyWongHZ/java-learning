package com.example.springbootdemo.dao.inter;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.springbootdemo.model.domain.UserDO;

public interface UserDao extends IService<UserDO> {

    boolean existsUsernameIncludingDeleted(String username, Long excludeId);
}

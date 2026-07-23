package com.example.springbootdemo.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springbootdemo.dao.inter.UserDao;
import com.example.springbootdemo.mapper.UserMapper;
import com.example.springbootdemo.model.domain.UserDO;
import org.springframework.stereotype.Repository;

@Repository
public class UserDaoImpl extends ServiceImpl<UserMapper, UserDO> implements UserDao {

    @Override
    public boolean existsUsernameIncludingDeleted(String username, Long excludeId) {
        return baseMapper.countByUsernameIncludingDeleted(username, excludeId) > 0;
    }
}

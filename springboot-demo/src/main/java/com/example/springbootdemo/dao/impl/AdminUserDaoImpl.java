package com.example.springbootdemo.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springbootdemo.dao.inter.AdminUserDao;
import com.example.springbootdemo.mapper.AdminUserMapper;
import com.example.springbootdemo.model.domain.AdminUserDO;
import org.springframework.stereotype.Repository;

@Repository
public class AdminUserDaoImpl extends ServiceImpl<AdminUserMapper, AdminUserDO>
        implements AdminUserDao {
}

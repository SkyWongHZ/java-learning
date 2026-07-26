package com.example.springbootdemo.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springbootdemo.dao.inter.AuthTokenDao;
import com.example.springbootdemo.mapper.AuthTokenMapper;
import com.example.springbootdemo.model.domain.AuthTokenDO;
import org.springframework.stereotype.Repository;

@Repository
public class AuthTokenDaoImpl extends ServiceImpl<AuthTokenMapper, AuthTokenDO>
        implements AuthTokenDao {
}

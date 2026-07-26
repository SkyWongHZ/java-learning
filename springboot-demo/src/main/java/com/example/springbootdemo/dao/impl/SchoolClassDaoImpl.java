package com.example.springbootdemo.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springbootdemo.dao.inter.SchoolClassDao;
import com.example.springbootdemo.mapper.SchoolClassMapper;
import com.example.springbootdemo.model.domain.SchoolClassDO;
import org.springframework.stereotype.Repository;

@Repository
public class SchoolClassDaoImpl
        extends ServiceImpl<SchoolClassMapper, SchoolClassDO>
        implements SchoolClassDao {

    @Override
    public boolean existsClassCodeIncludingDeleted(String classCode, Long excludeId) {
        return baseMapper.countByClassCodeIncludingDeleted(classCode, excludeId) > 0;
    }

    @Override
    public boolean hasActiveStudents(long classId) {
        return baseMapper.countActiveStudents(classId) > 0;
    }
}

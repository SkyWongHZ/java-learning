package com.example.springbootdemo.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springbootdemo.dao.inter.StudentDao;
import com.example.springbootdemo.mapper.StudentMapper;
import com.example.springbootdemo.model.domain.StudentDO;
import com.example.springbootdemo.model.dto.StudentPageRowDTO;
import com.example.springbootdemo.model.query.StudentPageQuery;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class StudentDaoImpl extends ServiceImpl<StudentMapper, StudentDO> implements StudentDao {

    @Override
    public List<StudentPageRowDTO> selectPageRows(StudentPageQuery query) {
        return baseMapper.selectPageRows(query);
    }

    @Override
    public boolean existsStudentNoIncludingDeleted(String studentNo, Long excludeId) {
        return baseMapper.countByStudentNoIncludingDeleted(studentNo, excludeId) > 0;
    }
}

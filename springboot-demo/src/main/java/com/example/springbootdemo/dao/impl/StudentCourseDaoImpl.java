package com.example.springbootdemo.dao.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springbootdemo.dao.inter.StudentCourseDao;
import com.example.springbootdemo.mapper.StudentCourseMapper;
import com.example.springbootdemo.model.domain.CourseDO;
import com.example.springbootdemo.model.domain.StudentCourseDO;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class StudentCourseDaoImpl
        extends ServiceImpl<StudentCourseMapper, StudentCourseDO>
        implements StudentCourseDao {

    @Override
    public List<CourseDO> selectCoursesByStudentId(long studentId) {
        return baseMapper.selectCoursesByStudentId(studentId);
    }

    @Override
    public void removeByStudentId(long studentId) {
        remove(new LambdaQueryWrapper<StudentCourseDO>()
                .eq(StudentCourseDO::getStudentId, studentId));
    }
}

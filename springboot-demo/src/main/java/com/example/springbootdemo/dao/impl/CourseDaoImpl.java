package com.example.springbootdemo.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springbootdemo.dao.inter.CourseDao;
import com.example.springbootdemo.mapper.CourseMapper;
import com.example.springbootdemo.model.domain.CourseDO;
import org.springframework.stereotype.Repository;

@Repository
public class CourseDaoImpl extends ServiceImpl<CourseMapper, CourseDO> implements CourseDao {

    @Override
    public boolean existsCourseCodeIncludingDeleted(String courseCode, Long excludeId) {
        return baseMapper.countByCourseCodeIncludingDeleted(courseCode, excludeId) > 0;
    }

    @Override
    public boolean hasStudentRelations(long courseId) {
        return baseMapper.countStudentRelations(courseId) > 0;
    }
}

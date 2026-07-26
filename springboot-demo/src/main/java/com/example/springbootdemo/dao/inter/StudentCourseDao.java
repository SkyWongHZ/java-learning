package com.example.springbootdemo.dao.inter;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.springbootdemo.model.domain.CourseDO;
import com.example.springbootdemo.model.domain.StudentCourseDO;

import java.util.List;

public interface StudentCourseDao extends IService<StudentCourseDO> {

    List<CourseDO> selectCoursesByStudentId(long studentId);

    void removeByStudentId(long studentId);
}

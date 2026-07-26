package com.example.springbootdemo.dao.inter;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.springbootdemo.model.domain.CourseDO;

public interface CourseDao extends IService<CourseDO> {

    boolean existsCourseCodeIncludingDeleted(String courseCode, Long excludeId);

    boolean hasStudentRelations(long courseId);
}

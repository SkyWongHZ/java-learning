package com.example.springbootdemo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springbootdemo.model.domain.CourseDO;
import com.example.springbootdemo.model.domain.StudentCourseDO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface StudentCourseMapper extends BaseMapper<StudentCourseDO> {

    @Select({
            "SELECT c.id, c.course_code, c.course_name, c.gmt_create, c.gmt_modify, c.deleted",
            "FROM student_course sc",
            "INNER JOIN course c ON c.id = sc.course_id AND c.deleted = 0",
            "WHERE sc.student_id = #{studentId}",
            "ORDER BY c.course_code ASC, c.id ASC"
    })
    List<CourseDO> selectCoursesByStudentId(@Param("studentId") long studentId);
}

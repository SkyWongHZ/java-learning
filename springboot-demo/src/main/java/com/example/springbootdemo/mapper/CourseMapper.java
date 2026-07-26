package com.example.springbootdemo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springbootdemo.model.domain.CourseDO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface CourseMapper extends BaseMapper<CourseDO> {

    @Select({
            "<script>",
            "SELECT COUNT(1) FROM course",
            "WHERE course_code = #{courseCode}",
            "<if test='excludeId != null'>AND id != #{excludeId}</if>",
            "</script>"
    })
    long countByCourseCodeIncludingDeleted(
            @Param("courseCode") String courseCode,
            @Param("excludeId") Long excludeId);

    @Select("SELECT COUNT(1) FROM student_course WHERE course_id = #{courseId}")
    long countStudentRelations(@Param("courseId") long courseId);
}

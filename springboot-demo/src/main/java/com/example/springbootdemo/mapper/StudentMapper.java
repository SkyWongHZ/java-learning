package com.example.springbootdemo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springbootdemo.model.domain.StudentDO;
import com.example.springbootdemo.model.dto.StudentPageRowDTO;
import com.example.springbootdemo.model.query.StudentPageQuery;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface StudentMapper extends BaseMapper<StudentDO> {

    @Select({
            "<script>",
            "SELECT s.id, s.student_no, s.name, s.gender, s.phone,",
            "s.class_id, sc.class_code, sc.class_name, s.gmt_create, s.gmt_modify",
            "FROM student s",
            "INNER JOIN school_class sc ON sc.id = s.class_id AND sc.deleted = 0",
            "WHERE s.deleted = 0",
            "<if test='query.keyword != null and query.keyword != \"\"'>",
            "AND (s.student_no LIKE CONCAT('%', #{query.keyword}, '%')",
            "OR s.name LIKE CONCAT('%', #{query.keyword}, '%'))",
            "</if>",
            "<if test='query.classId != null'>AND s.class_id = #{query.classId}</if>",
            "<if test='query.courseId != null'>",
            "AND EXISTS (",
            "SELECT 1 FROM student_course scr",
            "WHERE scr.student_id = s.id AND scr.course_id = #{query.courseId}",
            ")",
            "</if>",
            "ORDER BY s.gmt_create DESC, s.id DESC",
            "</script>"
    })
    List<StudentPageRowDTO> selectPageRows(@Param("query") StudentPageQuery query);

    @Select({
            "<script>",
            "SELECT COUNT(1) FROM student",
            "WHERE student_no = #{studentNo}",
            "<if test='excludeId != null'>AND id != #{excludeId}</if>",
            "</script>"
    })
    long countByStudentNoIncludingDeleted(
            @Param("studentNo") String studentNo,
            @Param("excludeId") Long excludeId);
}

package com.example.springbootdemo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springbootdemo.model.domain.SchoolClassDO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface SchoolClassMapper extends BaseMapper<SchoolClassDO> {

    @Select({
            "<script>",
            "SELECT COUNT(1) FROM school_class",
            "WHERE class_code = #{classCode}",
            "<if test='excludeId != null'>AND id != #{excludeId}</if>",
            "</script>"
    })
    long countByClassCodeIncludingDeleted(
            @Param("classCode") String classCode,
            @Param("excludeId") Long excludeId);

    @Select("SELECT COUNT(1) FROM student WHERE class_id = #{classId} AND deleted = 0")
    long countActiveStudents(@Param("classId") long classId);
}

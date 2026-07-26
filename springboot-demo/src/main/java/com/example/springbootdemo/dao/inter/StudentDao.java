package com.example.springbootdemo.dao.inter;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.springbootdemo.model.domain.StudentDO;
import com.example.springbootdemo.model.dto.StudentPageRowDTO;
import com.example.springbootdemo.model.query.StudentPageQuery;

import java.util.List;

public interface StudentDao extends IService<StudentDO> {

    List<StudentPageRowDTO> selectPageRows(StudentPageQuery query);

    boolean existsStudentNoIncludingDeleted(String studentNo, Long excludeId);
}

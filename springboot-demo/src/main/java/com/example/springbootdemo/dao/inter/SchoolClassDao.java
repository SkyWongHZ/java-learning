package com.example.springbootdemo.dao.inter;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.springbootdemo.model.domain.SchoolClassDO;

public interface SchoolClassDao extends IService<SchoolClassDO> {

    boolean existsClassCodeIncludingDeleted(String classCode, Long excludeId);

    boolean hasActiveStudents(long classId);
}

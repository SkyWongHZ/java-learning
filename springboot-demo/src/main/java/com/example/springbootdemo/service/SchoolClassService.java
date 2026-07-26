package com.example.springbootdemo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.springbootdemo.dao.inter.SchoolClassDao;
import com.example.springbootdemo.enums.BaseStatusCodeEnum;
import com.example.springbootdemo.exception.BaseException;
import com.example.springbootdemo.model.domain.SchoolClassDO;
import com.example.springbootdemo.model.form.CreateClassForm;
import com.example.springbootdemo.model.form.UpdateClassForm;
import com.example.springbootdemo.model.query.ClassQuery;
import com.example.springbootdemo.model.vo.ClassVO;
import com.example.springbootdemo.util.BusinessInputUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SchoolClassService {

    private final SchoolClassDao schoolClassDao;

    public SchoolClassService(SchoolClassDao schoolClassDao) {
        this.schoolClassDao = schoolClassDao;
    }

    public List<ClassVO> list(ClassQuery query) {
        String keyword = BusinessInputUtils.normalizeOptional(query.getKeyword());
        return schoolClassDao.list(new LambdaQueryWrapper<SchoolClassDO>()
                        .and(keyword != null, wrapper -> wrapper
                                .like(SchoolClassDO::getClassCode, keyword)
                                .or()
                                .like(SchoolClassDO::getClassName, keyword))
                        .orderByDesc(SchoolClassDO::getGmtCreate)
                        .orderByDesc(SchoolClassDO::getId))
                .stream()
                .map(ClassVO::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public ClassVO create(CreateClassForm form) {
        String classCode = BusinessInputUtils.normalizeCode(
                form.getClassCode(), "班级编码", 2, 32);
        String className = BusinessInputUtils.normalizeRequired(
                form.getClassName(), "班级名称", 1, 100);
        assertClassCodeAvailable(classCode, null);

        LocalDateTime now = LocalDateTime.now();
        SchoolClassDO schoolClass = new SchoolClassDO();
        schoolClass.setClassCode(classCode);
        schoolClass.setClassName(className);
        schoolClass.setGmtCreate(now);
        schoolClass.setGmtModify(now);
        schoolClass.setDeleted(0);
        schoolClassDao.save(schoolClass);
        return ClassVO.from(schoolClass);
    }

    @Transactional
    public ClassVO update(long id, UpdateClassForm form) {
        SchoolClassDO schoolClass = requireSchoolClass(id);
        String classCode = BusinessInputUtils.normalizeCode(
                form.getClassCode(), "班级编码", 2, 32);
        String className = BusinessInputUtils.normalizeRequired(
                form.getClassName(), "班级名称", 1, 100);
        assertClassCodeAvailable(classCode, id);

        schoolClass.setClassCode(classCode);
        schoolClass.setClassName(className);
        schoolClass.setGmtModify(LocalDateTime.now());
        schoolClassDao.updateById(schoolClass);
        return ClassVO.from(schoolClass);
    }

    @Transactional
    public Boolean delete(long id) {
        requireSchoolClass(id);
        if (schoolClassDao.hasActiveStudents(id)) {
            throw new BaseException(
                    BaseStatusCodeEnum.BUSINESS_ERROR,
                    "班级下仍有学生，不能删除");
        }
        schoolClassDao.removeById(id);
        return Boolean.TRUE;
    }

    private SchoolClassDO requireSchoolClass(long id) {
        SchoolClassDO schoolClass = schoolClassDao.getById(id);
        if (schoolClass == null) {
            throw new BaseException(BaseStatusCodeEnum.BUSINESS_ERROR, "班级不存在");
        }
        return schoolClass;
    }

    private void assertClassCodeAvailable(String classCode, Long excludeId) {
        if (schoolClassDao.existsClassCodeIncludingDeleted(classCode, excludeId)) {
            throw new BaseException(BaseStatusCodeEnum.BUSINESS_ERROR, "班级编码已存在");
        }
    }
}

package com.example.springbootdemo.service;

import com.example.springbootdemo.dao.inter.CourseDao;
import com.example.springbootdemo.dao.inter.SchoolClassDao;
import com.example.springbootdemo.dao.inter.StudentCourseDao;
import com.example.springbootdemo.dao.inter.StudentDao;
import com.example.springbootdemo.enums.BaseStatusCodeEnum;
import com.example.springbootdemo.exception.BaseException;
import com.example.springbootdemo.model.common.PageWrapper;
import com.example.springbootdemo.model.domain.CourseDO;
import com.example.springbootdemo.model.domain.SchoolClassDO;
import com.example.springbootdemo.model.domain.StudentCourseDO;
import com.example.springbootdemo.model.domain.StudentDO;
import com.example.springbootdemo.model.dto.StudentPageRowDTO;
import com.example.springbootdemo.model.form.CreateStudentForm;
import com.example.springbootdemo.model.form.UpdateStudentForm;
import com.example.springbootdemo.model.query.StudentPageQuery;
import com.example.springbootdemo.model.vo.ClassSimpleVO;
import com.example.springbootdemo.model.vo.CourseSimpleVO;
import com.example.springbootdemo.model.vo.StudentDetailVO;
import com.example.springbootdemo.model.vo.StudentPageItemVO;
import com.example.springbootdemo.util.BusinessInputUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class StudentService {

    private final StudentDao studentDao;
    private final SchoolClassDao schoolClassDao;
    private final CourseDao courseDao;
    private final StudentCourseDao studentCourseDao;

    public StudentService(
            StudentDao studentDao,
            SchoolClassDao schoolClassDao,
            CourseDao courseDao,
            StudentCourseDao studentCourseDao) {
        this.studentDao = studentDao;
        this.schoolClassDao = schoolClassDao;
        this.courseDao = courseDao;
        this.studentCourseDao = studentCourseDao;
    }

    public PageWrapper<StudentPageItemVO> pageList(StudentPageQuery query) {
        query.setKeyword(BusinessInputUtils.normalizeOptional(query.getKeyword()));
        PageHelper.startPage(query.getPageNum(), query.getPageSize());
        List<StudentPageRowDTO> rows = studentDao.selectPageRows(query);
        PageInfo<StudentPageRowDTO> pageInfo = new PageInfo<>(rows);
        List<StudentPageItemVO> list = rows.stream()
                .map(StudentPageItemVO::from)
                .collect(Collectors.toList());
        return PageWrapper.convert(pageInfo, list);
    }

    public StudentDetailVO get(long id) {
        return toDetail(requireStudent(id));
    }

    @Transactional
    public StudentDetailVO create(CreateStudentForm form) {
        String studentNo = BusinessInputUtils.normalizeCode(
                form.getStudentNo(), "学号", 2, 32);
        String name = BusinessInputUtils.normalizeRequired(
                form.getName(), "学生姓名", 1, 50);
        String phone = BusinessInputUtils.normalizePhone(form.getPhone());
        assertStudentNoAvailable(studentNo);
        requireSchoolClass(form.getClassId());
        List<Long> courseIds = validateCourses(form.getCourseIds());

        LocalDateTime now = LocalDateTime.now();
        StudentDO student = new StudentDO();
        student.setStudentNo(studentNo);
        student.setName(name);
        student.setGender(form.getGender());
        student.setPhone(phone);
        student.setClassId(form.getClassId());
        student.setGmtCreate(now);
        student.setGmtModify(now);
        student.setDeleted(0);
        studentDao.save(student);
        replaceCourseRelations(student.getId(), courseIds, now);
        return toDetail(student);
    }

    @Transactional
    public StudentDetailVO update(long id, UpdateStudentForm form) {
        StudentDO student = requireStudent(id);
        String name = BusinessInputUtils.normalizeRequired(
                form.getName(), "学生姓名", 1, 50);
        String phone = BusinessInputUtils.normalizePhone(form.getPhone());
        requireSchoolClass(form.getClassId());
        List<Long> courseIds = validateCourses(form.getCourseIds());

        LocalDateTime now = LocalDateTime.now();
        student.setName(name);
        student.setGender(form.getGender());
        student.setPhone(phone);
        student.setClassId(form.getClassId());
        student.setGmtModify(now);
        studentDao.updateById(student);
        replaceCourseRelations(student.getId(), courseIds, now);
        return toDetail(student);
    }

    @Transactional
    public Boolean delete(long id) {
        requireStudent(id);
        studentCourseDao.removeByStudentId(id);
        studentDao.removeById(id);
        return Boolean.TRUE;
    }

    private StudentDetailVO toDetail(StudentDO student) {
        SchoolClassDO schoolClass = requireSchoolClass(student.getClassId());
        List<CourseSimpleVO> courses = studentCourseDao.selectCoursesByStudentId(student.getId())
                .stream()
                .map(CourseSimpleVO::from)
                .collect(Collectors.toList());
        return StudentDetailVO.from(student, ClassSimpleVO.from(schoolClass), courses);
    }

    private StudentDO requireStudent(long id) {
        StudentDO student = studentDao.getById(id);
        if (student == null) {
            throw new BaseException(BaseStatusCodeEnum.BUSINESS_ERROR, "学生不存在");
        }
        return student;
    }

    private SchoolClassDO requireSchoolClass(long id) {
        SchoolClassDO schoolClass = schoolClassDao.getById(id);
        if (schoolClass == null) {
            throw new BaseException(BaseStatusCodeEnum.BUSINESS_ERROR, "班级不存在");
        }
        return schoolClass;
    }

    private void assertStudentNoAvailable(String studentNo) {
        if (studentDao.existsStudentNoIncludingDeleted(studentNo, null)) {
            throw new BaseException(BaseStatusCodeEnum.BUSINESS_ERROR, "学号已存在");
        }
    }

    private List<Long> validateCourses(List<Long> requestedCourseIds) {
        if (requestedCourseIds == null || requestedCourseIds.isEmpty()) {
            return Collections.emptyList();
        }
        for (Long courseId : requestedCourseIds) {
            if (courseId == null || courseId <= 0) {
                throw new BaseException(
                        BaseStatusCodeEnum.VALIDATION_ERROR,
                        "课程 ID 必须大于 0");
            }
        }
        Set<Long> uniqueIds = new HashSet<>(requestedCourseIds);
        if (uniqueIds.size() != requestedCourseIds.size()) {
            throw new BaseException(
                    BaseStatusCodeEnum.VALIDATION_ERROR,
                    "课程 ID 不能重复");
        }
        List<CourseDO> courses = courseDao.listByIds(uniqueIds);
        if (courses.size() != uniqueIds.size()) {
            throw new BaseException(BaseStatusCodeEnum.BUSINESS_ERROR, "课程不存在");
        }
        return new ArrayList<>(requestedCourseIds);
    }

    private void replaceCourseRelations(
            long studentId,
            List<Long> courseIds,
            LocalDateTime now) {
        studentCourseDao.removeByStudentId(studentId);
        if (courseIds.isEmpty()) {
            return;
        }
        List<StudentCourseDO> relations = courseIds.stream()
                .map(courseId -> {
                    StudentCourseDO relation = new StudentCourseDO();
                    relation.setStudentId(studentId);
                    relation.setCourseId(courseId);
                    relation.setGmtCreate(now);
                    return relation;
                })
                .collect(Collectors.toList());
        studentCourseDao.saveBatch(relations);
    }
}

package com.example.springbootdemo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.springbootdemo.dao.inter.CourseDao;
import com.example.springbootdemo.enums.BaseStatusCodeEnum;
import com.example.springbootdemo.exception.BaseException;
import com.example.springbootdemo.model.domain.CourseDO;
import com.example.springbootdemo.model.form.CreateCourseForm;
import com.example.springbootdemo.model.form.UpdateCourseForm;
import com.example.springbootdemo.model.query.CourseQuery;
import com.example.springbootdemo.model.vo.CourseVO;
import com.example.springbootdemo.util.BusinessInputUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseService {

    private final CourseDao courseDao;

    public CourseService(CourseDao courseDao) {
        this.courseDao = courseDao;
    }

    public List<CourseVO> list(CourseQuery query) {
        String keyword = BusinessInputUtils.normalizeOptional(query.getKeyword());
        return courseDao.list(new LambdaQueryWrapper<CourseDO>()
                        .and(keyword != null, wrapper -> wrapper
                                .like(CourseDO::getCourseCode, keyword)
                                .or()
                                .like(CourseDO::getCourseName, keyword))
                        .orderByDesc(CourseDO::getGmtCreate)
                        .orderByDesc(CourseDO::getId))
                .stream()
                .map(CourseVO::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public CourseVO create(CreateCourseForm form) {
        String courseCode = BusinessInputUtils.normalizeCode(
                form.getCourseCode(), "课程编码", 2, 32);
        String courseName = BusinessInputUtils.normalizeRequired(
                form.getCourseName(), "课程名称", 1, 100);
        assertCourseCodeAvailable(courseCode, null);

        LocalDateTime now = LocalDateTime.now();
        CourseDO course = new CourseDO();
        course.setCourseCode(courseCode);
        course.setCourseName(courseName);
        course.setGmtCreate(now);
        course.setGmtModify(now);
        course.setDeleted(0);
        courseDao.save(course);
        return CourseVO.from(course);
    }

    @Transactional
    public CourseVO update(long id, UpdateCourseForm form) {
        CourseDO course = requireCourse(id);
        String courseCode = BusinessInputUtils.normalizeCode(
                form.getCourseCode(), "课程编码", 2, 32);
        String courseName = BusinessInputUtils.normalizeRequired(
                form.getCourseName(), "课程名称", 1, 100);
        assertCourseCodeAvailable(courseCode, id);

        course.setCourseCode(courseCode);
        course.setCourseName(courseName);
        course.setGmtModify(LocalDateTime.now());
        courseDao.updateById(course);
        return CourseVO.from(course);
    }

    @Transactional
    public Boolean delete(long id) {
        requireCourse(id);
        if (courseDao.hasStudentRelations(id)) {
            throw new BaseException(
                    BaseStatusCodeEnum.BUSINESS_ERROR,
                    "课程仍被学生选择，不能删除");
        }
        courseDao.removeById(id);
        return Boolean.TRUE;
    }

    private CourseDO requireCourse(long id) {
        CourseDO course = courseDao.getById(id);
        if (course == null) {
            throw new BaseException(BaseStatusCodeEnum.BUSINESS_ERROR, "课程不存在");
        }
        return course;
    }

    private void assertCourseCodeAvailable(String courseCode, Long excludeId) {
        if (courseDao.existsCourseCodeIncludingDeleted(courseCode, excludeId)) {
            throw new BaseException(BaseStatusCodeEnum.BUSINESS_ERROR, "课程编码已存在");
        }
    }
}

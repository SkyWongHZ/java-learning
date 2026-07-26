package com.example.springbootdemo;

import com.example.springbootdemo.dao.inter.AdminUserDao;
import com.example.springbootdemo.dao.inter.CourseDao;
import com.example.springbootdemo.dao.inter.SchoolClassDao;
import com.example.springbootdemo.model.domain.AdminUserDO;
import com.example.springbootdemo.model.domain.CourseDO;
import com.example.springbootdemo.model.domain.SchoolClassDO;
import com.example.springbootdemo.service.RedisService;
import com.example.springbootdemo.web.auth.AuthInterceptor;
import com.example.springbootdemo.web.auth.ClientSystemResolver;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class StudentManagementControllerTest {

    private static final String TEST_PASSWORD = "TestAuth_123!";
    private static final int TEST_SYSTEM = 1;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AdminUserDao adminUserDao;

    @Autowired
    private SchoolClassDao schoolClassDao;

    @Autowired
    private CourseDao courseDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RedisService redisService;

    private String authToken;

    @BeforeEach
    void authenticate() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        AdminUserDO admin = new AdminUserDO();
        admin.setUsername(unique("student-admin"));
        admin.setDisplayName("Student Test Admin");
        admin.setPasswordHash(passwordEncoder.encode(TEST_PASSWORD));
        admin.setStatus(1);
        admin.setFailedLoginCount(0);
        admin.setGmtCreate(now);
        admin.setGmtModify(now);
        admin.setDeleted(0);
        adminUserDao.save(admin);

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .header(ClientSystemResolver.SYSTEM_HEADER, TEST_SYSTEM)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(mapOf(
                                "username", admin.getUsername(),
                                "password", TEST_PASSWORD))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andReturn();
        authToken = body(result).path("data").path("token").asText();
    }

    @AfterEach
    void cleanRedisToken() {
        if (authToken != null) {
            redisService.delete(authToken);
        }
    }

    @Test
    void completesClassCourseAndStudentCrudFlow() throws Exception {
        String suffix = Long.toUnsignedString(System.nanoTime(), 36).toUpperCase();
        long classId = createClass(" cs-" + suffix + " ", " 2026级计算机1班 ");
        long courseId = createCourse(" java-" + suffix + " ", " Java程序设计 ");

        mockMvc.perform(authorized(get("/api/v1/classes")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data[?(@.id == " + classId + ")]").exists());

        mockMvc.perform(authorized(get("/api/v1/courses").param("keyword", suffix)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(courseId));

        mockMvc.perform(authorized(put("/api/v1/classes/" + classId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(mapOf(
                                "classCode", "CS-" + suffix,
                                "className", "2026级软件工程1班")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.className").value("2026级软件工程1班"));

        mockMvc.perform(authorized(put("/api/v1/courses/" + courseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(mapOf(
                                "courseCode", "JAVA-" + suffix,
                                "courseName", "Java程序设计基础")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.courseName").value("Java程序设计基础"));

        String studentNo = "S" + suffix;
        MvcResult createStudent = mockMvc.perform(authorized(post("/api/v1/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(mapOf(
                                "studentNo", " " + studentNo.toLowerCase() + " ",
                                "name", " 张三 ",
                                "gender", 1,
                                "phone", "13800138000",
                                "classId", classId,
                                "courseIds", Collections.singletonList(courseId))))))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Trace-Id"))
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.studentNo").value(studentNo))
                .andExpect(jsonPath("$.data.classInfo.id").value(classId))
                .andExpect(jsonPath("$.data.courses[0].id").value(courseId))
                .andReturn();
        long studentId = body(createStudent).path("data").path("id").asLong();

        mockMvc.perform(authorized(get("/api/v1/students")
                        .param("pageNum", "1")
                        .param("pageSize", "10")
                        .param("classId", String.valueOf(classId))
                        .param("courseId", String.valueOf(courseId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.pageNum").value(1))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].id").value(studentId))
                .andExpect(jsonPath("$.data.boolLastPage").value(true));

        mockMvc.perform(authorized(get("/api/v1/students/" + studentId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.studentNo").value(studentNo))
                .andExpect(jsonPath("$.data.courses[0].id").value(courseId));

        Map<String, Object> updateStudentBody = mapOf(
                "studentNo", "THIS-VALUE-MUST-BE-IGNORED",
                "name", "李四",
                "gender", 2,
                "phone", null,
                "classId", classId,
                "courseIds", Collections.emptyList());
        mockMvc.perform(authorized(put("/api/v1/students/" + studentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(updateStudentBody))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("李四"))
                .andExpect(jsonPath("$.data.studentNo").value(studentNo))
                .andExpect(jsonPath("$.data.phone").value(nullValue()))
                .andExpect(jsonPath("$.data.courses").isEmpty());
        assertNull(jdbcTemplate.queryForObject(
                "SELECT phone FROM student WHERE id = ?",
                String.class,
                studentId));
        assertEquals(studentNo, jdbcTemplate.queryForObject(
                "SELECT student_no FROM student WHERE id = ?",
                String.class,
                studentId));

        mockMvc.perform(authorized(get("/api/v1/students/" + studentId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.courses").isEmpty());

        mockMvc.perform(authorized(delete("/api/v1/students/" + studentId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));

        mockMvc.perform(authorized(get("/api/v1/students/" + studentId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(2))
                .andExpect(jsonPath("$.msg").value("学生不存在"));

        assertEquals(1, jdbcTemplate.queryForObject(
                "SELECT deleted FROM student WHERE id = ?",
                Integer.class,
                studentId));
        assertEquals(0L, jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM student_course WHERE student_id = ?",
                Long.class,
                studentId));

        mockMvc.perform(authorized(delete("/api/v1/courses/" + courseId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));
        mockMvc.perform(authorized(delete("/api/v1/classes/" + classId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));
    }

    @Test
    void rejectsInvalidRelationsDuplicateCodesAndMissingEntities() throws Exception {
        String suffix = Long.toUnsignedString(System.nanoTime(), 36).toUpperCase();
        String classCode = "CLASS-" + suffix;
        String courseCode = "COURSE-" + suffix;
        String studentNo = "STUDENT-" + suffix;
        long classId = createClass(classCode, "重复校验班级");
        long courseId = createCourse(courseCode, "重复校验课程");

        mockMvc.perform(authorized(post("/api/v1/classes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(mapOf(
                                "classCode", classCode.toLowerCase(),
                                "className", "另一个同编码班级")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(2))
                .andExpect(jsonPath("$.msg").value("班级编码已存在"));

        mockMvc.perform(authorized(post("/api/v1/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(mapOf(
                                "courseCode", courseCode.toLowerCase(),
                                "courseName", "另一个同编码课程")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(2))
                .andExpect(jsonPath("$.msg").value("课程编码已存在"));

        createStudentDirectly(studentNo, classId, courseId);
        mockMvc.perform(authorized(post("/api/v1/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(mapOf(
                                "studentNo", studentNo.toLowerCase(),
                                "name", "重复学号学生",
                                "gender", 0,
                                "phone", null,
                                "classId", classId,
                                "courseIds", Collections.emptyList())))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(2))
                .andExpect(jsonPath("$.msg").value("学号已存在"));

        mockMvc.perform(authorized(post("/api/v1/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(mapOf(
                                "studentNo", "INVALID-CLASS-" + suffix,
                                "name", "无效班级学生",
                                "gender", 0,
                                "phone", null,
                                "classId", Long.MAX_VALUE,
                                "courseIds", Collections.emptyList())))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(2))
                .andExpect(jsonPath("$.msg").value("班级不存在"));

        mockMvc.perform(authorized(post("/api/v1/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(mapOf(
                                "studentNo", "INVALID-COURSE-" + suffix,
                                "name", "无效课程学生",
                                "gender", 0,
                                "phone", null,
                                "classId", classId,
                                "courseIds", Collections.singletonList(Long.MAX_VALUE))))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(2))
                .andExpect(jsonPath("$.msg").value("课程不存在"));

        mockMvc.perform(authorized(get("/api/v1/students/" + Long.MAX_VALUE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(2))
                .andExpect(jsonPath("$.msg").value("学生不存在"));
        mockMvc.perform(authorized(delete("/api/v1/courses/" + Long.MAX_VALUE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(2))
                .andExpect(jsonPath("$.msg").value("课程不存在"));
        mockMvc.perform(authorized(delete("/api/v1/classes/" + Long.MAX_VALUE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(2))
                .andExpect(jsonPath("$.msg").value("班级不存在"));
    }

    @Test
    void returnsEmptyPageAndKeepsTraceIdConsistent() throws Exception {
        String requestedTraceId = "student-acceptance-" + unique("trace");
        MvcResult result = mockMvc.perform(authorized(get("/api/v1/students")
                        .header("traceId", requestedTraceId)
                        .param("pageNum", "1")
                        .param("pageSize", "10")
                        .param("keyword", unique("no-such-student"))))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Trace-Id", requestedTraceId))
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.pageNum").value(1))
                .andExpect(jsonPath("$.data.pageSize").value(10))
                .andExpect(jsonPath("$.data.total").value(0))
                .andExpect(jsonPath("$.data.pages").value(0))
                .andExpect(jsonPath("$.data.list").isEmpty())
                .andExpect(jsonPath("$.data.boolLastPage").value(true))
                .andReturn();

        JsonNode response = body(result);
        assertEquals(requestedTraceId, response.path("tid").asText());
        assertTrue(response.has("code"));
        assertTrue(response.has("msg"));
        assertTrue(response.has("errorDetail"));
        assertTrue(response.has("data"));
        assertTrue(response.has("tid"));
    }

    @Test
    void validatesPaginationDuplicatesAndRelationshipDeleteConstraints() throws Exception {
        String suffix = Long.toUnsignedString(System.nanoTime(), 36).toUpperCase();
        long classId = insertClass("CLASS-" + suffix, "测试班级");
        long courseId = insertCourse("COURSE-" + suffix, "测试课程");

        mockMvc.perform(authorized(get("/api/v1/students")
                        .param("pageSize", "10")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(4))
                .andExpect(jsonPath("$.msg").value("页码不能为空"));

        mockMvc.perform(authorized(post("/api/v1/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(mapOf(
                                "studentNo", "STUDENT-" + suffix,
                                "name", "测试学生",
                                "gender", 0,
                                "phone", "",
                                "classId", classId,
                                "courseIds", Arrays.asList(courseId, courseId))))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(4))
                .andExpect(jsonPath("$.msg").value("课程 ID 不能重复"));

        long studentId = createStudentDirectly(
                "STUDENT-" + suffix,
                classId,
                courseId);

        mockMvc.perform(authorized(delete("/api/v1/courses/" + courseId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(2))
                .andExpect(jsonPath("$.msg").value("课程仍被学生选择，不能删除"));

        mockMvc.perform(authorized(delete("/api/v1/classes/" + classId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(2))
                .andExpect(jsonPath("$.msg").value("班级下仍有学生，不能删除"));

        mockMvc.perform(authorized(delete("/api/v1/students/" + studentId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));
    }

    private long createClass(String classCode, String className) throws Exception {
        MvcResult result = mockMvc.perform(authorized(post("/api/v1/classes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(mapOf(
                                "classCode", classCode,
                                "className", className)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andReturn();
        return body(result).path("data").path("id").asLong();
    }

    private long createCourse(String courseCode, String courseName) throws Exception {
        MvcResult result = mockMvc.perform(authorized(post("/api/v1/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(mapOf(
                                "courseCode", courseCode,
                                "courseName", courseName)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andReturn();
        return body(result).path("data").path("id").asLong();
    }

    private long insertClass(String classCode, String className) {
        LocalDateTime now = LocalDateTime.now();
        SchoolClassDO schoolClass = new SchoolClassDO();
        schoolClass.setClassCode(classCode);
        schoolClass.setClassName(className);
        schoolClass.setGmtCreate(now);
        schoolClass.setGmtModify(now);
        schoolClass.setDeleted(0);
        schoolClassDao.save(schoolClass);
        return schoolClass.getId();
    }

    private long insertCourse(String courseCode, String courseName) {
        LocalDateTime now = LocalDateTime.now();
        CourseDO course = new CourseDO();
        course.setCourseCode(courseCode);
        course.setCourseName(courseName);
        course.setGmtCreate(now);
        course.setGmtModify(now);
        course.setDeleted(0);
        courseDao.save(course);
        return course.getId();
    }

    private long createStudentDirectly(
            String studentNo,
            long classId,
            long courseId) throws Exception {
        MvcResult result = mockMvc.perform(authorized(post("/api/v1/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(mapOf(
                                "studentNo", studentNo,
                                "name", "测试学生",
                                "gender", 1,
                                "phone", null,
                                "classId", classId,
                                "courseIds", Collections.singletonList(courseId))))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andReturn();
        return body(result).path("data").path("id").asLong();
    }

    private MockHttpServletRequestBuilder authorized(MockHttpServletRequestBuilder request) {
        return request
                .header(AuthInterceptor.TOKEN_HEADER, authToken)
                .header(ClientSystemResolver.SYSTEM_HEADER, TEST_SYSTEM);
    }

    private JsonNode body(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsByteArray());
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    private static Map<String, Object> mapOf(Object... entries) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int index = 0; index < entries.length; index += 2) {
            map.put((String) entries[index], entries[index + 1]);
        }
        return map;
    }

    private static String unique(String prefix) {
        return prefix + "-" + Long.toUnsignedString(System.nanoTime(), 36);
    }
}

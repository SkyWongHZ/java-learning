package com.example.springbootdemo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserControllerTest {

    private static final String USERS_API = "/api/v1/users";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void createUserPersistsDataAndReturnsUnifiedResponse() throws Exception {
        String username = uniqueUsername("sky");
        MvcResult result = mockMvc.perform(post(USERS_API)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson("  " + username + "  ", "  Sky Wang  ")))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Trace-Id"))
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.msg").value("成功"))
                .andExpect(jsonPath("$.errorDetail").value(nullValue()))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.username").value(username))
                .andExpect(jsonPath("$.data.displayName").value("Sky Wang"))
                .andExpect(jsonPath("$.data.gmtCreate")
                        .value(matchesPattern("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}")))
                .andExpect(jsonPath("$.data.gmtModify")
                        .value(matchesPattern("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}")))
                .andExpect(jsonPath("$.tid").isNotEmpty())
                .andReturn();

        JsonNode response = readBody(result);
        assertEquals(result.getResponse().getHeader("X-Trace-Id"), response.path("tid").asText());
        Long rowCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM demo_user WHERE id = ? AND username = ? AND deleted = 0",
                Long.class,
                response.path("data").path("id").asLong(),
                username);
        assertEquals(1L, rowCount);
    }

    @Test
    void getUserUsesIncomingTraceId() throws Exception {
        String username = uniqueUsername("trace-user");
        long id = createUser(username, "Trace User");
        String traceId = "frontend-trace-001";

        mockMvc.perform(get(USERS_API + "/" + id).header("traceId", traceId))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Trace-Id", traceId))
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.id").value(id))
                .andExpect(jsonPath("$.data.username").value(username))
                .andExpect(jsonPath("$.tid").value(traceId));
    }

    @Test
    void updateUserPersistsAndReturnsUpdatedUser() throws Exception {
        String beforeUsername = uniqueUsername("before-update");
        String afterUsername = uniqueUsername("after-update");
        long id = createUser(beforeUsername, "Before Update");

        mockMvc.perform(put(USERS_API + "/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson("  " + afterUsername + "  ", "  After Update  ")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.id").value(id))
                .andExpect(jsonPath("$.data.username").value(afterUsername))
                .andExpect(jsonPath("$.data.displayName").value("After Update"));

        mockMvc.perform(get(USERS_API + "/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value(afterUsername));
    }

    @Test
    void deleteUserIsLogicalAndRepeatedDeleteReturnsCodeNine() throws Exception {
        long id = createUser(uniqueUsername("delete-user"), "Delete User");

        mockMvc.perform(delete(USERS_API + "/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data").value(true));

        Integer deleted = jdbcTemplate.queryForObject(
                "SELECT deleted FROM demo_user WHERE id = ?", Integer.class, id);
        assertEquals(1, deleted);

        mockMvc.perform(get(USERS_API + "/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(9))
                .andExpect(jsonPath("$.msg").value("用户不存在"))
                .andExpect(jsonPath("$.data").value(nullValue()));

        mockMvc.perform(delete(USERS_API + "/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(9));
    }

    @Test
    void usernameIsCaseInsensitiveAndRemainsReservedAfterLogicalDelete() throws Exception {
        String username = uniqueUsername("SkyUser");
        long id = createUser(username, "Sky User");

        mockMvc.perform(post(USERS_API)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson("  " + username.toLowerCase() + "  ", "Another User")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(2))
                .andExpect(jsonPath("$.msg").value("用户名已存在"));

        mockMvc.perform(delete(USERS_API + "/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        mockMvc.perform(post(USERS_API)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson(username.toUpperCase(), "Reused User")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(2))
                .andExpect(jsonPath("$.msg").value("用户名已存在"));
    }

    @Test
    void validationAndInvalidIdReturnCodeFourWithHttpTwoHundred() throws Exception {
        mockMvc.perform(post(USERS_API)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson("   ", "Valid Display Name")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(4))
                .andExpect(jsonPath("$.msg").value("用户名不能为空"));

        mockMvc.perform(post(USERS_API)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson("x".repeat(51), "Valid Display Name")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(4))
                .andExpect(jsonPath("$.msg").value("用户名长度必须为 2 到 50 个字符"));

        mockMvc.perform(get(USERS_API + "/0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(4))
                .andExpect(jsonPath("$.msg").value("用户 ID 必须大于 0"));
    }

    @Test
    void invalidJsonAndUnsupportedMethodUseConfiguredErrorCodes() throws Exception {
        mockMvc.perform(post(USERS_API)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(5))
                .andExpect(jsonPath("$.errorDetail").value(nullValue()));

        mockMvc.perform(patch(USERS_API + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson("patch-user", "Patch User")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(6));
    }

    @Test
    void vueOriginCanCallApiAndReadTraceHeader() throws Exception {
        long id = createUser(uniqueUsername("cors-user"), "Cors User");

        mockMvc.perform(get(USERS_API + "/" + id)
                        .header("Origin", "http://localhost:5173"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
                .andExpect(header().string("Access-Control-Expose-Headers", "X-Trace-Id"));
    }

    @Test
    void knife4jDocumentAndUnknownRouteHaveExpectedHttpStatus() throws Exception {
        mockMvc.perform(get("/doc.html"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/not-existing"))
                .andExpect(status().isNotFound());
    }

    private long createUser(String username, String displayName) throws Exception {
        MvcResult result = mockMvc.perform(post(USERS_API)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson(username, displayName)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andReturn();
        long id = readBody(result).path("data").path("id").asLong();
        assertNotNull(id);
        return id;
    }

    private String userJson(String username, String displayName) throws Exception {
        return objectMapper.writeValueAsString(new UserPayload(username, displayName));
    }

    private String uniqueUsername(String prefix) {
        return prefix + "-" + Long.toUnsignedString(System.nanoTime(), 36);
    }

    private JsonNode readBody(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsByteArray());
    }

    private static class UserPayload {

        private final String username;
        private final String displayName;

        private UserPayload(String username, String displayName) {
            this.username = username;
            this.displayName = displayName;
        }

        public String getUsername() {
            return username;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}

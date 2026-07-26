package com.example.springbootdemo;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.springbootdemo.dao.inter.AdminUserDao;
import com.example.springbootdemo.dao.inter.AuthTokenDao;
import com.example.springbootdemo.model.domain.AdminUserDO;
import com.example.springbootdemo.model.domain.AuthTokenDO;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerTest {

    private static final String AUTH_API = "/api/v1/auth";
    private static final String TEST_PASSWORD = "TestAuth_123!";
    private static final int PC_SYSTEM = 1;
    private static final int APP_SYSTEM = 2;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AdminUserDao adminUserDao;

    @Autowired
    private AuthTokenDao authTokenDao;

    @Autowired
    private RedisService redisService;

    private AdminUserDO admin;
    private final List<String> issuedTokens = new ArrayList<>();

    @BeforeEach
    void createAdmin() {
        LocalDateTime now = LocalDateTime.now();
        admin = new AdminUserDO();
        admin.setUsername("auth-admin-" + Long.toUnsignedString(System.nanoTime(), 36));
        admin.setDisplayName("Auth Test Admin");
        admin.setPasswordHash(passwordEncoder.encode(TEST_PASSWORD));
        admin.setStatus(1);
        admin.setFailedLoginCount(0);
        admin.setGmtCreate(now);
        admin.setGmtModify(now);
        admin.setDeleted(0);
        adminUserDao.save(admin);
    }

    @AfterEach
    void cleanRedisState() {
        for (String token : issuedTokens) {
            redisService.delete(token);
        }
        for (int systemType : new int[]{PC_SYSTEM, APP_SYSTEM}) {
            String failureKey = String.format(
                    "sleep_login_error:%s:%s",
                    systemType,
                    admin.getUsername());
            redisService.deleteRateLimit(failureKey);
            redisService.delete(String.format("sleep_login_error_lock:%s", failureKey));
        }
    }

    @Test
    void loginSupportsCompatibleAndBearerHeadersAndProtectsBusinessApi() throws Exception {
        mockMvc.perform(get("/api/v1/students")
                        .param("pageNum", "1")
                        .param("pageSize", "10")
                        .header(ClientSystemResolver.SYSTEM_HEADER, PC_SYSTEM))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(8))
                .andExpect(jsonPath("$.msg").value("用户未登录"));

        String token = login(TEST_PASSWORD)
                .path("data")
                .path("token")
                .asText();

        mockMvc.perform(get(AUTH_API + "/me")
                        .header(ClientSystemResolver.SYSTEM_HEADER, PC_SYSTEM)
                        .header(AuthInterceptor.TOKEN_HEADER, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.id").value(admin.getId()))
                .andExpect(jsonPath("$.data.username").value(admin.getUsername()))
                .andExpect(jsonPath("$.data.displayName").value(admin.getDisplayName()))
                .andExpect(jsonPath("$.data.systemType").value(PC_SYSTEM));

        mockMvc.perform(get(AUTH_API + "/me")
                        .header(ClientSystemResolver.SYSTEM_HEADER, PC_SYSTEM)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.id").value(admin.getId()));
    }

    @Test
    void fivePasswordFailuresLockAccountWithoutRevealingAccountDetails() throws Exception {
        for (int attempt = 0; attempt < 5; attempt++) {
            loginExpecting("wrong-password", 23, "用户名或密码有误");
        }

        loginExpecting(TEST_PASSWORD, 24, "账号或密码错误次数过多，请 30 分钟后再试");

        String failureKey = String.format(
                "sleep_login_error:%s:%s",
                PC_SYSTEM,
                admin.getUsername());
        assertTrue(redisService.exists(String.format("sleep_login_error_lock:%s", failureKey)));
    }

    @Test
    void secondLoginRevokesFirstTokenAndLogoutRevokesCurrentToken() throws Exception {
        String firstToken = login(TEST_PASSWORD).path("data").path("token").asText();
        String secondToken = login(TEST_PASSWORD).path("data").path("token").asText();

        mockMvc.perform(get(AUTH_API + "/me")
                        .header(ClientSystemResolver.SYSTEM_HEADER, PC_SYSTEM)
                        .header(AuthInterceptor.TOKEN_HEADER, firstToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(8));

        mockMvc.perform(post(AUTH_API + "/logout")
                        .header(ClientSystemResolver.SYSTEM_HEADER, PC_SYSTEM)
                        .header(AuthInterceptor.TOKEN_HEADER, secondToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data").value(true));

        mockMvc.perform(get(AUTH_API + "/me")
                        .header(ClientSystemResolver.SYSTEM_HEADER, PC_SYSTEM)
                        .header(AuthInterceptor.TOKEN_HEADER, secondToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(8));
    }

    @Test
    void differentSystemsKeepIndependentSingleSignOnSessions() throws Exception {
        String pcToken = login(TEST_PASSWORD, PC_SYSTEM).path("data").path("token").asText();
        String appToken = login(TEST_PASSWORD, APP_SYSTEM).path("data").path("token").asText();

        mockMvc.perform(get(AUTH_API + "/me")
                        .header(ClientSystemResolver.SYSTEM_HEADER, PC_SYSTEM)
                        .header(AuthInterceptor.TOKEN_HEADER, pcToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.systemType").value(PC_SYSTEM));

        mockMvc.perform(get(AUTH_API + "/me")
                        .header(ClientSystemResolver.SYSTEM_HEADER, APP_SYSTEM)
                        .header(AuthInterceptor.TOKEN_HEADER, appToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.systemType").value(APP_SYSTEM));

        mockMvc.perform(get(AUTH_API + "/me")
                        .header(ClientSystemResolver.SYSTEM_HEADER, APP_SYSTEM)
                        .header(AuthInterceptor.TOKEN_HEADER, pcToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(8));
    }

    @Test
    void missingRedisTokenReturnsExpiredAndKeepsMysqlWhitelist() throws Exception {
        String token = login(TEST_PASSWORD).path("data").path("token").asText();
        AuthTokenDO tokenDO = authTokenDao.getOne(new LambdaQueryWrapper<AuthTokenDO>()
                .eq(AuthTokenDO::getToken, token));
        redisService.delete(token);

        mockMvc.perform(get(AUTH_API + "/me")
                        .header(ClientSystemResolver.SYSTEM_HEADER, PC_SYSTEM)
                        .header(AuthInterceptor.TOKEN_HEADER, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(11))
                .andExpect(jsonPath("$.msg").value("登录已过期"));

        mockMvc.perform(get(AUTH_API + "/me")
                        .header(ClientSystemResolver.SYSTEM_HEADER, PC_SYSTEM)
                        .header(AuthInterceptor.TOKEN_HEADER, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(11));

        assertNotNull(authTokenDao.getById(tokenDO.getId()));
    }

    @Test
    void loginValidatesBodyAndUsesUnifiedResponse() throws Exception {
        mockMvc.perform(post(AUTH_API + "/login")
                        .header(ClientSystemResolver.SYSTEM_HEADER, PC_SYSTEM)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(" ", TEST_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(4))
                .andExpect(jsonPath("$.msg").value("用户名不能为空"));

        mockMvc.perform(post(AUTH_API + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(admin.getUsername(), TEST_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(4))
                .andExpect(jsonPath("$.msg").value("system 请求头不能为空"));

        loginExpecting(TEST_PASSWORD + "-wrong", 23, "用户名或密码有误");
    }

    private JsonNode login(String password) throws Exception {
        return login(password, PC_SYSTEM);
    }

    private JsonNode login(String password, int systemType) throws Exception {
        MvcResult result = mockMvc.perform(post(AUTH_API + "/login")
                        .header(ClientSystemResolver.SYSTEM_HEADER, systemType)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(admin.getUsername(), password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.uid").value(admin.getId()))
                .andExpect(jsonPath("$.data.systemType").value(systemType))
                .andReturn();
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsByteArray());
        issuedTokens.add(body.path("data").path("token").asText());
        return body;
    }

    private void loginExpecting(String password, int code, String message) throws Exception {
        mockMvc.perform(post(AUTH_API + "/login")
                        .header(ClientSystemResolver.SYSTEM_HEADER, PC_SYSTEM)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(admin.getUsername(), password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(code))
                .andExpect(jsonPath("$.msg").value(message))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    private String loginJson(String username, String password) throws Exception {
        return objectMapper.writeValueAsString(new LoginPayload(username, password));
    }

    private static class LoginPayload {

        private final String username;
        private final String password;

        private LoginPayload(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }
    }
}

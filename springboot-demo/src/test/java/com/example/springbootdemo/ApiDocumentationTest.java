package com.example.springbootdemo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiDocumentationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void exposesKnife4jAndAllThirteenBusinessOperations() throws Exception {
        mockMvc.perform(get("/doc.html"))
                .andExpect(status().isOk());

        MvcResult result = mockMvc.perform(get("/v2/api-docs"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode paths = objectMapper.readTree(result.getResponse().getContentAsByteArray())
                .path("paths");

        assertOperation(paths, "/api/v1/students", "get");
        assertOperation(paths, "/api/v1/students", "post");
        assertOperation(paths, "/api/v1/students/{id}", "get");
        assertOperation(paths, "/api/v1/students/{id}", "put");
        assertOperation(paths, "/api/v1/students/{id}", "delete");
        assertOperation(paths, "/api/v1/courses", "get");
        assertOperation(paths, "/api/v1/courses", "post");
        assertOperation(paths, "/api/v1/courses/{id}", "put");
        assertOperation(paths, "/api/v1/courses/{id}", "delete");
        assertOperation(paths, "/api/v1/classes", "get");
        assertOperation(paths, "/api/v1/classes", "post");
        assertOperation(paths, "/api/v1/classes/{id}", "put");
        assertOperation(paths, "/api/v1/classes/{id}", "delete");
    }

    private void assertOperation(JsonNode paths, String path, String method) {
        assertTrue(
                paths.path(path).has(method),
                () -> method.toUpperCase() + " " + path + " is missing from Swagger");
    }
}

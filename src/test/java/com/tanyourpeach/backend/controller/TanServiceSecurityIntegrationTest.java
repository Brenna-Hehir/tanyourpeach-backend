package com.tanyourpeach.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tanyourpeach.backend.dto.ServiceCreateRequest;
import com.tanyourpeach.backend.model.ServiceType;
import com.tanyourpeach.backend.util.TestAuthHelper;
import com.tanyourpeach.backend.util.TestDataCleaner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TanServiceSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestDataCleaner testDataCleaner;

    @Autowired
    private TestAuthHelper testAuthHelper;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;
    private String userToken;

    @BeforeEach
    void setup() {
        testDataCleaner.cleanAll();

        adminToken = testAuthHelper.generateTokenFor("admin-services@example.com", true);
        userToken = testAuthHelper.generateTokenFor("user-services@example.com", false);
    }

    @Test
    void publicServiceList_shouldBePublic() throws Exception {
        mockMvc.perform(get("/api/services"))
                .andExpect(status().isOk());
    }

    @Test
    void adminServiceList_shouldRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/services/admin"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminServiceList_shouldRejectNonAdminUser() throws Exception {
        mockMvc.perform(get("/api/services/admin")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminServiceList_shouldAllowAdminUser() throws Exception {
        mockMvc.perform(get("/api/services/admin")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    void createService_shouldRequireAuthentication() throws Exception {
        mockMvc.perform(post("/api/services")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validCreateRequest())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createService_shouldRejectNonAdminUser() throws Exception {
        mockMvc.perform(post("/api/services")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validCreateRequest())))
                .andExpect(status().isForbidden());
    }

    @Test
    void createService_shouldAllowAdminUser() throws Exception {
        mockMvc.perform(post("/api/services")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validCreateRequest())))
                .andExpect(status().isCreated());
    }

    private ServiceCreateRequest validCreateRequest() {
        ServiceCreateRequest request = new ServiceCreateRequest();
        request.setName("Peach Cobbler");
        request.setSlug("peach-cobbler");
        request.setBasePrice(60.0);
        request.setDurationMinutes(30);
        request.setServiceType(ServiceType.MAIN_SERVICE);
        return request;
    }
}

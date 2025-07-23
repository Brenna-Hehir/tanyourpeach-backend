package com.tanyourpeach.backend.controller;

import com.tanyourpeach.backend.model.User;
import com.tanyourpeach.backend.repository.UserRepository;
import com.tanyourpeach.backend.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AdminStatsControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    private String adminToken;
    
    private String userToken;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        User admin = new User();
        admin.setName("Admin");
        admin.setEmail("admin@example.com");
        admin.setPasswordHash("hashed"); // value doesn't matter for test
        admin.setIsAdmin(true);
        userRepository.save(admin);
        adminToken = "Bearer " + jwtService.generateToken(admin);

        User user = new User();
        user.setName("Regular User");
        user.setEmail("user@example.com");
        user.setPasswordHash("hashed");
        user.setIsAdmin(false);
        userRepository.save(user);
        userToken = "Bearer " + jwtService.generateToken(user);
    }

    @Test
    void getSummary_shouldReturnOkForAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/stats/summary")
                .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRevenue").exists())
                .andExpect(jsonPath("$.totalExpenses").exists())
                .andExpect(jsonPath("$.totalProfit").exists());
    }

    @Test
    void getMonthlyStats_shouldReturnOkForAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/stats/monthly")
                .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").isNumber())
                .andExpect(jsonPath("$[0].month").exists())
                .andExpect(jsonPath("$[0].revenue").exists())
                .andExpect(jsonPath("$[0].expenses").exists())
                .andExpect(jsonPath("$[0].profit").exists());
    }

    @Test
    void getUpcomingAppointments_shouldReturnOkForAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/stats/upcoming")
                .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getLowStockItems_shouldReturnOkForAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/stats/low-stock")
                .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void allEndpoints_shouldReturnForbiddenForNonAdmin() throws Exception {
        String[] urls = {
                "/api/admin/stats/summary",
                "/api/admin/stats/monthly",
                "/api/admin/stats/upcoming",
                "/api/admin/stats/low-stock"
        };

        for (String url : urls) {
            mockMvc.perform(get(url)
                    .header("Authorization", userToken))
                    .andExpect(status().isForbidden())
                    .andExpect(content().string("Access denied"));
        }
    }

    @Test
    void allEndpoints_shouldReturnForbiddenIfMissingToken() throws Exception {
        String[] urls = {
                "/api/admin/stats/summary",
                "/api/admin/stats/monthly",
                "/api/admin/stats/upcoming",
                "/api/admin/stats/low-stock"
        };

        for (String url : urls) {
            mockMvc.perform(get(url))
                    .andExpect(status().isForbidden())
                    .andExpect(content().string("Access denied"));
        }
    }

    @Test
    void allEndpoints_shouldReturnForbiddenForInvalidToken() throws Exception {
        String[] urls = {
                "/api/admin/stats/summary",
                "/api/admin/stats/monthly",
                "/api/admin/stats/upcoming",
                "/api/admin/stats/low-stock"
        };

        for (String url : urls) {
            mockMvc.perform(get(url)
                    .header("Authorization", "Bearer invalid.token.here"))
                    .andExpect(status().isForbidden())
                    .andExpect(content().string("Access denied"));
        }
    }

    @Test
    void allEndpoints_shouldReturnForbiddenIfTokenMissingBearerPrefix() throws Exception {
        String rawToken = adminToken.replace("Bearer ", ""); // remove prefix

        String[] urls = {
                "/api/admin/stats/summary",
                "/api/admin/stats/monthly",
                "/api/admin/stats/upcoming",
                "/api/admin/stats/low-stock"
        };

        for (String url : urls) {
            mockMvc.perform(get(url)
                    .header("Authorization", rawToken)) // no "Bearer "
                    .andExpect(status().isForbidden())
                    .andExpect(content().string("Access denied"));
        }
    }
}
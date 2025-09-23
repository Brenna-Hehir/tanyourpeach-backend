package com.tanyourpeach.backend.repository;

import org.junit.jupiter.api.BeforeEach;
import com.tanyourpeach.backend.model.FinancialLog;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.jdbc.core.JdbcTemplate;


import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = {
        // H2 in MySQL mode so FUNCTION('DATE_FORMAT', ...) works
        "spring.datasource.url=jdbc:h2:mem:finlogrepo;DB_CLOSE_DELAY=-1;MODE=MySQL;DATABASE_TO_UPPER=false",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.sql.init.mode=never",
        "spring.flyway.enabled=false",
        "spring.liquibase.enabled=false",
        "spring.cloud.gcp.secretmanager.enabled=false",
        "spring.cloud.gcp.sql.enabled=false"
})
class FinancialLogRepositoryTest {

    @Autowired private FinancialLogRepository financialLogRepository;
    @Autowired private JdbcTemplate jdbc;

    @BeforeEach
    void registerMysqlCompat() {
        // Be tolerant if it doesn't exist yet
        try {
            jdbc.execute("DROP ALIAS IF EXISTS DATE_FORMAT");
        } catch (Exception ignore) { /* no-op */ }

        // Create alias mapping to our test helper
        jdbc.execute(
            "CREATE ALIAS DATE_FORMAT FOR " +
            "\"com.tanyourpeach.backend.testutil.H2MysqlCompat.dateFormat\""
        );
    }

    private FinancialLog seed(FinancialLog.Type type, String amount, LocalDateTime when, String note) {
        FinancialLog f = new FinancialLog();
        f.setType(type);                              // enum: revenue/expense (lowercase)
        f.setAmount(new BigDecimal(amount));          // > 0.00 (per @DecimalMin 0.01)
        f.setDescription(note);                       // optional
        f = financialLogRepository.save(f);           // persist to get ID
        jdbc.update("UPDATE financial_log SET log_date=? WHERE log_id=?",
                Timestamp.valueOf(when), f.getLogId()); // set date for DATE_FORMAT(...) query
        return f;
    }

    @Test
    @DisplayName("sumByType: sums amounts by type")
    void sumByType_basic() {
        seed(FinancialLog.Type.revenue, "100.00", LocalDateTime.of(2025,10,1,10,0), "A");
        seed(FinancialLog.Type.revenue, "50.50",  LocalDateTime.of(2025,10,2,10,0), "B");
        seed(FinancialLog.Type.expense, "30.00",  LocalDateTime.of(2025,10,3,10,0), "C");

        assertEquals(new BigDecimal("150.50"), financialLogRepository.sumByType(FinancialLog.Type.revenue));
        assertEquals(new BigDecimal("30.00"),  financialLogRepository.sumByType(FinancialLog.Type.expense));
    }

    @Test
    @DisplayName("sumByTypeAndMonth: sums by type filtered to YYYY-MM via DATE_FORMAT")
    void sumByTypeAndMonth_monthly() {
        // Oct 2025
        seed(FinancialLog.Type.revenue, "10.00", LocalDateTime.of(2025,10,10,9,0), "oct-a");
        seed(FinancialLog.Type.revenue, "15.00", LocalDateTime.of(2025,10,11,9,0), "oct-b");
        seed(FinancialLog.Type.expense, "5.00",  LocalDateTime.of(2025,10,12,9,0), "oct-c");

        // Nov 2025 (excluded for 2025-10)
        seed(FinancialLog.Type.revenue, "99.99", LocalDateTime.of(2025,11,1,9,0), "nov-x");

        assertEquals(new BigDecimal("25.00"),
                financialLogRepository.sumByTypeAndMonth(FinancialLog.Type.revenue, "2025-10"));
        assertEquals(new BigDecimal("5.00"),
                financialLogRepository.sumByTypeAndMonth(FinancialLog.Type.expense, "2025-10"));
        assertEquals(new BigDecimal("99.99"),
                financialLogRepository.sumByTypeAndMonth(FinancialLog.Type.revenue, "2025-11"));
    }
}
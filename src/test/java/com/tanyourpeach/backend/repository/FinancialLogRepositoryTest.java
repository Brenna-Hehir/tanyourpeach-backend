package com.tanyourpeach.backend.repository;

import com.tanyourpeach.backend.model.FinancialLog;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = {
        // Plain H2; no MySQL mode needed since we aren't using DATE_FORMAT here
        "spring.datasource.url=jdbc:h2:mem:finlogrepo;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.sql.init.mode=never",
        "spring.flyway.enabled=false",
        "spring.liquibase.enabled=false"
})
class FinancialLogRepositoryTest {

    @Autowired
    private FinancialLogRepository repo;

    private FinancialLog save(FinancialLog.Type type, String amount) {
        FinancialLog f = new FinancialLog();
        f.setType(type);                         // enum is lowercase: revenue / expense
        f.setAmount(new BigDecimal(amount));     // > 0.00 per @DecimalMin
        f.setDescription("seed");
        return repo.save(f);
    }

    @Test
    @DisplayName("sumByType: sums amounts by type")
    void sumByType_basic() {
        save(FinancialLog.Type.revenue, "100.00");
        save(FinancialLog.Type.revenue, "50.50");
        save(FinancialLog.Type.expense, "30.00");

        assertEquals(new BigDecimal("150.50"), repo.sumByType(FinancialLog.Type.revenue));
        assertEquals(new BigDecimal("30.00"),  repo.sumByType(FinancialLog.Type.expense));
    }

    @Test
    @DisplayName("sumByType: returns null when no rows of that type exist")
    void sumByType_none_returnsNull() {
        // Only expenses present
        save(FinancialLog.Type.expense, "12.34");
        assertNull(repo.sumByType(FinancialLog.Type.revenue));
    }
}
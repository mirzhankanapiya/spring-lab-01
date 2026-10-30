package kz.iitu.springlab.bank;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class PropagationTest {

    @Autowired PropagationDemoService service;
    @Autowired AuditRepository auditRepository;
    @Autowired JdbcTemplate jdbc;

    @BeforeEach
    void clearAudit() {
        jdbc.update("DELETE FROM audit_log");
    }

    @Test
    void requiredInnerIsRolledBackWithOuter() {
        assertThrows(IllegalStateException.class, () -> service.outerWithRequired(true));

        // inner joined the outer transaction -> rolled back together
        assertThat(auditRepository.count()).isZero();
    }

    @Test
    void requiresNewInnerSurvivesOuterRollback() {
        assertThrows(IllegalStateException.class, () -> service.outerWithRequiresNew(true));

        // inner committed in its own transaction -> record remains
        assertThat(auditRepository.count()).isEqualTo(1);
    }

    @Test
    void bothModesCommitWhenOuterSucceeds() {
        service.outerWithRequired(false);
        service.outerWithRequiresNew(false);

        assertThat(auditRepository.count()).isEqualTo(2);
    }
}
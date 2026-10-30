package kz.iitu.springlab.bank;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class IsolationTest {

    @Autowired PlatformTransactionManager txManager;
    @Autowired JdbcTemplate jdbc;

    @BeforeEach
    void reset() {
        jdbc.update("UPDATE accounts SET balance = 1000.00 WHERE owner_name = 'Alice'");
    }

    private BigDecimal readAlice() {
        return jdbc.queryForObject(
                "SELECT balance FROM accounts WHERE owner_name = 'Alice'", BigDecimal.class);
    }

    // T1 reads twice with the given isolation; between the reads T2 (a separate,
    // independent transaction) updates the same row and commits.
    private BigDecimal[] run(int isolationLevel) {
        TransactionTemplate t1 = new TransactionTemplate(txManager);
        t1.setIsolationLevel(isolationLevel);

        TransactionTemplate t2 = new TransactionTemplate(txManager);
        t2.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        return t1.execute(status -> {
            BigDecimal first = readAlice();
            t2.executeWithoutResult(s ->
                    jdbc.update("UPDATE accounts SET balance = 777.00 WHERE owner_name = 'Alice'"));
            BigDecimal second = readAlice();
            return new BigDecimal[]{first, second};
        });
    }

    @Test
    void readCommitted() {
        BigDecimal[] r = run(TransactionDefinition.ISOLATION_READ_COMMITTED);
        System.out.println("ISOLATION READ_COMMITTED  : first=" + r[0] + " second=" + r[1]);

        // non-repeatable read: T1 sees T2's committed change
        assertThat(r[0]).isEqualByComparingTo("1000.00");
        assertThat(r[1]).isEqualByComparingTo("777.00");
    }

    @Test
    void repeatableRead() {
        BigDecimal[] r = run(TransactionDefinition.ISOLATION_REPEATABLE_READ);
        System.out.println("ISOLATION REPEATABLE_READ : first=" + r[0] + " second=" + r[1]);

        // no hard assertion: the lab asks to RECORD the actual H2 behaviour
        assertThat(r[0]).isEqualByComparingTo("1000.00");
    }
}
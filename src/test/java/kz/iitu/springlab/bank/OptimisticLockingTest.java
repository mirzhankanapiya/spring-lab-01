package kz.iitu.springlab.bank;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class OptimisticLockingTest {

    @Autowired AccountRepository repository;
    @Autowired JdbcTemplate jdbc;

    Long aliceId;

    @BeforeEach
    void reset() {
        jdbc.update("UPDATE accounts SET balance = 1000.00, version = 0 WHERE owner_name = 'Alice'");
        aliceId = repository.findByOwnerName("Alice").orElseThrow().getId();
    }

    @Test
    void staleUpdateIsRejected() {
        // two independent copies of the same row (each findById runs in its own transaction,
        // so both entities are detached and both carry version = 0)
        Account copy1 = repository.findById(aliceId).orElseThrow();
        Account copy2 = repository.findById(aliceId).orElseThrow();
        System.out.println("OPTLOCK loaded: copy1.version=" + copy1.getVersion()
                + " copy2.version=" + copy2.getVersion());

        // first copy: update and commit -> version becomes 1
        copy1.credit(new BigDecimal("10.00"));
        repository.save(copy1);

        // second copy is stale (still version 0): must fail, must not overwrite
        copy2.credit(new BigDecimal("50.00"));
        OptimisticLockingFailureException ex = assertThrows(
                OptimisticLockingFailureException.class, () -> repository.save(copy2));
        System.out.println("OPTLOCK conflict: " + ex.getClass().getSimpleName());

        // first update survived, second one did not overwrite it
        Account current = repository.findById(aliceId).orElseThrow();
        System.out.println("OPTLOCK final: balance=" + current.getBalance()
                + " version=" + current.getVersion());
        assertThat(current.getBalance()).isEqualByComparingTo("1010.00");
        assertThat(current.getVersion()).isEqualTo(1L);
    }

    @Test
    void safeResponseRefreshAndRetry() {
        Account copy1 = repository.findById(aliceId).orElseThrow();
        Account stale = repository.findById(aliceId).orElseThrow();

        copy1.credit(new BigDecimal("10.00"));
        repository.save(copy1);

        stale.credit(new BigDecimal("50.00"));
        assertThrows(OptimisticLockingFailureException.class, () -> repository.save(stale));

        // safe response: reload fresh data and repeat the (idempotent-safe) operation
        Account fresh = repository.findById(aliceId).orElseThrow();
        fresh.credit(new BigDecimal("50.00"));
        repository.save(fresh);

        Account current = repository.findById(aliceId).orElseThrow();
        System.out.println("OPTLOCK retry: balance=" + current.getBalance()
                + " version=" + current.getVersion());
        assertThat(current.getBalance()).isEqualByComparingTo("1060.00");
        assertThat(current.getVersion()).isEqualTo(2L);
    }
}
package kz.iitu.springlab.bank;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class TransferServiceTest {

    @Autowired TransferService service;
    @Autowired AccountRepository repository;
    @Autowired JdbcTemplate jdbc;

    Long aliceId;
    Long bobId;

    @BeforeEach
    void resetBalances() {
        jdbc.update("UPDATE accounts SET balance = 1000.00 WHERE owner_name = 'Alice'");
        jdbc.update("UPDATE accounts SET balance = 500.00 WHERE owner_name = 'Bob'");
        aliceId = repository.findByOwnerName("Alice").orElseThrow().getId();
        bobId = repository.findByOwnerName("Bob").orElseThrow().getId();
    }

    private BigDecimal balance(Long id) {
        return repository.findById(id).orElseThrow().getBalance();
    }

    @Test
    void successfulTransfer() {
        service.transfer(aliceId, bobId, new BigDecimal("100.00"));

        assertThat(balance(aliceId)).isEqualByComparingTo("900.00");
        assertThat(balance(bobId)).isEqualByComparingTo("600.00");
    }

    @Test
    void insufficientFundsChangesNothing() {
        assertThrows(IllegalStateException.class,
                () -> service.transfer(aliceId, bobId, new BigDecimal("5000.00")));

        assertThat(balance(aliceId)).isEqualByComparingTo("1000.00");
        assertThat(balance(bobId)).isEqualByComparingTo("500.00");
    }

    @Test
    void uncheckedExceptionRollsBack() {
        assertThrows(IllegalStateException.class,
                () -> service.transferFailAfterDebit(aliceId, bobId, new BigDecimal("100.00")));

        assertThat(balance(aliceId)).isEqualByComparingTo("1000.00");
        assertThat(balance(bobId)).isEqualByComparingTo("500.00");
    }

    @Test
    void checkedExceptionDoesNotRollBackByDefault() {
        assertThrows(TransferException.class,
                () -> service.transferCheckedNoRollback(aliceId, bobId, new BigDecimal("100.00")));

        // the debit was COMMITTED, Bob never got the money
        assertThat(balance(aliceId)).isEqualByComparingTo("900.00");
        assertThat(balance(bobId)).isEqualByComparingTo("500.00");
    }

    @Test
    void checkedExceptionWithRollbackForRollsBack() {
        assertThrows(TransferException.class,
                () -> service.transferCheckedWithRollbackFor(aliceId, bobId, new BigDecimal("100.00")));

        assertThat(balance(aliceId)).isEqualByComparingTo("1000.00");
        assertThat(balance(bobId)).isEqualByComparingTo("500.00");
    }
}
package kz.iitu.springlab.bank;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class TransferService {

    private final AccountRepository repository;

    public TransferService(AccountRepository repository) {
        this.repository = repository;
    }

    // Normal transfer (from the lab handout)
    @Transactional
    public void transfer(Long fromId, Long toId, BigDecimal amount) {
        Account from = repository.findById(fromId).orElseThrow();
        Account to = repository.findById(toId).orElseThrow();

        from.debit(amount);
        to.credit(amount);
        repository.save(from);
        repository.save(to);
    }

    // Test 3: unchecked exception after debit, before credit
    @Transactional
    public void transferFailAfterDebit(Long fromId, Long toId, BigDecimal amount) {
        Account from = repository.findById(fromId).orElseThrow();
        from.debit(amount);
        repository.saveAndFlush(from);   // the UPDATE is really sent to the DB
        throw new IllegalStateException("Simulated failure after debit");
    }

    // Test 4a: checked exception, default behaviour (NO rollback)
    @Transactional
    public void transferCheckedNoRollback(Long fromId, Long toId, BigDecimal amount) throws TransferException {
        Account from = repository.findById(fromId).orElseThrow();
        from.debit(amount);
        repository.saveAndFlush(from);
        throw new TransferException("Checked failure after debit");
    }

    // Test 4b: checked exception with rollbackFor (rollback happens)
    @Transactional(rollbackFor = TransferException.class)
    public void transferCheckedWithRollbackFor(Long fromId, Long toId, BigDecimal amount) throws TransferException {
        Account from = repository.findById(fromId).orElseThrow();
        from.debit(amount);
        repository.saveAndFlush(from);
        throw new TransferException("Checked failure after debit");
    }
}
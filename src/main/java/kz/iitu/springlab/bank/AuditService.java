package kz.iitu.springlab.bank;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

    private final AuditRepository repository;

    public AuditService(AuditRepository repository) {
        this.repository = repository;
    }

    // joins the caller's transaction
    @Transactional(propagation = Propagation.REQUIRED)
    public void logRequired(String message) {
        repository.save(new AuditRecord(message));
    }

    // suspends the caller's transaction and commits on its own
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logRequiresNew(String message) {
        repository.save(new AuditRecord(message));
    }
}
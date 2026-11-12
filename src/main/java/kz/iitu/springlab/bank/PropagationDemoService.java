package kz.iitu.springlab.bank;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PropagationDemoService {

    private final AuditService audit;

    public PropagationDemoService(AuditService audit) {
        this.audit = audit;
    }

    @Transactional
    public void outerWithRequired(boolean fail) {
        audit.logRequired("audit written with REQUIRED");
        if (fail) {
            throw new IllegalStateException("Outer failed after inner returned (REQUIRED)");
        }
    }

    @Transactional
    public void outerWithRequiresNew(boolean fail) {
        audit.logRequiresNew("audit written with REQUIRES_NEW");
        if (fail) {
            throw new IllegalStateException("Outer failed after inner returned (REQUIRES_NEW)");
        }
    }
}
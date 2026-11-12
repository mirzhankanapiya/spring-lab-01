package kz.iitu.springlab.bank;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.output.MigrateResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class FlywayHistoryTest {

    @Autowired
    Flyway flyway;

    @Test
    void appliedMigrationsAreNotExecutedAgain() {
        MigrationInfo[] applied = flyway.info().applied();

        System.out.println("FLYWAY applied migrations: " + applied.length);
        for (MigrationInfo info : applied) {
            System.out.println("FLYWAY v" + info.getVersion()
                    + " | " + info.getDescription()
                    + " | " + info.getState()
                    + " | checksum=" + info.getChecksum());
        }
        assertThat(applied.length).isGreaterThanOrEqualTo(4);

        // a second migrate() on the same database must do nothing
        MigrateResult result = flyway.migrate();
        System.out.println("FLYWAY second migrate: migrationsExecuted=" + result.migrationsExecuted);
        assertThat(result.migrationsExecuted).isZero();
    }
}
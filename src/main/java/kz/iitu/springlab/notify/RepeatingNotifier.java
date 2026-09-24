package kz.iitu.springlab.notify;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component("repeating")
@Order(3)
class RepeatingNotifier implements Notifier {

    private static final Logger log = LoggerFactory.getLogger(RepeatingNotifier.class);

    private final int times;

    RepeatingNotifier(@Value("${app.repeat-count:3}") int times) {
        this.times = times;
    }

    @PostConstruct
    void init() {
        log.info("REPEATING >> initialised, N = {}", times);
    }

    @Override
    public String send(String message) {
        return "repeating: " + message.repeat(times);
    }

    @Override
    public String channel() { return "repeating"; }
}
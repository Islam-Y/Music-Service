package ru.itmo.music.music_service.infrastructure.messaging.outbox;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.itmo.music.music_service.infrastructure.persistence.OutboxRepository;
import ru.itmo.music.music_service.infrastructure.persistence.entity.enums.OutboxStatus;

@Component
@AllArgsConstructor
public class OutboxMonitor {

    private static final Logger log = LoggerFactory.getLogger(OutboxMonitor.class);

    private final OutboxRepository outboxRepository;

    @Scheduled(fixedDelayString = "${app.outbox.monitor-interval-ms:60000}")
    public void logOutboxStats() {
        long ready = outboxRepository.countByStatus(OutboxStatus.READY);
        long failed = outboxRepository.countByStatus(OutboxStatus.FAILED);
        if (failed > 0) {
            log.warn("outbox_status ready={} failed={}", ready, failed);
        } else {
            log.info("outbox_status ready={} failed={}", ready, failed);
        }
    }
}

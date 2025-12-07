package ru.itmo.music.music_service.infrastructure.messaging.outbox;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class TraceContextProvider {

    private static final String TRACE_ID_KEY = "traceId";
    private static final String CORRELATION_ID_KEY = "correlationId";

    public String currentTraceId() {
        return Optional.ofNullable(MDC.get(TRACE_ID_KEY))
                .or(() -> Optional.ofNullable(MDC.get(CORRELATION_ID_KEY)))
                .orElse(UUID.randomUUID().toString());
    }

    public String currentCorrelationId() {
        return Optional.ofNullable(MDC.get(CORRELATION_ID_KEY))
                .or(() -> Optional.ofNullable(MDC.get(TRACE_ID_KEY)))
                .orElse(UUID.randomUUID().toString());
    }
}

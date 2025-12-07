package ru.itmo.music.music_service.infrastructure.messaging.kafka;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.slf4j.MDC;
import org.springframework.kafka.listener.RecordInterceptor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class MdcRecordInterceptor implements RecordInterceptor<String, Object> {

    private static final String TRACE_ID = "traceId";
    private static final String CORRELATION_ID = "correlationId";


    @Override
    public ConsumerRecord<String, Object> intercept(ConsumerRecord<String, Object> record, Consumer<String, Object> consumer) {
        putIfPresent(record, TRACE_ID);
        putIfPresent(record, CORRELATION_ID);
        return record;
    }

    @Override
    public void afterRecord(ConsumerRecord<String, Object> record, Consumer<String, Object> consumer) {
        clear();
    }

    private void putIfPresent(ConsumerRecord<String, Object> record, String key) {
        Header header = record.headers().lastHeader(key);
        if (header != null) {
            MDC.put(key, new String(header.value(), StandardCharsets.UTF_8));
        }
    }

    private void clear() {
        MDC.remove(TRACE_ID);
        MDC.remove(CORRELATION_ID);
    }
}

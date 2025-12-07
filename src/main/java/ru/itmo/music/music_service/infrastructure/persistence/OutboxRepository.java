package ru.itmo.music.music_service.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.itmo.music.music_service.infrastructure.persistence.entity.OutboxMessageEntity;
import ru.itmo.music.music_service.infrastructure.persistence.entity.enums.OutboxStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OutboxRepository extends JpaRepository<OutboxMessageEntity, UUID> {

    @Query("select o from OutboxMessageEntity o where o.status = :status and o.availableAt <= :now order by o.availableAt asc")
    List<OutboxMessageEntity> findReady(@Param("status") OutboxStatus status, @Param("now") Instant now);

    long countByStatus(OutboxStatus status);

    List<OutboxMessageEntity> findTop100ByStatusOrderByUpdatedAtAsc(OutboxStatus status);
}

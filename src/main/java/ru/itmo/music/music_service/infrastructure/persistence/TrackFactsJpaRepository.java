package ru.itmo.music.music_service.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.itmo.music.music_service.infrastructure.persistence.entity.TrackFactsEntity;

public interface TrackFactsJpaRepository extends JpaRepository<TrackFactsEntity, String> {
}

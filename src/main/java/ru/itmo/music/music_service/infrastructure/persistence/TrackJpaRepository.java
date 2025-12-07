package ru.itmo.music.music_service.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.itmo.music.music_service.infrastructure.persistence.entity.TrackEntity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * Spring Data repository for TrackEntity.
 */
public interface TrackJpaRepository extends JpaRepository<TrackEntity, String> {

    Page<TrackEntity> findByTitleContainingIgnoreCaseOrArtistContainingIgnoreCase(String title, String artist, Pageable pageable);

    Optional<TrackEntity> findByAudioStorageKey(String audioStorageKey);
}

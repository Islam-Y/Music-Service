package ru.itmo.music.music_service.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "track_facts")
public class TrackFactsEntity {

    @Id
    @Column(name = "track_id")
    private String trackId;

    @Column(name = "facts_json", columnDefinition = "text")
    private String factsJson;

    @Column(name = "generated_at")
    private Instant generatedAt;

    @Column(name = "updated_at")
    private Instant updatedAt;
}

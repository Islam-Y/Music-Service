package ru.itmo.music.music_service.application.mapper;

import ru.itmo.music.music_service.api.dto.TrackDetailsResponse;
import ru.itmo.music.music_service.api.dto.TrackSummaryResponse;
import ru.itmo.music.music_service.infrastructure.persistence.entity.TrackEntity;

import org.mapstruct.Mapper;

/**
 * Mapper between persistence entities and API DTOs.
 */
@Mapper(componentModel = "spring")
public interface TrackMapper {

    TrackSummaryResponse toSummary(TrackEntity entity);

    TrackDetailsResponse toDetails(TrackEntity entity);
}

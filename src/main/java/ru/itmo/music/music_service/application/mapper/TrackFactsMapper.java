package ru.itmo.music.music_service.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import ru.itmo.music.music_service.api.dto.TrackFactsResponse;
import ru.itmo.music.music_service.infrastructure.persistence.entity.TrackFactsEntity;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TrackFactsMapper {

    @Mapping(source = "trackId", target = "trackId")
    TrackFactsResponse toResponse(TrackFactsEntity entity);
}

package ru.itmo.music.music_service.infrastructure.storage;

public interface StorageService {

    String generatePresignedGetUrl(String objectKey);
    String generatePresignedPutUrl(String objectKey, String contentType);

    void deleteObject(String objectKey);

    boolean objectExists(String objectKey);
}

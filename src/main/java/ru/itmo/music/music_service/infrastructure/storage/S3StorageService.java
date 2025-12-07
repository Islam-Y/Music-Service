package ru.itmo.music.music_service.infrastructure.storage;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import ru.itmo.music.music_service.config.S3Properties;

import java.net.URL;
import java.time.Duration;

/**
 * S3-backed StorageService that issues presigned URLs and wraps basic object operations.
 */
@Component
@RequiredArgsConstructor
public class S3StorageService implements StorageService {

    private static final Logger log = LoggerFactory.getLogger(S3StorageService.class);

    private final S3Properties properties;
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Override
    public String generatePresignedGetUrl(String objectKey) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(properties.getBucket())
                .key(objectKey)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(properties.getPresignTtlSeconds()))
                .getObjectRequest(getObjectRequest)
                .build();

        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
        URL url = presignedRequest.url();
        return url.toString();
    }

    @Override
    public String generatePresignedPutUrl(String objectKey, String contentType) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(properties.getBucket())
                .key(objectKey)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(properties.getPresignTtlSeconds()))
                .putObjectRequest(putObjectRequest)
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
        URL url = presignedRequest.url();
        return url.toString();
    }

    @Override
    public void deleteObject(String objectKey) {
        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(properties.getBucket())
                    .key(objectKey)
                    .build();
            s3Client.deleteObject(deleteRequest);
        } catch (S3Exception e) {
            log.warn("Failed to delete object {} from bucket {}: {}", objectKey, properties.getBucket(), e.awsErrorDetails().errorMessage());
        }
    }

    @Override
    public boolean objectExists(String objectKey) {
        try {
            HeadObjectRequest head = HeadObjectRequest.builder()
                    .bucket(properties.getBucket())
                    .key(objectKey)
                    .build();
            s3Client.headObject(head);
            return true;
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                return false;
            }
            log.warn("Error checking object existence {} in bucket {}: {}", objectKey, properties.getBucket(), e.awsErrorDetails().errorMessage());
            return false;
        }
    }
}

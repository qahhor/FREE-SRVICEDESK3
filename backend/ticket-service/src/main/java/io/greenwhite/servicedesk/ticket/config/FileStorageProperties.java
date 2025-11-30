package io.greenwhite.servicedesk.ticket.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for file storage
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "file.storage")
public class FileStorageProperties {

    /**
     * Storage type: LOCAL, S3, MINIO
     */
    private String type = "LOCAL";

    /**
     * Local storage directory path
     */
    private String uploadDir = "./uploads";

    /**
     * Maximum file size in bytes (default: 10MB)
     */
    private Long maxFileSize = 10485760L;

    /**
     * Allowed file extensions (comma-separated)
     */
    private String allowedExtensions = "jpg,jpeg,png,gif,pdf,doc,docx,xls,xlsx,txt,zip";

    /**
     * S3 bucket name
     */
    private String s3Bucket;

    /**
     * S3 region
     */
    private String s3Region = "us-east-1";

    /**
     * S3 access key
     */
    private String s3AccessKey;

    /**
     * S3 secret key
     */
    private String s3SecretKey;

    /**
     * MinIO endpoint URL
     */
    private String minioEndpoint;

    /**
     * Base URL for download links
     */
    private String baseDownloadUrl = "/api/v1/attachments";
}

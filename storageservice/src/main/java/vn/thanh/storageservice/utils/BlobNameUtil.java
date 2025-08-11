package vn.thanh.storageservice.utils;

import java.util.UUID;

public class BlobNameUtil {

    /**
     * Tạo blob name duy nhất cho Azure Blob Storage.
     *
     * @param metadataId       ID metadata (thư mục cấp 1)
     * @param versionId        ID version (thư mục cấp 2)
     * @param originalFilename Tên file gốc (VD: abc.pdf)
     * @return Blob name dạng: {metadataId}/{versionId}/{UUID}_{originalFilename}
     */
    public static String generateBlobName(Long metadataId, Long versionId, String originalFilename) {
        // UUID random để tránh collision
        String uniqueId = UUID.randomUUID().toString();

        // Tránh ký tự xấu trong tên file
        String safeFileName = originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");

        return String.format("%s/%s/%s_%s", metadataId, versionId, uniqueId, safeFileName);
    }
}
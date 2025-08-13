package vn.thanh.storageservice.service;

import vn.thanh.storageservice.dto.DeleteBlobsResult;

import java.io.File;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface IAzureStorageService {
    String uploadChunkedWithContainerDefault(InputStream data, String originalFileName, long length, int chunkSize);

    CompletableFuture<String> uploadChunk(InputStream data, String originalFileName, long length, int chunkSize);

    String copyBlob(String sourceBlobName);

    void deleteBlob(String blobName);

    DeleteBlobsResult deleteBlobs(Map<Long, String> metadataBlobMap);

    InputStream downloadBlobInputStream(String blobName); // Tải blob về

    File downloadToFile(String blobName, String tempDirPath);

    String getBlobUrl(String blobName); // Lấy url của blob

    String getUrlUpload(String blobName);

}

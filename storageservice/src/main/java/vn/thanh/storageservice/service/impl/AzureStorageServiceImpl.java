package vn.thanh.storageservice.service.impl;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.azure.storage.common.sas.SasProtocol;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.thanh.storageservice.dto.DeleteBlobsResult;
import vn.thanh.storageservice.exception.CustomBlobStorageException;
import vn.thanh.storageservice.exception.ResourceNotFoundException;
import vn.thanh.storageservice.service.IAzureStorageService;
import vn.thanh.storageservice.utils.TextUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
@RequiredArgsConstructor
@Slf4j
public class AzureStorageServiceImpl implements IAzureStorageService {
    private final BlobServiceClient blobServiceClient;
    @Value("${spring.cloud.azure.storage.blob.container-name}")
    private String containerNameDefault;
    private static final int MAX_RETRIES = 3;
    private static final int THREAD_COUNT = 5;

    @Override
    public String uploadChunkedWithContainerDefault(InputStream data, String originalFileName, long length, int chunkSize) {
        try {
            BlobContainerClient blobContainerClient = blobServiceClient.getBlobContainerClient(containerNameDefault);
            String newFileName = UUID.randomUUID() + "_" + TextUtils.normalizeFileName(originalFileName);
            BlockBlobClient blockBlobClient = blobContainerClient.getBlobClient(newFileName).getBlockBlobClient();
            List<String> blockIds = new ArrayList<>();
            byte[] buffer = new byte[chunkSize];
            int bytesRead;
            int blockNumber = 0;

            while ((bytesRead = data.read(buffer)) != -1) {
                String blockId = Base64.getEncoder().encodeToString(String.format("%06d", blockNumber).getBytes()); // Tạo Block ID
                blockBlobClient.stageBlock(blockId, new ByteArrayInputStream(buffer, 0, bytesRead), bytesRead);  // Upload từng phần
                blockIds.add(blockId);
                // 📌 In log để biết phần nào đã upload xong
                // 📌 In log với số phần upload thành công
                System.out.println("✅ Đã upload thành công phần " + (blockNumber + 1) + " trên tổng số " + ((length + chunkSize - 1) / chunkSize) + " phần");
                blockNumber++;
            }

            // Ghép các phần lại
            blockBlobClient.commitBlockList(blockIds);

            return blockBlobClient.getBlobName();
        } catch (IOException | BlobStorageException e) {
            log.error("Error uploading file from InputStream: {}", e.getMessage());
            throw new CustomBlobStorageException("Lỗi upload file ");
        }
    }

    @Override
    public CompletableFuture<String> uploadChunk(InputStream data, String originalFileName, long length, int chunkSize) {
        try {
            BlobContainerClient blobContainerClient = blobServiceClient.getBlobContainerClient(containerNameDefault);
            String newFileName = UUID.randomUUID() + "_" + TextUtils.normalizeFileName(originalFileName);
            BlockBlobClient blockBlobClient = blobContainerClient.getBlobClient(newFileName).getBlockBlobClient();
            List<String> blockIds = new ArrayList<>();
            byte[] buffer = new byte[chunkSize];
            int bytesRead;
            int blockNumber = 0;
            while ((bytesRead = data.read(buffer)) != -1) {
                String blockId = Base64.getEncoder().encodeToString(String.format("%06d", blockNumber).getBytes()); // Tạo Block ID
                blockBlobClient.stageBlock(blockId, new ByteArrayInputStream(buffer, 0, bytesRead), bytesRead);  // Upload từng phần
                blockIds.add(blockId);
                //  In log để biết phần nào đã upload xong
                //  In log với số phần upload thành công
                System.out.println("✅ Đã upload thành công phần " + (blockNumber + 1) + " trên tổng số " + ((length + chunkSize - 1) / chunkSize) + " phần");
                blockNumber++;
            }
            // Ghép các phần lại
            blockBlobClient.commitBlockList(blockIds);

            return CompletableFuture.completedFuture(blockBlobClient.getBlobName());
        } catch (IOException | BlobStorageException e) {
            log.error("Error uploading file from InputStream: {}", e.getMessage());
            throw new CustomBlobStorageException("Lỗi upload file ");
        }
    }


    @Override
    public String copyBlob(String sourceBlobName) {
        try {
            // Lấy client của container
            BlobContainerClient blobContainerClient = blobServiceClient.getBlobContainerClient(containerNameDefault);

            // Tạo client cho blob nguồn và blob đích
            BlobClient sourceBlobClient = blobContainerClient.getBlobClient(sourceBlobName);
            String destinationBlobName = UUID.randomUUID() + "_" + sourceBlobName.substring(sourceBlobName.indexOf("_") + 1, sourceBlobName.lastIndexOf(".") - 1) + "_copy" + sourceBlobName.substring(sourceBlobName.lastIndexOf("."));
            BlobClient destinationBlobClient = blobContainerClient.getBlobClient(destinationBlobName);

            // Kiểm tra xem file nguồn có tồn tại không
            if (!sourceBlobClient.exists()) {
                throw new CustomBlobStorageException("File nguồn không tồn tại: " + sourceBlobName);
            }

            // Lấy URL của file nguồn
            String sourceUrl = sourceBlobClient.getBlobUrl();

            // Sao chép file
            destinationBlobClient.beginCopy(sourceUrl, null);

            return destinationBlobClient.getBlobName();
        } catch (BlobStorageException e) {
            log.error("Lỗi khi sao chép file: {}", e.getMessage());
            throw new CustomBlobStorageException("Lỗi khi sao chép file: " + e.getMessage());
        }
    }


    @Override
    public void deleteBlob(String blobName) {
        deleteBlobByContainerAndBlob(containerNameDefault, blobName);
    }

    private void deleteBlobByContainerAndBlob(String containerName, String blobName) {
        log.info("Deleting blob '{}' in container '{}'", blobName, containerName);
        try {
            BlockBlobClient blobClient = getBlockBlobClient(containerName, blobName);

            if (!blobClient.exists()) {
                log.warn("Blob '{}' does not exist in container '{}'", blobName, containerName);
                return;
            }
            blobClient.delete();
            log.info("Deleted blob '{}' successfully", blobName);

        } catch (Exception e) {
            log.error("Failed to delete blob '{}' in container '{}': {}", blobName, containerName, e.getMessage(), e);
        }
    }

    /**
     *
     * @param metadataBlobMap: list blob delete, key is metadata id, value is blob name belong metadata id
     * @return: DeleteBlobsResult object chứa danh sách metadata id xóa thành công và thất bại
     */
    @Override
    public DeleteBlobsResult deleteBlobs(Map<Long, String> metadataBlobMap) {
        if (metadataBlobMap == null || metadataBlobMap.isEmpty()) {
            log.warn("No blobs to delete");
            return new DeleteBlobsResult(Collections.emptyList(), Collections.emptyList());
        }

        log.info("Deleting {} blobs (parallel)...", metadataBlobMap.size());

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        List<Future<Map.Entry<Long, Boolean>>> futures = new ArrayList<>();

        for (Map.Entry<Long, String> entry : metadataBlobMap.entrySet()) {
            Long metadataId = entry.getKey();
            String blobName = entry.getValue();

            futures.add(executor.submit(() -> {
                boolean success = deleteWithRetry(blobName);
                return Map.entry(metadataId, success);
            }));
        }

        List<Long> successMetadataIds = new ArrayList<>();
        List<Long> failedMetadataIds = new ArrayList<>();

        for (Future<Map.Entry<Long, Boolean>> future : futures) {
            try {
                Map.Entry<Long, Boolean> result = future.get();
                if (Boolean.TRUE.equals(result.getValue())) {
                    successMetadataIds.add(result.getKey());
                } else {
                    failedMetadataIds.add(result.getKey());
                }
            } catch (Exception e) {
                log.error("Error while deleting blob in parallel task", e);
                // Nếu future lỗi, coi là fail
                failedMetadataIds.add(-1L); // hoặc có thể bỏ qua
            }
        }

        executor.shutdown();
        log.info("Blob deletion completed. Success: {}, Fail: {}",
                successMetadataIds.size(), failedMetadataIds.size());

        return new DeleteBlobsResult(successMetadataIds, failedMetadataIds);
    }


    public boolean deleteWithRetry(String blobName) {
        int maxRetries = 3;
        int retryCount = 0;
        while (retryCount < maxRetries) {
            try {
                // Giả sử gọi hàm xóa thực tế
                deleteBlob(blobName);  // nếu thành công sẽ không ném exception
                return true;           // xóa thành công
            } catch (Exception e) {
                retryCount++;
                log.warn("Failed to delete blob {}, retry {}/{}", blobName, retryCount, maxRetries);
                try {
                    Thread.sleep(1000); // delay trước khi retry (tuỳ chọn)
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        log.error("Failed to delete blob {} after {} retries", blobName, maxRetries);
        return false;  // xóa không thành công
    }


    @Override
    public InputStream downloadBlobInputStream(String blobName) {
        return getInputStreamBlob(containerNameDefault, blobName);
    }

    @Override
    public File downloadToFile(String blobName, String tempDirPath) {
        try {
            BlobContainerClient blobContainerClient = blobServiceClient.getBlobContainerClient(containerNameDefault);
            BlockBlobClient blockBlobClient = blobContainerClient.getBlobClient(blobName).getBlockBlobClient();

            // Tạo thư mục tạm nếu chưa tồn tại
            File tempDir = new File(tempDirPath);
            if (!tempDir.exists()) {
                tempDir.mkdirs();
            }

            // Tạo file tạm để lưu
            File downloadedFile = new File(tempDirPath + File.separator + blobName);
            blockBlobClient.downloadToFile(downloadedFile.getAbsolutePath(), true); // true = overwrite if exists

            return downloadedFile;
        } catch (Exception e) {
            log.error("Lỗi khi tải file từ Azure Blob: {}", e.getMessage());
            throw new CustomBlobStorageException("Không thể tải file từ Azure Blob: " + e.getMessage());
        }
    }

    @Override
    public String getBlobUrl(String blobName) {
        try {
            // Kết nối tới blob container
            BlobContainerClient blobContainerClient = blobServiceClient.getBlobContainerClient(containerNameDefault);
            BlobClient blobClient = blobContainerClient.getBlobClient(blobName);

            // Xác định thời gian hết hạn cho SAS token (ví dụ 1 giờ)
            OffsetDateTime expiryTime = OffsetDateTime.now().plusHours(1);

            // Tạo quyền truy cập SAS cho blob (chỉ phép đọc)
            BlobSasPermission permission = new BlobSasPermission()
                    .setReadPermission(true);

            // Tạo SAS token cho blob

            BlobServiceSasSignatureValues sasSignatureValues = new BlobServiceSasSignatureValues(expiryTime, permission)
                    .setStartTime(OffsetDateTime.now().minusMinutes(5)) // Đặt thời gian bắt đầu sớm hơn hiện tại
                    .setProtocol(SasProtocol.HTTPS_HTTP);
            String sasToken = blobClient.generateSas(sasSignatureValues);

            // Tạo và trả về URL với SAS token
            String blobUrlWithSas = blobClient.getBlobUrl() + "?" + sasToken;
            return blobUrlWithSas;
        } catch (Exception e) {
            log.error("Lỗi khi lấy URL của blob: {}", e.getMessage());
            throw new CustomBlobStorageException("Không thể lấy URL của blob: " + e.getMessage());
        }
    }


    @Override
    public String getUrlUpload(String blobName) {

        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerNameDefault);

        BlobClient blobClient = containerClient.getBlobClient(blobName);

// Cấu hình quyền ghi (upload)
        BlobSasPermission permission = new BlobSasPermission()
                .setWritePermission(true)
                //Muốn hạn chế upload overwrite thì ko cần write
                .setCreatePermission(true); // cần thiết để tạo blob

        OffsetDateTime expiryTime = OffsetDateTime.now().plusMinutes(15);

        BlobServiceSasSignatureValues values = new BlobServiceSasSignatureValues(expiryTime, permission)
                .setStartTime(OffsetDateTime.now().minusMinutes(1));

        String sasToken = blobClient.generateSas(values);
//Có thể dùng Azure Event Grid để nhận thông báo "blob created"
        return blobClient.getBlobUrl() + "?" + sasToken;
        //clien upload truc tiep bang http put:
//        curl --location --request PUT 'url trả về ' \
//--header 'x-ms-blob-type: BlockBlob' \
//--header 'Content-Type: text/plain' \
//--data-binary '@/home/thanh/Projects/microservice_jmaster/keycloak.txt'

    }

    private InputStream getInputStreamBlob(String containerName, String blobName) {
        try {
            BlockBlobClient blobClient = getBlockBlobClient(containerName, blobName);
            if (!blobClient.exists()) {
                log.error("Blob not found: {}", blobName);
                throw new ResourceNotFoundException("File không tồn tại: " + blobName);
            }
            // Đọc file từ Azure Blob Storage
            return blobClient.openInputStream();
        } catch (Exception e) {
            log.error("Error downloading blob: {}", e.getMessage());
            throw new ResourceNotFoundException("Lỗi khi tải blob: " + blobName);
        }
    }

    /**
     * Helper method to get BlockBlobClient
     */
    private BlockBlobClient getBlockBlobClient(String containerName, String blobName) {
        return blobServiceClient.getBlobContainerClient(containerName).getBlobClient(blobName).getBlockBlobClient();
    }


}

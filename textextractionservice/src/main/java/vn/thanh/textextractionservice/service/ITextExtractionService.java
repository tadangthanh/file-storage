package vn.thanh.textextractionservice.service;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

public interface ITextExtractionService {
    void extractAndSend(Long documentId, UUID ownerId, String visibility,String blobName,
                        Long categoryId, List<Long> allowedUserIds,
                        List<Long> allowedGroupIds, InputStream fileStream, String fileType);
}

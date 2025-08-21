package vn.thanh.textextractionservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import vn.thanh.textextractionservice.dto.DocumentIndexMessage;
import vn.thanh.textextractionservice.dto.DocumentReadyForExtraction;
import vn.thanh.textextractionservice.service.IOutboxService;
import vn.thanh.textextractionservice.service.ITextExtractionService;
import vn.thanh.textextractionservice.utils.FileUtil;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class TextExtractionServiceImpl implements ITextExtractionService {
    private final IOutboxService outboxService;
    private static final int CHUNK_SIZE = 1000;       // số ký tự mỗi chunk
    private static final int CHUNK_OVERLAP = 100;     // overlap 100-200 ký tự

    @Override
    public void extractAndSend(Long documentId, UUID ownerId, String visibility, String blobName,
                               Long categoryId, List<Long> allowedUserIds,
                               List<Long> allowedGroupIds, InputStream fileStream, String fileType) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(fileStream))) {
            StringBuilder buffer = new StringBuilder();
            String line;
            int chunkIndex = 0;

            while ((line = reader.readLine()) != null) {
                buffer.append(line).append("\n");

                if (buffer.length() >= CHUNK_SIZE) {
                    String chunk = buffer.toString();

                    // tạo message
                    DocumentIndexMessage message = new DocumentIndexMessage(
                            documentId,
                            1,
                            chunkIndex++,
                            chunk,
                            ownerId,
                            visibility,
                            categoryId,
                            allowedUserIds,
                            allowedGroupIds,
                            blobName,
                            new Date()
                    );
                    outboxService.addEventTextExtracted(message);

                    // giữ lại overlap, bỏ phần cũ
                    if (CHUNK_OVERLAP > 0 && buffer.length() > CHUNK_OVERLAP) {
                        buffer = new StringBuilder(buffer.substring(buffer.length() - CHUNK_OVERLAP));
                    } else {
                        buffer = new StringBuilder();
                    }
                }
            }

            // gửi phần còn sót lại
            if (!buffer.isEmpty()) {
                DocumentIndexMessage message = new DocumentIndexMessage(
                        documentId,
                        1,
                        chunkIndex,
                        buffer.toString(),
                        ownerId,
                        visibility,
                        categoryId,
                        allowedUserIds,
                        allowedGroupIds,
                        blobName,
                        new Date()
                );
                outboxService.addEventTextExtracted(message);
            }

            log.info("Text extraction completed. Total chunks: {}", chunkIndex + 1);

        } catch (Exception e) {
            log.error("Error extracting text: ", e);
        }
    }


    @KafkaListener(topics = "${app.kafka.metadata-update-topic}", groupId = "${app.kafka.text-extractor-group}")
    public void listen(DocumentReadyForExtraction message) {
        log.info("Received file for extraction: documentId={}, blobName={}",
                message.getDocumentId(), message.getBlobName());
        try (InputStream inputStream = new URL(message.getFileUrl()).openStream()) {
            this.extractAndSend(
                    message.getDocumentId(),
                    message.getOwnerId(),
                    "private", // visibility, có thể lấy từ metadata nếu cần
                    message.getBlobName(),
                    message.getCategoryId(),      // categoryId
                    null,      // allowedUserIds
                    null,      // allowedGroupIds
                    inputStream,
                    message.getType()
            );
        } catch (Exception e) {
            log.error("Failed to process file: documentId={}", message.getDocumentId(), e);
        }
    }


    private List<String> splitTextIntoChunks(String text, int chunkSize, int overlap) {
        List<String> chunks = new ArrayList<>();
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + chunkSize, text.length());
            chunks.add(text.substring(start, end));
            start = end - overlap; // overlap để tránh mất ngữ cảnh
            if (start < 0) start = 0;
        }
        return chunks;
    }
}

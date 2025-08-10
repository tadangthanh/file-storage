package vn.thanh.metadataservice.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.thanh.metadataservice.dto.MetadataUpdate;
import vn.thanh.metadataservice.entity.Category;
import vn.thanh.metadataservice.entity.File;
import vn.thanh.metadataservice.exception.ResourceNotFoundException;
import vn.thanh.metadataservice.repository.CategoryRepo;
import vn.thanh.metadataservice.repository.FileRepo;
import vn.thanh.metadataservice.service.IMetadataMapper;
import vn.thanh.metadataservice.service.IMetadataStorageService;
import vn.thanh.metadataservice.utils.AuthUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j(topic = "METADATA_STORAGE_SERVICE")
public class MetadataStorageServiceImpl implements IMetadataStorageService {
    private final IMetadataMapper documentMapperService;
    private final FileRepo fileRepo;
    private final CategoryRepo categoryRepo;
    private final CategoryValidation categoryValidation;
//    @Value("${app.delete.document-retention-days}")
//    private int documentRetentionDays;

    private File getFileByIdOrThrow(Long id) {
        return fileRepo.findById(id).orElseThrow(() -> {
            log.info("Resource not found with id {}", id);
            return new ResourceNotFoundException("Resource not found with id " + id);
        });
    }

    @Override
    public File copyFile(File file) {
        log.info("copy file name: {}, id: {} by: {}", file.getName(), file.getId(), AuthUtils.getUserId());
        File copied = new File();
        documentMapperService.copyFile(copied, file);

        String newName = generateCopyName(file.getName());
        copied.setName(newName);

        copied = fileRepo.save(copied);

        copied.setDeletedAt(null);
        copied.setPermanentDeleteAt(null);

        //  Copy related data
        copyRelatedData(file, copied);

        return fileRepo.save(copied);
    }

    private void copyRelatedData(File source, File target) {
        // index document
        // send kafka
    }

    private String generateCopyName(String originalName) {
        int lastDotIndex = originalName.lastIndexOf(".");
        return originalName.substring(0, lastDotIndex - 1) + "_copy" + originalName.substring(lastDotIndex);
    }


    @Override
    public List<File> saveFilesCategory(List<MultipartFile> files, Long categoryId) {
        log.info("Save list file to category id: {}", categoryId);
        // Lưu tài liệu vào cơ sở dữ liệu
        UUID userId = AuthUtils.getUserId();
        categoryValidation.validIsOwnerCategory(categoryId, userId);
        List<File> documents = documentMapperService.mapToListFile(files);
        for (File doc : documents) {
            doc.setOwnerId(userId);
        }
        documents = fileRepo.saveAll(documents);
        Category category = categoryRepo.findById(categoryId).orElseThrow(() -> {
            log.info("category id: {} not found", categoryId);
            return new ResourceNotFoundException("category not found");
        });
        for (File file : documents) {
            file.setCategory(category);
        }
        documents = fileRepo.saveAll(documents);
        return documents;
    }

    @Override
    public List<File> saveFiles(List<MultipartFile> files) {
        log.info("Save list file, list size: {}", files.size());
        // Lưu tài liệu vào cơ sở dữ liệu
        List<File> documents = documentMapperService.mapToListFile(files);
        UUID userId = AuthUtils.getUserId();
        for (File doc : documents) {
            doc.setOwnerId(userId);
        }
        documents = fileRepo.saveAll(documents);

        documents = fileRepo.saveAll(documents);
        return documents;
    }

    @Override
    public void softDeleteFile(Long fileId) {
        log.info("soft delete file with id {}", fileId);
        File file = getFileByIdOrThrow(fileId);
        file.setDeletedAt(LocalDateTime.now());
        file.setPermanentDeleteAt(LocalDateTime.now().plusDays(7));
    }

    @Override
    public void hardDeleteFile(Long fileId) {
        log.info("hard delete file with id {}", fileId);
        File file = getFileByIdOrThrow(fileId);
        fileRepo.delete(file);
        // send kafka
    }

    @Override
    public void detachedCategory(Long categoryId) {
        log.info("detached file category id: {}", categoryId);
        List<File> files = fileRepo.getFilesByCategoryId(categoryId);
        for (File f : files) {
            f.setCategory(null);
        }
        fileRepo.saveAll(files);
    }

    // Lắng nghe topic "my-topic" với groupId "my-consumer-group"
    @KafkaListener(topics = "metadata", groupId = "metadata-group")
    public void listenUpdateMetadata(MetadataUpdate metadataUpdate) {
        System.out.println("Nhận message: " + metadataUpdate.toString());
    }

}

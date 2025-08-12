package vn.thanh.metadataservice.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.thanh.metadataservice.dto.*;
import vn.thanh.metadataservice.entity.Category;
import vn.thanh.metadataservice.entity.File;
import vn.thanh.metadataservice.exception.AccessDeniedException;
import vn.thanh.metadataservice.exception.ResourceNotFoundException;
import vn.thanh.metadataservice.mapper.FileMapper;
import vn.thanh.metadataservice.repository.CategoryRepo;
import vn.thanh.metadataservice.repository.FileRepo;
import vn.thanh.metadataservice.repository.specification.EntitySpecificationsBuilder;
import vn.thanh.metadataservice.repository.specification.FileSpecification;
import vn.thanh.metadataservice.repository.specification.SpecificationUtil;
import vn.thanh.metadataservice.service.IFileService;
import vn.thanh.metadataservice.service.IMetadataStorageService;
import vn.thanh.metadataservice.service.IOutboxService;
import vn.thanh.metadataservice.utils.AuthUtils;
import vn.thanh.metadataservice.utils.DocumentTypeUtil;
import vn.thanh.metadataservice.utils.FileUtil;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@Slf4j(topic = "DOCUMENT_SERVICE")
@RequiredArgsConstructor
public class FileServiceImpl implements IFileService {
    private final IMetadataStorageService metadataStorageService;
    private final FileRepo fileRepo;
    private final FileMapper fileMapper;
    private final CategoryRepo categoryRepo;
    private final CategoryValidation categoryValidation;
    private final IOutboxService outboxService;

    @Override
    public List<FileResponse> uploadFile(List<MultipartFile> files) {
        log.info("up load file empty category");
        // luu db
        List<File> documents = metadataStorageService.saveFiles(files);
        // send kafka upload document
        return fileMapper.toResponse(documents);
    }

    @Override
    public List<FileResponse> uploadFileCategory(Long categoryId, List<MultipartFile> files) {
        log.info("upload file with category id {}", categoryId);
        categoryValidation.validIsOwnerCategory(categoryId, AuthUtils.getUserId());
        List<File> documents = metadataStorageService.saveFilesCategory(files, categoryId);
        // send file to storage service
        return fileMapper.toResponse(documents);
    }

    @Override
    public FileResponse initMetadata(MetadataRequest metadataRequest) {
        log.info("init metadata");
        File file = metadataStorageService.initMetadata(metadataRequest);
        return fileMapper.toResponse(file);
    }


    @Override
    public void softDeleteFileById(Long fileId) {
        // check permission
        metadataStorageService.softDeleteFile(fileId);
    }

    private File getFileByIdOrThrow(Long docId) {
        return fileRepo.findById(docId).orElseThrow(() -> {
            log.warn("Resource not found with id {}", docId);
            return new ResourceNotFoundException("Resource not found with id " + docId);
        });
    }

    @Override
    public FileResponse copyFileById(Long fileId) {
        log.info("copy file by id: {}", fileId);
        File file = getFileByIdOrThrow(fileId);
        File copied = metadataStorageService.copyFile(file);
        FileResponse itemResponse = fileMapper.toResponse(copied);
        itemResponse.setOwnerEmail(AuthUtils.getEmail());
        itemResponse.setOwnerName(AuthUtils.getUsername());
        // send kafka
        // check permission
        return itemResponse;
    }

    @Override
    public FileResponse updateFileById(Long fileId, FileRequest fileRequest) {
        log.info("update file by id: {}", fileId);
        File fileExists = getFileByIdOrThrow(fileId);
        fileMapper.updateFile(fileExists, fileRequest);
        if (fileRequest.getCategoryId() != null) {
            Category category = categoryRepo.findById(fileRequest.getCategoryId()).orElseThrow(() -> {
                log.warn("category id: {} not found", fileRequest.getCategoryId());
                return new ResourceNotFoundException("category not found");
            });
            fileExists.setCategory(category);
        }
        fileExists = fileRepo.save(fileExists);
        return fileMapper.toResponse(fileExists);
    }

    @Override
    public FileResponse getFileById(Long id) {
        File doc = fileRepo.findById(id).orElseThrow(() -> {
            log.warn("file not foud by id : {}", id);
            return new ResourceNotFoundException("không tìm thấy tài liệu này");
        });
        return fileMapper.toResponse(doc);
    }


    @Override
    public OnlyOfficeConfig getOnlyOfficeConfig(Long documentId) {
        File file = getFileByIdOrThrow(documentId);
        boolean isEdit = false;
        if (file.getOwnerId().equals(AuthUtils.getUserId())) {
            isEdit = true;
        } else {
            return null;
        }
        // Lấy phần mở rộng file từ tên file
        String fileExtension = FileUtil.getFileExtension(file.getName());

        // Sử dụng hàm util để lấy fileType và documentType
        DocumentTypeUtil.DocumentTypeInfo documentTypeInfo = DocumentTypeUtil.getDocumentTypeInfo(fileExtension);
        // Tạo cấu hình cho OnlyOffice
        OnlyOfficeConfig config = new OnlyOfficeConfig();
        config.setFileId(file.getId());
        String documentKey = file.getId() + "-" + file.getUpdatedAt().getTime();
        config.setDocumentKey(documentKey);
        config.setDocumentTitle(file.getName());
        config.setFileType(documentTypeInfo.getFileType());
        config.setDocumentType(documentTypeInfo.getDocumentType());
//        config.setDocumentUrl(azureStorageService.getBlobUrl(document.getCurrentVersion().getBlobName())); // SAS URL để tải tài liệu
        config.setCallbackUrl("https://localhost:8080/api/v1/documents/save-editor");

        // Thông tin quyền truy cập người dùng
        OnlyOfficeConfig.Permissions permissions = new OnlyOfficeConfig.Permissions();
        permissions.setEdit(isEdit); // Quyền chỉnh sửa (có thể tùy chỉnh)
        permissions.setComment(true); // Quyền bình luận
        permissions.setDownload(false); // Quyền tải xuống

        config.setPermissions(permissions);

        // Thông tin người dùng
        OnlyOfficeConfig.User user = new OnlyOfficeConfig.User();
        user.setId(AuthUtils.getUserId()); // Lấy từ context hoặc JWT của người dùng
        user.setName(AuthUtils.getUsername());
        config.setUser(user);
        return config;
    }

    @Override
    public void hardDeleteFile(Long fileId) {
        // check permission
        metadataStorageService.hardDeleteFile(fileId);
        outboxService.addDeleteMetadataEvent(List.of(fileId));
    }

    @Override
    public void detachedCategory(Long categoryId) {
        metadataStorageService.detachedCategory(categoryId);
    }

    @Override
    public PageResponse<List<FileResponse>> getPage(Pageable pageable, String[] files) {
        log.info("get page files[]: {}", (Object) files);
        EntitySpecificationsBuilder<File> builder = new EntitySpecificationsBuilder<>();
        UUID ownerId = AuthUtils.getUserId();
        Specification<File> spec = FileSpecification.ownedBy(ownerId);
        if (files != null && files.length > 0) {
            spec = spec.and(SpecificationUtil.buildSpecificationFromFilters(files, builder));
            Page<File> pageAccessByResource = fileRepo.findAll(spec, pageable);
            return PaginationUtils.convertToPageResponse(pageAccessByResource, pageable, fileMapper::toResponse);
        }
        return PaginationUtils.convertToPageResponse(fileRepo.findAll(spec, pageable), pageable, fileMapper::toResponse);
    }

    @Override
    public void validateOwnerOfAllFile(UUID userId, List<Long> filesId) {
        long count = fileRepo.countFilesByIds(filesId);
        if (count != filesId.size()) {
            throw new ResourceNotFoundException("Có file không tồn tại");
        }
        boolean isOwnerAll = fileRepo.isOwnerOfAll(filesId, userId);
        if (!isOwnerAll) {
            throw new AccessDeniedException("Bạn không có quyền tải lên file này");
        }
    }
}

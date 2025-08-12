package vn.thanh.metadataservice.service;

import org.springframework.data.domain.Pageable;
import vn.thanh.metadataservice.dto.*;

import java.util.List;
import java.util.UUID;

public interface IFileService {
    FileResponse initMetadata(MetadataRequest metadataRequest);

    void softDeleteFileById(Long fileId); // xoa tam file

    FileResponse copyFileById(Long fileId); //tao ban sao document

    FileResponse updateFileById(Long fileId, FileRequest fileRequest); //cap nhat document

    FileResponse getFileById(Long id);

    OnlyOfficeConfig getOnlyOfficeConfig(Long documentId); // lay thong tin config onlyoffice

    void hardDeleteFile(Long fileId);

    void detachedCategory(Long categoryId);

    PageResponse<List<FileResponse>> getPage(Pageable pageable, String[] files);

    void validateOwnerOfAllFile(UUID userId, List<Long> filesId);
}

package vn.thanh.metadataservice.service;

import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import vn.thanh.metadataservice.dto.*;

import java.util.List;

public interface IFileService {
    List<FileResponse> uploadFile(List<MultipartFile> files);

    List<FileResponse> uploadFileCategory(Long categoryId, List<MultipartFile> files);

    void softDeleteFileById(Long fileId); // xoa tam file

    FileResponse copyFileById(Long fileId); //tao ban sao document

    FileResponse updateFileById(Long fileId, FileRequest fileRequest); //cap nhat document

    FileResponse getFileById(Long id);

    OnlyOfficeConfig getOnlyOfficeConfig(Long documentId); // lay thong tin config onlyoffice

    void hardDeleteFile(Long fileId);

    void detachedCategory(Long categoryId);

    PageResponse<List<FileResponse>> getPage(Pageable pageable, String[] files);
}

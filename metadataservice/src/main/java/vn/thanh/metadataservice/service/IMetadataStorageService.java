package vn.thanh.metadataservice.service;


import org.springframework.web.multipart.MultipartFile;
import vn.thanh.metadataservice.entity.File;

import java.util.List;

public interface IMetadataStorageService {

    File copyFile(File file);

    List<File> saveFilesCategory(List<MultipartFile> files, Long categoryId);


    List<File> saveFiles(List<MultipartFile> files);

    void softDeleteFile(Long fileId);

    void hardDeleteFile(Long fileId);

    void detachedCategory(Long categoryId);
}

package vn.thanh.metadataservice.service;


import org.springframework.web.multipart.MultipartFile;
import vn.thanh.metadataservice.dto.FileRequest;
import vn.thanh.metadataservice.dto.FileResponse;
import vn.thanh.metadataservice.entity.File;

import java.util.List;

public interface IMetadataMapper {
    FileResponse mapToFileResponse(File file);

    void updateFile(File fileExists, FileRequest fileRequest);

    List<FileResponse> mapToFileResponseList(List<File> files);

    void copyFile(File fileDes, File file);

    List<File> mapToListFile(List<MultipartFile> files);
}

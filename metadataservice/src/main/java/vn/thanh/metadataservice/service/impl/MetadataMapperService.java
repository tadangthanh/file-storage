package vn.thanh.metadataservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.thanh.metadataservice.dto.FileRequest;
import vn.thanh.metadataservice.dto.FileResponse;
import vn.thanh.metadataservice.entity.File;
import vn.thanh.metadataservice.mapper.FileMapper;
import vn.thanh.metadataservice.repository.SavedFileRepo;
import vn.thanh.metadataservice.service.IMetadataMapper;
import vn.thanh.metadataservice.utils.AuthUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j(topic = "DOCUMENT_MAPPER_SERVICE")
@RequiredArgsConstructor
public class MetadataMapperService implements IMetadataMapper {
    private final FileMapper fileMapper;
    private final SavedFileRepo savedFileRepo;


    @Override
    public FileResponse mapToFileResponse(File file) {
        FileResponse response = fileMapper.toResponse(file);
        response.setOwnerEmail(AuthUtils.getEmail());
        response.setOwnerName(AuthUtils.getUsername());
        response.setSaved(savedFileRepo.existsByOwnerIdAndFileId(AuthUtils.getUserId(), file.getId()));
        return response;
    }

    @Override
    public void updateFile(File fileExists, FileRequest fileRequest) {
        fileMapper.updateFile(fileExists, fileRequest);
    }

    @Override
    public List<FileResponse> mapToFileResponseList(List<File> files) {
        List<FileResponse> responses = new ArrayList<>();
        for (File doc : files) {
            responses.add(this.mapToFileResponse(doc));
        }
        return responses;
    }

    @Override
    public void copyFile(File fileDes, File file) {
        fileMapper.copyFile(fileDes, file);
    }

    @Override
    public List<File> mapToListFile(List<MultipartFile> files) {
        List<File> documents = new ArrayList<>();
        // Duyệt từng file
        for (MultipartFile f : files) {
            File file = new File();
            String name = f.getOriginalFilename();
            String type = f.getContentType();
            file.setType(type);
            file.setName(name);
            file.setSize(f.getSize());
            file.setOwnerId(AuthUtils.getUserId());
            documents.add(file);
        }
        return documents;
    }

}

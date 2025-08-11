package vn.thanh.metadataservice.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.thanh.metadataservice.dto.*;
import vn.thanh.metadataservice.service.IFileService;
import vn.thanh.metadataservice.validation.ValidFiles;

import java.util.List;
import java.util.UUID;


@RequiredArgsConstructor
@RequestMapping("/api/v1/files")
@RestController
@Validated
@Slf4j(topic = "FILE_REST")
public class FileRest {
    private final IFileService fileService;

    @PostMapping
    public ResponseData<List<FileResponse>> uploadFileEmptyCategory(@ValidFiles @RequestPart("files") MultipartFile[] files) {
        List<FileResponse> result = fileService.uploadFile(List.of(files));
        return new ResponseData<>(201, "Tải lên thành công", result);
    }

    @PostMapping("/category/{categoryId}")
    public ResponseData<List<FileResponse>> uploadFileCategory(@PathVariable Long categoryId, @ValidFiles @RequestPart("files") MultipartFile[] files) {
        List<FileResponse> result = fileService.uploadFileCategory(categoryId, List.of(files));
        return new ResponseData<>(201, "Tải lên thành công", result);
    }


    @DeleteMapping("/{fileId}/hard")
    public ResponseData<Void> hardDelete(@PathVariable Long fileId) {
        fileService.hardDeleteFile(fileId);
        return new ResponseData<>(204, "Xóa thành công", null);
    }

    @DeleteMapping("/{fileId}/soft")
    public ResponseData<Void> softDelete(@PathVariable Long fileId) {
        fileService.softDeleteFileById(fileId);
        return new ResponseData<>(204, "Xóa thành công", null);
    }

    @PostMapping("/{fileId}/copy")
    public ResponseData<FileResponse> copy(@PathVariable Long fileId) {
        return new ResponseData<>(201, "Thành công", fileService.copyFileById(fileId));
    }

    @PutMapping("/{fileId}")
    public ResponseData<FileResponse> update(@PathVariable Long fileId, @Valid @RequestBody FileRequest fileRequest) {
        return new ResponseData<>(200, "Thành công", fileService.updateFileById(fileId, fileRequest));
    }


    @GetMapping("/{fileId}")
    public ResponseData<FileResponse> getFileById(@PathVariable Long fileId) {
        return new ResponseData<>(200, "Thành công", fileService.getFileById(fileId));
    }


    @GetMapping("/{fileId}/onlyoffice-config")
    public ResponseData<OnlyOfficeConfig> getOnlyOfficeConfig(@PathVariable Long fileId) {
        return new ResponseData<>(200, "Thành công", fileService.getOnlyOfficeConfig(fileId));
    }

    @GetMapping("/search")
    public ResponseData<PageResponse<List<FileResponse>>> search(Pageable pageable, @RequestParam(required = false, value = "files") String[] files) {
        return new ResponseData<>(200, "thành công", fileService.getPage(pageable, files));
    }

    @PostMapping("/init")
    public ResponseData<FileResponse> initFile(@RequestBody MetadataRequest metadataRequest) {
        return new ResponseData<>(201, "Tải lên thành công", fileService.initMetadata(metadataRequest));
    }

    @PostMapping("/is-owner-all-file/{userId}")
    public ResponseData<Void> isOwnerAllFile(
            @PathVariable UUID userId,
            @RequestBody List<Long> fileIds) {
        fileService.validateOwnerOfAllFile(userId, fileIds);
        return new ResponseData<>(200, "OK");
    }
}

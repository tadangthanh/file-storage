package vn.thanh.metadataservice.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vn.thanh.metadataservice.annotation.RequirePermission;
import vn.thanh.metadataservice.dto.*;
import vn.thanh.metadataservice.service.IFileService;

import java.util.List;
import java.util.UUID;


@RequiredArgsConstructor
@RequestMapping("/api/v1/files")
@RestController
@Validated
@Slf4j(topic = "FILE_REST")
public class FileRest {
    private final IFileService fileService;

    @PostMapping("/init")
    public ResponseData<FileResponse> intMetadata(@RequestBody MetadataRequest metadataRequest) {
        return new ResponseData<>(201, "Thành công", fileService.initMetadata(metadataRequest));
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

    @RequirePermission(resourceType = ResourceType.DOCUMENT,
            resourceParam = "fileId", // trùng với @PathVariable Long fileId
            permissionBit = Perms.READ)
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

    @PostMapping("/is-owner-all-file/{userId}")
    public ResponseData<Void> isOwnerAllFile(
            @PathVariable UUID userId,
            @RequestBody List<Long> fileIds) {
        fileService.validateOwnerOfAllFile(userId, fileIds);
        return new ResponseData<>(200, "OK");
    }
    @GetMapping("/{userId}/is-owner/{docId}")
    public ResponseData<Boolean> checkUserIsOwner(@PathVariable UUID userId, @PathVariable Long docId) {
        return new ResponseData<>(200, "thành công", fileService.checkUserIsOwner(userId, docId));
    }
}

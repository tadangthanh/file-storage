package vn.thanh.storageservice.rest;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import vn.thanh.storageservice.dto.ResponseData;
import vn.thanh.storageservice.dto.UploadSignRequest;
import vn.thanh.storageservice.dto.UploadUrlResponse;
import vn.thanh.storageservice.entity.Version;
import vn.thanh.storageservice.service.IAzureStorageService;
import vn.thanh.storageservice.service.IVersionService;
import vn.thanh.storageservice.service.impl.AuthenticationService;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/v1/versions")
@RequiredArgsConstructor
public class VersionRest {
    private final IVersionService versionService;
    private final AuthenticationService authenticationService;
    @PostMapping("/presign-url")
    ResponseData<List<UploadUrlResponse>> presignUrl(@RequestBody List<UploadSignRequest> uploadSignRequest) {
        return new ResponseData<>(200, "ok", versionService.presignUpload(uploadSignRequest));
    }

    @GetMapping("/{metadataId}/view")
    public ResponseEntity<InputStreamResource> viewDoc(@PathVariable Long metadataId) {
        Version ver = versionService.getVersionMaxByMetadataId(metadataId);
        // 2. Lấy dữ liệu từ Azure Blob
        InputStream inputStream = versionService.downloadMaxVersionByMetadataId(metadataId);

        // 3. Trả về stream cho OnlyOffice
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM).header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + ver.getBlobName() + "\"").body(new InputStreamResource(inputStream));
    }
    @GetMapping("/{metadataId}/download/{accessToken}")
    public void downloadDoc(@PathVariable Long metadataId, @PathVariable String accessToken, HttpServletResponse response) throws IOException {
        SecurityContextHolder.getContext().setAuthentication(authenticationService.authenticateToken(accessToken));
        Version version = versionService.getVersionMaxByMetadataId(metadataId);
        try (InputStream inputStream = versionService.downloadMaxVersionByMetadataId(metadataId)) {
            String fileName = version.getBlobName().split("/")[version.getBlobName().split("/").length-1];
            String mimeType = URLConnection.guessContentTypeFromName(fileName);
            if (mimeType == null) {
                mimeType = "application/octet-stream"; // fallback
            }

            response.setContentType(mimeType);
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                response.getOutputStream().write(buffer, 0, bytesRead);
            }
            response.flushBuffer();
        }
    }

}

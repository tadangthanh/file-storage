package vn.thanh.storageservice.rest;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import vn.thanh.storageservice.dto.ResponseData;
import vn.thanh.storageservice.dto.VersionInitRequest;
import vn.thanh.storageservice.service.IAzureStorageService;
import vn.thanh.storageservice.service.IVersionService;
import org.apache.tika.Tika;
import org.apache.tika.mime.MimeTypes;
import org.apache.tika.mime.MimeTypeException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/v1/versions")
@RequiredArgsConstructor
public class VersionRest {
    private final IVersionService versionService;
    private final IAzureStorageService azureStorageService;
    @PostMapping("/init")
    ResponseData<String> initVersion(@RequestBody VersionInitRequest versionInitRequest){
        return new ResponseData<>(200,"ok", versionService.initVersion(versionInitRequest));
    }
    @GetMapping("/view")
    public void downloadDocAsPdf(@RequestParam String blobName,@RequestParam String contentType,HttpServletResponse response) throws IOException {

        try (InputStream inputStream = azureStorageService.downloadBlobInputStream(blobName)) {
            response.setContentType(contentType);
            // Lấy đuôi mở rộng dựa trên contentType
            String fileExtension = getExtensionFromContentType(contentType);
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" +blobName+fileExtension);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                response.getOutputStream().write(buffer, 0, bytesRead);
            }
            response.flushBuffer();
        }
    }

    public String getExtensionFromContentType(String contentType) {
        try {
            MimeTypes allTypes = MimeTypes.getDefaultMimeTypes();
            System.out.println("file extendsion: "+allTypes.forName(contentType).getExtension());
            return allTypes.forName(contentType).getExtension();  // trả về đuôi có dấu chấm
        } catch (MimeTypeException e) {
            // Nếu không tìm được thì trả về rỗng
            return "";
        }
    }

}

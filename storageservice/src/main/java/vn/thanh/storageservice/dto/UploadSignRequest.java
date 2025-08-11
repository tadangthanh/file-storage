package vn.thanh.storageservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UploadSignRequest {
    private Long metadataId;       // ID metadata đã tạo trước đó
    private String originalFilename; // Tên file gốc (client gửi lên)
    private String contentType;      // MIME type tạm (pdf, txt, ...)
    private Long size;               // Kích thước file (byte)
}

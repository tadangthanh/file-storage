package vn.thanh.storageservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UploadUrlResponse {
    private String originalName;
    private String blobName;
    private String uploadUrl;
}

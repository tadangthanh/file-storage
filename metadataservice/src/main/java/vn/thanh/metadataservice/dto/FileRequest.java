package vn.thanh.metadataservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileRequest {
    private String name;
    private Long categoryId;
}

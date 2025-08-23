package vn.thanh.metadataservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MetadataRequest {
    private Long size;
    private String name;
    private Long categoryId;
}

package vn.thanh.metadataservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MetadataRequest {
    private Long size;
    private String type;
    private String name;
    private Long categoryId;
}

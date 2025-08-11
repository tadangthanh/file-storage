package vn.thanh.metadataservice.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class MetadataUpdate {
    private Long id;
    private Long currentVersionId;
    private Long size;
    private String type;
    private String name;
}

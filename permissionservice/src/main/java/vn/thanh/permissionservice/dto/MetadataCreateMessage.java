package vn.thanh.permissionservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MetadataCreateMessage {
    private Long metadataId;
    private Long categoryId;
}

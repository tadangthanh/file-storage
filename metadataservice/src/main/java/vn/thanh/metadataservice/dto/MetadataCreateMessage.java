package vn.thanh.metadataservice.dto;

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

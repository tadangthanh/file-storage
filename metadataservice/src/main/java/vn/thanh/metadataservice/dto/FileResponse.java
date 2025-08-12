package vn.thanh.metadataservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class FileResponse extends BaseDto {
    private String type;
    private String name;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long categoryId;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String ownerName;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String ownerEmail;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LocalDateTime deletedAt;
    private Long size;
    private boolean saved;
}

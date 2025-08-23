package vn.thanh.textextractionservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Getter
@Setter
public class DocumentReadyForExtraction {
    private Long documentId;
    private Long categoryId;
    private int version;
    private String fileUrl;
    private String type;
    private String createdBy;
    private UUID ownerId;
    private Date createdAt;
    private String blobName;
}

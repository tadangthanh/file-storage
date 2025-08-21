package vn.thanh.storageservice.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@ToString
public class DocumentReady {
    private Long documentId;
    private Long currentVersionId;
    private Long size;
    private String type;
    private int version;
    private String name;
    private String fileUrl;
    private String createdBy;
    private UUID ownerId;
    private Date createdAt;
    private String blobName;
}

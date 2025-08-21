package vn.thanh.textextractionservice.dto;

import lombok.*;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentIndexMessage {
    private Long documentId;
    private Integer version;
    private Integer chunkIndex;
    private String text;
    private UUID ownerId;
    private String visibility; // private | public | shared
    private Long categoryId;
    private List<Long> allowedUserIds;
    private List<Long> allowedGroupIds;
    private String blobName;
    private Date createdAt;
}
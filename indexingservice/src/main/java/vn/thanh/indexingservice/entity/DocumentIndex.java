package vn.thanh.indexingservice.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Document(indexName = "documents")
public class DocumentIndex {
    @Id
    private String id; // id mới cho Elasticsearch
    private Long documentId;

    private Integer version;
    private String createdBy;

    private Integer chunkIndex;

    @Field(type = FieldType.Text)
    private String text;

    private UUID ownerId;

    private String visibility; // private | public | shared

    private Long categoryId;

    @Field(type = FieldType.Keyword)
    private List<Long> allowedUserIds;

    @Field(type = FieldType.Keyword)
    private List<Long> allowedGroupIds;

    private Date createdAt;

}

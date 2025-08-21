package vn.thanh.storageservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "versions")
@Getter
@Setter
public class Version extends BaseEntity {
    private Long metadataId;
    private Integer versionNumber;
    private String blobName;
    private String contentType;
    @Enumerated(value = EnumType.STRING)
    private VersionStatus status;
    private Long size;
    private UUID ownerId;
}

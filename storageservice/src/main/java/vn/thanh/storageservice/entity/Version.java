package vn.thanh.storageservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "versions")
@Getter
@Setter
public class Version extends BaseEntity {
    private Long metadataId;
    private Integer versionNumber;
    private String blobName;
    private Long size;
}

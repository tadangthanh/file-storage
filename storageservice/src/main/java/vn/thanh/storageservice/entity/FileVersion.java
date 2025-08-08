package vn.thanh.storageservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "files_version")
@Getter
@Setter
public class FileVersion extends BaseEntity {
    private Long fileId;
    private Integer version;
    private String blobName;
    private Long size;
}

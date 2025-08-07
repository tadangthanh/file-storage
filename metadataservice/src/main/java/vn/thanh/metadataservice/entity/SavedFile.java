package vn.thanh.metadataservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "saved_file", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"owner_id", "file_id"})
})
public class SavedFile extends BaseEntity {
    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;
    @ManyToOne
    @JoinColumn(name = "file_id", nullable = false)
    private File file;
}

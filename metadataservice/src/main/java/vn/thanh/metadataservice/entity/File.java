package vn.thanh.metadataservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "file")
public class File extends BaseEntity {
    private String type;
    private Long size;
    private String name;
    @Column(name = "owner_id")
    private UUID ownerId;
    private LocalDateTime deletedAt;
    @Column(name = "permanent_delete_at")
    private LocalDateTime permanentDeleteAt; // thoi gian xoa vinh vien
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
}

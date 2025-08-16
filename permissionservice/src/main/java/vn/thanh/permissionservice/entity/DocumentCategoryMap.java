package vn.thanh.permissionservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "document_category_map", uniqueConstraints = @UniqueConstraint(columnNames = {"document_id", "category_id"}))
@AllArgsConstructor
@NoArgsConstructor
public class DocumentCategoryMap extends BaseEntity {
    @Column(name = "document_id", nullable = false)
    private Long documentId;
    @Column(name = "category_id", nullable = false)
    private Long categoryId;
}

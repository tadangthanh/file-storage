package vn.thanh.metadataservice.repository.specification;

import org.springframework.data.jpa.domain.Specification;
import vn.thanh.metadataservice.entity.File;

import java.util.UUID;

public class FileSpecification {
    public static Specification<File> ownedBy(UUID userId) {
        return (root, query, cb) -> cb.equal(root.get("ownerId"), userId);
    }
}

package vn.thanh.metadataservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.thanh.metadataservice.entity.SavedFile;

import java.util.UUID;

@Repository
public interface SavedFileRepo extends JpaRepository<SavedFile, Long> {
    boolean existsByOwnerIdAndFileId(UUID ownerId, Long fileId);
}

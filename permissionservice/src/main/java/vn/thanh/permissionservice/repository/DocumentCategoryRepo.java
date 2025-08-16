package vn.thanh.permissionservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import vn.thanh.permissionservice.entity.DocumentCategoryMap;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentCategoryRepo extends JpaRepository<DocumentCategoryMap, Long> {
    @Modifying
    @Transactional
    void deleteAllByDocumentIdIn(List<Long> documentIds);
    // Tìm categoryId theo documentId
    @Query("select d.categoryId from DocumentCategoryMap d where d.documentId = :documentId")
    Optional<Long> findCategoryIdByDocumentId(@Param("documentId") Long documentId);
}

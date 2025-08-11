package vn.thanh.metadataservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.thanh.metadataservice.entity.File;

import java.util.List;
import java.util.UUID;

@Repository
public interface FileRepo extends JpaRepository<File, Long>, JpaSpecificationExecutor<File> {
    @Query("select f from File f where f.category.id = ?1")
    List<File> getFilesByCategoryId(Long categoryId);

    @Query("""
                SELECT CASE WHEN COUNT(f) = 0 THEN true ELSE false END
                FROM File f
                WHERE f.id IN :fileIds AND f.ownerId <> :userId
            """)
    boolean isOwnerOfAll(@Param("fileIds") List<Long> fileIds,
                         @Param("userId") UUID userId);

    @Query("SELECT COUNT(f) FROM File f WHERE f.id IN :fileIds")
    long countFilesByIds(@Param("fileIds") List<Long> fileIds);
}

package vn.thanh.metadataservice.repository;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.thanh.metadataservice.entity.File;
import vn.thanh.metadataservice.entity.Status;

import java.time.Duration;
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

    @Modifying
    @Query("UPDATE File f SET f.status = :status WHERE f.id IN :ids")
    int updateStatusByIds(@Param("ids") List<Long> ids, @Param("status") Status status);

    @Query(value = """
    SELECT *
    FROM file f
    WHERE f.status = :status
      AND (
        f.last_retry_time IS NULL
        OR f.last_retry_time + INTERVAL :waitMinutes MINUTE <= NOW()
      )
    FOR UPDATE SKIP LOCKED
    """, nativeQuery = true)
    List<File> findReadyForRetry(@Param("status") Status status,
                                 @Param("waitMinutes") long waitMinutes,
                                 Pageable pageable);

}

package vn.thanh.storageservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.thanh.storageservice.entity.OutboxEvent;

import java.util.Date;
import java.util.List;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {
    @Query(value = """
            SELECT *
            FROM outbox_event
            WHERE (status = 'PENDING' OR (status = 'FAILED' AND retry_count < :maxRetry))
            ORDER BY created_at
            LIMIT :batchSize
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<OutboxEvent> findEventsToSend(@Param("maxRetry") int maxRetry,
                                       @Param("batchSize") int batchSize);

    @Modifying
    @Query("""
                DELETE FROM OutboxEvent e
                WHERE e.status = 'SUCCESS'
                AND e.createdAt < :threshold
            """)
    int deleteOldSuccessEvents(@Param("threshold") Date threshold);

}

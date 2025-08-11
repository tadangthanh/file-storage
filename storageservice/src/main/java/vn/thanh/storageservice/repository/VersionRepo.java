package vn.thanh.storageservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.thanh.storageservice.entity.Version;
import vn.thanh.storageservice.entity.VersionStatus;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface VersionRepo extends JpaRepository<Version, Long> {

    @Query("SELECT v FROM Version v " +
           "WHERE v.status = :status " +
           "AND v.createdAt < :cutoffTime")
    List<Version> findUploadingVersionsBefore(
            @Param("status") VersionStatus status,
            @Param("cutoffTime") Date cutoffTime
    );
}

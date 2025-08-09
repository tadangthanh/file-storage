package vn.thanh.storageservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.thanh.storageservice.entity.Version;

@Repository
public interface VersionRepo extends JpaRepository<Version, Long> {
}

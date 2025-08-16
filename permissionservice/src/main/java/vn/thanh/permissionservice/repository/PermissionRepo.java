package vn.thanh.permissionservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import vn.thanh.permissionservice.entity.Permission;
import vn.thanh.permissionservice.entity.ResourceType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PermissionRepo extends JpaRepository<Permission, Long> {
    Optional<Permission> findByUserIdAndResourceTypeAndResourceId(UUID userId, ResourceType resourceType, Long resourceId);

    Optional<Permission> findByUserIdAndRResourceId(UUID userId, Long resourceId);

    @Modifying
    @Transactional
    void deleteAllByResourceIdInAndResourceType(List<Long> metadataIds, ResourceType resourceType);
}

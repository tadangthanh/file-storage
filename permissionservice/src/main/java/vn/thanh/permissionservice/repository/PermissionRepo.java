package vn.thanh.permissionservice.repository;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.thanh.permissionservice.entity.Permission;
import vn.thanh.permissionservice.entity.ResourceType;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PermissionRepo extends JpaRepository<Permission, Long> {
    Optional<Permission> findByUserIdAndResourceTypeAndResourceId(UUID userId, ResourceType resourceType,Long resourceId);
}

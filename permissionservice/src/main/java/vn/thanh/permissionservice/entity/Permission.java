package vn.thanh.permissionservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static vn.thanh.permissionservice.entity.Perms.*;

@Entity
@Table(name = "permission", uniqueConstraints = @UniqueConstraint(columnNames = {"resource_type", "resource_id", "user_id"}))
@Getter
@Setter
public class Permission extends BaseEntity {
    @Column(name = "resource_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ResourceType resourceType;
    @Column(name = "resource_id", nullable = false)
    private Long resourceId;
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    private int allowMask; // mặc định 0

    public List<String> toList(int mask) {
        List<String> result = new ArrayList<>();
        if ((mask & READ) != 0) result.add("READ");
        if ((mask & WRITE) != 0) result.add("WRITE");
        if ((mask & DELETE) != 0) result.add("DELETE");
        if ((mask & SHARE) != 0) result.add("SHARE");
        return result;
    }

    public void add(int p) {
        this.allowMask = this.allowMask | p;
    }

    public void remove(int p) {
        this.allowMask = this.allowMask & ~p;
    }
}

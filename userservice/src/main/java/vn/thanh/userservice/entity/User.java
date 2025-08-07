package vn.thanh.userservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "user")
public class User extends BaseEntity {
    @Column(unique = true, nullable = false)
    private String email;
    @Column(columnDefinition = "TEXT")
    private String password;
    @Column(nullable = false)
    private String fullName;
    private String avatarUrl;
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private UserStatus status;
}

package vn.thanh.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.thanh.userservice.entity.User;

import java.util.Optional;

public interface UserRepo extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}

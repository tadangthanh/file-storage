package vn.thanh.userservice.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.thanh.userservice.entity.User;
import vn.thanh.userservice.exception.ResourceNotFoundException;
import vn.thanh.userservice.repository.UserRepo;
import vn.thanh.userservice.service.IUserService;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements IUserService {
    private final UserRepo userRepo;

    @Override
    public User getUserByEmail(String email) {
        return userRepo.findByEmail(email).orElseThrow(() -> {
            log.warn("User not found by email: {}", email);
            return new ResourceNotFoundException("Không tìm thấy user {}" + email);
        });
    }

    @Override
    public User getUserById(Long id) {
        return userRepo.findById(id).orElseThrow(() -> {
            log.warn("User not found by id: {}", id);
            return new ResourceNotFoundException("Không tìm thấy user {}" + id);
        });
    }

}

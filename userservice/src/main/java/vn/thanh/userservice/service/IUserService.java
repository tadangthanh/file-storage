package vn.thanh.userservice.service;

import vn.thanh.userservice.entity.User;

public interface IUserService {

    User getUserByEmail(String email);

    User getUserById(Long id);

}

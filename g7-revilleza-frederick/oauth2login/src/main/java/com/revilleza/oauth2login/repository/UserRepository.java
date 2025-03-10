package com.revilleza.oauth2login.repository;

import com.revilleza.oauth2login.model.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Integer> {
    public User findByEmail(String email);
}

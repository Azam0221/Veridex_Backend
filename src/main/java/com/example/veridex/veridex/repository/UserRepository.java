package com.example.veridex.veridex.repository;

import com.example.veridex.veridex.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface UserRepository extends JpaRepository<User,Long> {

    User findByEmail(String username);
    boolean existsByEmail(String email);

}

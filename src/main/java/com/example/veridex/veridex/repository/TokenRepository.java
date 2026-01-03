package com.example.veridex.veridex.repository;

import com.example.veridex.veridex.model.Token;
import com.example.veridex.veridex.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TokenRepository extends JpaRepository<Token,Long> {

    List<Token> findAllValidTokensByUser(User user);
}

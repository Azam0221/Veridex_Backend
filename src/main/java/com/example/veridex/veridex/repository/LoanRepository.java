package com.example.veridex.veridex.repository;

import com.example.veridex.veridex.model.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan,Long> {

    List<Loan> findByAgent_Email(String email);


    List<Loan> findByBorrower_Email(String email);
}

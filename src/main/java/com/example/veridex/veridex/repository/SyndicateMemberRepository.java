package com.example.veridex.veridex.repository;

import com.example.veridex.veridex.model.SyndicateMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface SyndicateMemberRepository extends JpaRepository<SyndicateMember, Long> {

    List<SyndicateMember> findByLoanId(Long loanId);

    List<SyndicateMember> findByLender_Email(String email);
}

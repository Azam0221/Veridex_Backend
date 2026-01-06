package com.example.veridex.veridex.repository;


import com.example.veridex.veridex.model.KPI;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KpiRepository extends JpaRepository<KPI,Long> {

    List<KPI> findByLoanId(Long loanId);
}

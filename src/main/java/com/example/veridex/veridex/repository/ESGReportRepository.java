package com.example.veridex.veridex.repository;

import com.example.veridex.veridex.model.ESGReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ESGReportRepository extends JpaRepository<ESGReport, Long> {

    List<ESGReport> findByLoanId(Long loanId);

    Optional<ESGReport> findTopByLoanIdOrderByUploadTimestampDesc(Long loanId);


}

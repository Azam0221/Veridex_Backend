package com.example.veridex.veridex.repository;

import com.example.veridex.veridex.model.VerificationReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VerificationRepository extends JpaRepository<VerificationReport, Long> {

    VerificationReport findByEsgReportId(Long esgReportId);

    Optional<VerificationReport> findByEsgReport_Id(Long esgReportId);

    List<VerificationReport> findByEsgReport_Loan_IdOrderByVerifiedAtDesc(Long loanId);

}

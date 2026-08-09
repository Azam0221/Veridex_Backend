package com.example.veridex.veridex.service;


import com.example.veridex.veridex.enum_.OptimizationType;
import com.example.veridex.veridex.enum_.Role;
import com.example.veridex.veridex.enum_.Status;
import com.example.veridex.veridex.model.*;
import com.example.veridex.veridex.repository.KpiRepository;
import com.example.veridex.veridex.repository.LoanRepository;
import com.example.veridex.veridex.repository.SyndicateMemberRepository;
import com.example.veridex.veridex.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanRepository loanRepository;
    private final KpiRepository kpiRepository;
    private final SyndicateMemberRepository syndicateMemberRepository;
    private final UserRepository userRepository;

    @Transactional
    public Loan createLoan(LoanRequest request, String agentEmail){

        User agentUser = userRepository.findByEmail(agentEmail);

        if (agentUser == null) {
            throw new IllegalArgumentException("Agent user not found.");
        }

        User borrowerUser = userRepository.findByEmail(request.getBorrowerEmail());

        if (borrowerUser == null) {
            throw new IllegalArgumentException("Borrower user not found.");
        }

        Loan loan = new Loan();
        loan.setAgent(agentUser);
        loan.setBorrower(borrowerUser);
        loan.setBorrowerName(request.getBorrowerName());
        loan.setPrincipalAmount(request.getAmount());
        loan.setBaseMargin(request.getBaseMargin());
        loan.setTenorYears(request.getTenorYears());
        loan.setCurrentMargin(request.getBaseMargin());

        loan.setNextReportingDate(LocalDate.now().plusYears(1));
        loan.setMaturityDate(LocalDate.now().plusYears(request.getTenorYears()));

        loan.setStatus(Status.ACTIVE);

        Loan savedLoan = loanRepository.save(loan);

        SyndicateMember agentMember = new SyndicateMember();
        agentMember.setLoan(savedLoan);

        agentMember.setLender(agentUser);

        agentMember.setBankName(agentUser.getName());
        agentMember.setParticipationAmount(request.getAmount());
        agentMember.setRole(Role.AGENT);

        syndicateMemberRepository.save(agentMember);

        if (request.getKpis() != null) {
            for (KPIRequest kpiDto : request.getKpis()) {
                KPI kpi = new KPI();
                kpi.setName(kpiDto.getName());
                kpi.setKpiType(kpiDto.getKpiType());
                kpi.setUnit(kpiDto.getUnit());
                kpi.setBaselineValue(kpiDto.getBaseline());
                kpi.setTargetValue(kpiDto.getTarget());
                kpi.setOptimizationType(OptimizationType.valueOf(kpiDto.getOptimizationType()));
                kpi.setMarginAdjustment(kpiDto.getMarginDelta());

                kpi.setLoan(savedLoan);

                kpiRepository.save(kpi);
            }
        }
        return savedLoan;

    }


    public AgentDashboardStatsDTO getAgentStats(String agentEmail) {
        List<Loan> loans = loanRepository.findByAgent_Email(agentEmail);

        long totalActive = loans.size();
        long pending = loans.stream()
                .filter(l -> l.getStatus() == Status.PENDING || l.getStatus() == Status.REQUIRES_REVIEW)
                .count();


        BigDecimal totalExposure = loans.stream()
                .map(Loan::getPrincipalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        double totalSavingsBps = loans.stream()
                .mapToDouble(l ->
                        l.getBaseMargin().subtract(l.getCurrentMargin()).doubleValue() * 100
                ).sum();

        int avgSavings = totalActive > 0 ? (int) (totalSavingsBps / totalActive) : 0;

        return AgentDashboardStatsDTO.builder()
                .totalActiveLoans(totalActive)
                .pendingVerification(pending)
                .totalExposure(totalExposure)
                .avgMarginSavingsBps(avgSavings)
                .build();
    }

    public List<Loan> getLoansManagedBy(String agentEmail) {
        return loanRepository.findByAgent_Email(agentEmail);
    }

    public List<Loan> getLoansOwnedBy(String borrowerEmail) {
        return loanRepository.findByBorrower_Email(borrowerEmail);
    }

    public Loan getLoanById(Long id) {
        return loanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Loan not found with ID: " + id));
    }
}

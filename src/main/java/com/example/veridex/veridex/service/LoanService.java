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
    public ResponseEntity<Loan> createLoan(LoanRequest request, String agentEmail){

        User agentUser = userRepository.findByEmail(agentEmail);

        if(agentUser == null){
            return ResponseEntity.badRequest().body(null);
        }

        User borrowerUser = userRepository.findByEmail(request.getBorrowerEmail());

        if(borrowerUser == null){
            return ResponseEntity.badRequest().body(null);
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
        return ResponseEntity.ok(savedLoan);

    }

    public AgentDashboardStatsDTO getAgentStats(String agentEmail) {
        List<Loan> loans = loanRepository.findByAgent_Email(agentEmail);

        long activeCount = loans.size();

        long pendingCount = loans.stream()
                .filter(l -> LocalDate.now().plusDays(30).isAfter(l.getNextReportingDate()))
                .count();

        BigDecimal totalExposure = loans.stream()
                .map(Loan::getPrincipalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        double totalSavings = loans.stream()
                .mapToDouble(l -> l.getBaseMargin().subtract(l.getCurrentMargin()).doubleValue())
                .sum();

        int avgBps = 0;
        if (activeCount > 0) {
            double avgSavingsPercent = totalSavings / activeCount;
            avgBps = (int) (avgSavingsPercent * 100);
        }

        return AgentDashboardStatsDTO.builder()
                .totalActiveLoans(activeCount)
                .pendingVerification(pendingCount)
                .totalExposure(totalExposure)
                .avgMarginSavingsBps(avgBps)
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

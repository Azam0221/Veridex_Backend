package com.example.veridex.veridex.service;


import com.example.veridex.veridex.enum_.OptimizationType;
import com.example.veridex.veridex.enum_.Role;
import com.example.veridex.veridex.enum_.Status;
import com.example.veridex.veridex.model.*;
import com.example.veridex.veridex.repository.KpiRepository;
import com.example.veridex.veridex.repository.LoanRepository;
import com.example.veridex.veridex.repository.SyndicateMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanRepository loanRepository;
    private final KpiRepository kpiRepository;
    private final SyndicateMemberRepository syndicateMemberRepository; //

    @Transactional
    public Loan createLoan(LoanRequest request){

        Loan loan = new Loan();
        loan.setBorrowerName(request.getBorrowerName());
        loan.setPrincipalAmount(request.getAmount());
        loan.setBaseMargin(request.getBaseMargin());
        loan.setTenorYears(request.getTenorYears());
        loan.setCurrentMargin(request.getBaseMargin());

        loan.setStatus(Status.ACTIVE);

        Loan savedLoan = loanRepository.save(loan);

        SyndicateMember agent = new SyndicateMember();
        agent.setLoan(savedLoan);

        agent.setBankName(request.getBorrowerName() != null ? request.getBorrowerName(): "Agent Bank");
        agent.setParticipationAmount(request.getAmount());
        agent.setRole(Role.AGENT);

        syndicateMemberRepository.save(agent);

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

    public List<Loan> getAllLoans() {
        return loanRepository.findAll();
    }

    public Loan getLoanById(Long id) {
        return loanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Loan not found with ID: " + id));
    }
}

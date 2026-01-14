package com.example.veridex.veridex.service;


import com.example.veridex.veridex.enum_.Role;
import com.example.veridex.veridex.model.*;
import com.example.veridex.veridex.repository.LoanRepository;
import com.example.veridex.veridex.repository.SyndicateMemberRepository;
import com.example.veridex.veridex.repository.UserRepository;
import com.example.veridex.veridex.repository.VerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SyndicateService {


    private final SyndicateMemberRepository syndicateMemberRepository;
    private final LoanRepository loanRepository;
    private final VerificationRepository verificationRepository;
    private final UserRepository userRepository;

    @Transactional
    public ResponseEntity<SyndicateMember> addMember(Long loanId , SyndicateRequest request){


        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        User lenderUser = userRepository.findByEmail(request.getLenderEmail());

        if(lenderUser == null){
            return ResponseEntity.badRequest().body(null);
        }

        SyndicateMember agentMember = syndicateMemberRepository.findByLoanId(loanId).stream()
                .filter(m -> m.getRole() == Role.AGENT)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Agent not found for this loan (Data Integrity Error)"));

        BigDecimal newInvestment = request.getParticipationAmount();

        if (agentMember.getParticipationAmount().compareTo(newInvestment) < 0) {
            throw new RuntimeException("Agent does not have enough participation amount to sell this portion.");
        }

        BigDecimal newAgentAmount = agentMember.getParticipationAmount().subtract(newInvestment);
        agentMember.setParticipationAmount(newAgentAmount);
        syndicateMemberRepository.save(agentMember);

        SyndicateMember member = new SyndicateMember();
        member.setLoan(loan);
        member.setLender(lenderUser);

        String name = (request.getBankName() != null && !request.getBankName().isEmpty())
                ? request.getBankName()
                : lenderUser.getOrganizationName();
        member.setBankName(name);

        member.setParticipationAmount(newInvestment);
        member.setRole(Role.valueOf(request.getRole().toUpperCase()));

        return ResponseEntity.ok(syndicateMemberRepository.save(member));
    }

    @Transactional(readOnly = true)
    public List<SyndicateDashboardDTO> getLenderPortfolio(String lenderEmail) {

        List<SyndicateMember> myInvestments = syndicateMemberRepository.findByLender_Email(lenderEmail);

        return myInvestments.stream().map(this::calculateStats).collect(Collectors.toList());
    }


    private SyndicateDashboardDTO calculateStats(SyndicateMember member) {

        Loan loan = member.getLoan();


        BigDecimal marginDiff = loan.getBaseMargin().subtract(loan.getCurrentMargin()).abs();
        BigDecimal annualSavings = loan.getPrincipalAmount()
                .multiply(marginDiff)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal totalSavings = annualSavings.multiply(BigDecimal.valueOf(loan.getTenorYears()));

        BigDecimal shareRatio = BigDecimal.ZERO;
        if (loan.getPrincipalAmount().compareTo(BigDecimal.ZERO) > 0) {
            shareRatio = member.getParticipationAmount()
                    .divide(loan.getPrincipalAmount(), 4, RoundingMode.HALF_UP);
        }
        BigDecimal mySavings = totalSavings.multiply(shareRatio);


        List<SyndicateDashboardDTO.BenchmarkMetric> benchmarkList = loan.getKpi().stream().map(kpi -> {

            double borrowerVal = kpi.getBaselineValue() != null ? kpi.getBaselineValue() : 0.0;
            double industryVal;
            boolean isBetter = true;
            industryVal = borrowerVal * 1.2;
            double diff = 0.0;
            if (industryVal > 0) {
                diff = Math.abs((industryVal - borrowerVal) / industryVal) * 100.0;
            }

            return SyndicateDashboardDTO.BenchmarkMetric.builder()
                    .kpiName(kpi.getName())
                    .unit(kpi.getUnit())
                    .borrowerValue(Math.round(borrowerVal * 100.0) / 100.0)
                    .industryAvg(Math.round(industryVal * 100.0) / 100.0)
                    .isBetter(true)
                    .percentageDiff(Math.round(diff * 10.0) / 10.0)
                    .build();
        }).collect(Collectors.toList());



        return SyndicateDashboardDTO.builder()
                .loanId(loan.getId())
                .bankName(member.getBankName())
                .investmentAmount(member.getParticipationAmount())
                .ownershipSharePercentage(shareRatio.doubleValue() * 100.0)
                .totalLoanSavings(totalSavings)
                .myShareOfSavings(mySavings)
                .baseMargin(loan.getBaseMargin())
                .currentMargin(loan.getCurrentMargin())
                .benchmarks(benchmarkList)
                .build();
    }

    @Transactional(readOnly = true)
    public List<VerificationReport> getAuditTrail(Long loanId) {

        return verificationRepository.findByEsgReport_Loan_IdOrderByVerifiedAtDesc(loanId);
    }

    @Transactional(readOnly = true)
    public SyndicateDashboardDTO getMemberStats(Long memberId) {
        SyndicateMember member = syndicateMemberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        Loan loan = member.getLoan();

        BigDecimal marginDiff = loan.getBaseMargin().subtract(loan.getCurrentMargin()).abs();

        BigDecimal annualSavings = loan.getPrincipalAmount()
                .multiply(marginDiff)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        BigDecimal totalSavings = annualSavings.multiply(BigDecimal.valueOf(loan.getTenorYears()));

        BigDecimal shareRatio = member.getParticipationAmount()
                .divide(loan.getPrincipalAmount(), 4, RoundingMode.HALF_UP);

        BigDecimal mySavings = totalSavings.multiply(shareRatio);

        return SyndicateDashboardDTO.builder()
                .bankName(member.getBankName())
                .investmentAmount(member.getParticipationAmount())
                .ownershipSharePercentage(shareRatio.doubleValue() * 100.0)
                .totalLoanSavings(totalSavings)
                .myShareOfSavings(mySavings)
                .build();
    }




    @Transactional(readOnly = true)
    public List<SyndicateMember> getLoanMembers(Long loanId) {
        return syndicateMemberRepository.findByLoanId(loanId);
    }

}
 
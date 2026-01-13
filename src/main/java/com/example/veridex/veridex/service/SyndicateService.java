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

        return SyndicateDashboardDTO.builder()
                .loanId(loan.getId())
                .bankName(member.getBankName())
                .investmentAmount(member.getParticipationAmount())
                .ownershipSharePercentage(shareRatio.doubleValue() * 100.0)
                .totalLoanSavings(totalSavings)
                .myShareOfSavings(mySavings)
                .baseMargin(loan.getBaseMargin())
                .currentMargin(loan.getCurrentMargin())
                .build();
    }
    
    public List<VerificationReport> getAuditTrail(Long loanId) {

        return verificationRepository.findByEsgReport_Loan_IdOrderByVerifiedAtDesc(loanId);
    }
 
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




    public List<SyndicateMember> getLoanMembers(Long loanId) {
        return syndicateMemberRepository.findByLoanId(loanId);
    }

}

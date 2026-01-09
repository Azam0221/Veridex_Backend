package com.example.veridex.veridex.service;


import com.example.veridex.veridex.enum_.Role;
import com.example.veridex.veridex.model.*;
import com.example.veridex.veridex.repository.LoanRepository;
import com.example.veridex.veridex.repository.SyndicateMemberRepository;
import com.example.veridex.veridex.repository.VerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SyndicateService {


    private final SyndicateMemberRepository syndicateMemberRepository;
    private final LoanRepository loanRepository;
    private final VerificationRepository verificationRepository;

    public SyndicateMember addMember(Long loanId , SyndicateRequest request){

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        SyndicateMember member = new SyndicateMember();
        member.setLoan(loan);
        member.setBankName(request.getBankName());
        member.setParticipationAmount(request.getParticipationAmount());

        member.setRole(Role.valueOf(request.getRole().toUpperCase()));

        return syndicateMemberRepository.save(member);
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

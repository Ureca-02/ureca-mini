package com.example.ureka02.settlement.service;

import com.example.ureka02.payment.entity.Payment;
import com.example.ureka02.payment.enums.PaymentStatus;
import com.example.ureka02.payment.repository.PaymentRepository;
import com.example.ureka02.recruitment.entity.Recruitment;
import com.example.ureka02.recruitment.entity.RecruitmentMember;
import com.example.ureka02.settlement.dto.SettlementProgressResponse;
import com.example.ureka02.settlement.dto.SettlementStatusResponse;
import com.example.ureka02.settlement.entity.Settlement;
import com.example.ureka02.settlement.enums.SettlementStatus;
import com.example.ureka02.settlement.repository.SettlementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementService {

    private final SettlementRepository settlementRepository;
    private final PaymentRepository paymentRepository;

    /**
     * 정산 생성 (모집 완료 시 자동 호출)
     */
    @Transactional
    public Settlement createSettlement(Recruitment recruitment, Integer totalAmount) {
        // 이미 정산이 있는지 확인
        if (recruitment.getSettlement() != null) {
            throw new IllegalStateException("이미 정산이 생성된 모집입니다.");
        }

        int memberCount = recruitment.getMembers().size();
        if (memberCount == 0) {
            throw new IllegalStateException("멤버가 없어 정산을 생성할 수 없습니다.");
        }

        int amountPerPerson = totalAmount / memberCount;

        Settlement settlement = Settlement.builder()
                .recruitment(recruitment)
                .totalAmount(totalAmount)
                .amountPerPerson(amountPerPerson)
                .status(SettlementStatus.PENDING)
                .build();

        return settlementRepository.save(settlement);
    }

    /**
     * 정산 시작 - 모든 멤버에게 결제 요청 생성
     */
    @Transactional
    public void startSettlement(Long settlementId) {
        Settlement settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new IllegalArgumentException("정산을 찾을 수 없습니다."));

        if (settlement.getStatus() != SettlementStatus.PENDING) {
            throw new IllegalStateException("정산을 시작할 수 없는 상태입니다.");
        }

        // 모든 멤버에게 결제 요청 생성
        for (RecruitmentMember member : settlement.getRecruitment().getMembers()) {
            String orderId = generateOrderId(settlement.getId(), member.getId());

            Payment payment = Payment.builder()
                    .orderId(orderId)
                    .amount(settlement.getAmountPerPerson())
                    .status(PaymentStatus.PENDING)
                    .member(member)
                    .build();

            settlement.addPayment(payment);
            member.setPayment(payment);
        }

        settlement.start();
        log.info("정산 시작 - Settlement ID: {}, 총 {}명의 멤버에게 결제 요청 생성",
                settlementId, settlement.getPayments().size());
    }

    /**
     * 결제 완료 후 정산 상태 업데이트
     */
    @Transactional
    public void updateSettlementProgress(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("결제를 찾을 수 없습니다."));

        Settlement settlement = payment.getSettlement();
        if (settlement == null) {
            log.warn("정산과 연결되지 않은 결제입니다. Payment ID: {}", paymentId);
            return;
        }

        log.info("결제 완료 - Settlement ID: {}, 진행률: {}/{}",
                settlement.getId(),
                settlement.getCompletedPaymentCount(),
                settlement.getTotalPaymentCount());

        // 모든 결제가 완료되었는지 확인
        settlement.checkAndComplete();

        if (settlement.getStatus() == SettlementStatus.COMPLETED) {
            log.info("정산 완료! Settlement ID: {}", settlement.getId());
        }
    }

    /**
     * 정산 조회
     */
    @Transactional(readOnly = true)
    public Settlement getSettlement(Long settlementId) {
        return settlementRepository.findById(settlementId)
                .orElseThrow(() -> new IllegalArgumentException("정산을 찾을 수 없습니다."));
    }

    /**
     * 정산 진행 상황 조회
     */
    @Transactional(readOnly = true)
    public SettlementProgressResponse getSettlementProgress(Long settlementId) {
        Settlement settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new IllegalArgumentException("정산을 찾을 수 없습니다."));

        return SettlementProgressResponse.builder()
                .settlementId(settlement.getId())
                .status(settlement.getStatus())
                .totalAmount(settlement.getTotalAmount())
                .amountPerPerson(settlement.getAmountPerPerson())
                .completedCount(settlement.getCompletedPaymentCount())
                .totalCount(settlement.getTotalPaymentCount())
                .isCompleted(settlement.getStatus() == SettlementStatus.COMPLETED)
                .build();
    }

    /**
     * 정산 상세 상태 조회
     */
    @Transactional(readOnly = true)
    public SettlementStatusResponse getSettlementStatus(Long settlementId) {
        Settlement settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new IllegalArgumentException("정산을 찾을 수 없습니다."));

        return SettlementStatusResponse.from(settlement);
    }

    /**
     * 주문 ID 생성
     */
    private String generateOrderId(Long settlementId, Long memberId) {
        return String.format("ST%d-M%d-%s",
                settlementId,
                memberId,
                UUID.randomUUID().toString().substring(0, 8));
    }
}
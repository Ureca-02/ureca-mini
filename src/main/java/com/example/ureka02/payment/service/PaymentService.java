package com.example.ureka02.payment.service;

import com.example.ureka02.payment.client.TossPaymentClient;
import com.example.ureka02.payment.dto.TossConfirmRequest;
import com.example.ureka02.payment.entity.Payment;
import com.example.ureka02.payment.enums.PaymentStatus;
import com.example.ureka02.payment.repository.PaymentRepository;
import com.example.ureka02.settlement.service.SettlementService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final TossPaymentClient tossPaymentClient;
    private final SettlementService settlementService;

    /**
     * 결제 승인 처리
     */
    @Transactional
    public Payment confirmPayment(String paymentKey, String orderId, Integer amount) {
        // 1. 결제 정보 조회
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다."));

        // 2. 금액 검증
        if (!payment.getAmount().equals(amount)) {
            payment.fail();
            throw new IllegalArgumentException("결제 금액이 일치하지 않습니다.");
        }

        // 3. Toss API 승인 요청
        try {
            TossConfirmRequest request = new TossConfirmRequest(paymentKey, orderId, amount);
            JsonNode response = tossPaymentClient.confirmPayment(request);

            String method = response.has("method") ? response.get("method").asText() : "UNKNOWN";

            // 4. 결제 승인 처리
            payment.approve(paymentKey, method);
            log.info("결제 승인 완료 - Order ID: {}, Payment Key: {}", orderId, paymentKey);

            // 5. 정산 진행률 업데이트
            if (payment.getSettlement() != null) {
                settlementService.updateSettlementProgress(payment.getId());
            }

            return payment;

        } catch (Exception e) {
            log.error("결제 승인 실패 - Order ID: {}", orderId, e);
            payment.fail();
            throw new RuntimeException("결제 승인에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * 결제 실패 처리
     */
    @Transactional
    public void failPayment(String orderId, String errorMessage) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다."));

        payment.fail();
        log.warn("결제 실패 - Order ID: {}, Error: {}", orderId, errorMessage);
    }

    /**
     * 결제 정보 조회
     */
    @Transactional(readOnly = true)
    public Payment getPayment(String orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다."));
    }

    /**
     * 결제 취소
     */
    @Transactional
    public void cancelPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다."));

        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new IllegalStateException("완료된 결제만 취소할 수 있습니다.");
        }

        // TODO: Toss API 취소 요청 구현
        payment.cancel();
        log.info("결제 취소 - Payment ID: {}", paymentId);
    }
}
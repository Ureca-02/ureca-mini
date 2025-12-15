package com.example.ureka02.payment.controller;

import com.example.ureka02.payment.entity.Payment;
import com.example.ureka02.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * 결제 페이지
     */
    @GetMapping("/checkout")
    public String checkout(@RequestParam String orderId, Model model) {
        try {
            Payment payment = paymentService.getPayment(orderId);

            model.addAttribute("orderId", payment.getOrderId());
            model.addAttribute("amount", payment.getAmount());
            model.addAttribute("orderName", "식사 정산 결제");

            // 정산 정보가 있다면 추가
            if (payment.getSettlement() != null) {
                model.addAttribute("settlementId", payment.getSettlement().getId());
                model.addAttribute("recruitmentTitle",
                        payment.getSettlement().getRecruitment().getTitle());
            }

            return "toss/checkout";

        } catch (Exception e) {
            log.error("결제 페이지 로드 실패", e);
            model.addAttribute("error", e.getMessage());
            return "toss/fail";
        }
    }

    /**
     * 결제 승인
     */
    @GetMapping("/success")
    public String success(
            @RequestParam String paymentKey,
            @RequestParam String orderId,
            @RequestParam Integer amount,
            Model model) {

        try {
            Payment payment = paymentService.confirmPayment(paymentKey, orderId, amount);

            model.addAttribute("orderId", payment.getOrderId());
            model.addAttribute("amount", payment.getAmount());
            model.addAttribute("paymentKey", payment.getPaymentKey());
            model.addAttribute("method", payment.getMethod());

            // 정산 진행 정보
            if (payment.getSettlement() != null) {
                int completed = payment.getSettlement().getCompletedPaymentCount();
                int total = payment.getSettlement().getTotalPaymentCount();
                model.addAttribute("settlementProgress",
                        String.format("%d/%d명 결제 완료", completed, total));
                model.addAttribute("isAllCompleted",
                        payment.getSettlement().isAllPaid());
            }

            return "toss/success";

        } catch (Exception e) {
            log.error("결제 승인 실패", e);
            model.addAttribute("error", e.getMessage());
            return "toss/fail";
        }
    }

    /**
     * 결제 실패
     */
    @GetMapping("/fail")
    public String fail(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String message,
            @RequestParam(required = false) String orderId,
            Model model) {

        if (orderId != null) {
            paymentService.failPayment(orderId, message);
        }

        model.addAttribute("code", code);
        model.addAttribute("message", message);

        return "toss/fail";
    }
}
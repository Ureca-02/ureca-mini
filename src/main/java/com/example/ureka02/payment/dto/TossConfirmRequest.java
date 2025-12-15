package com.example.ureka02.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor  // 이 어노테이션이 생성자를 자동으로 만들어줍니다
public class TossConfirmRequest {
    private String paymentKey;
    private String orderId;
    private Integer amount;  // Integer 타입 주의!

}
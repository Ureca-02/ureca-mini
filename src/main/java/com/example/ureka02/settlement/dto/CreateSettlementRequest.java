package com.example.ureka02.settlement.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 정산 생성 요청
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateSettlementRequest {
    private Long recruitmentId;
    private Integer totalAmount;
}
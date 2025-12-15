package com.example.ureka02.settlement.enums;

public enum SettlementStatus {
    PENDING,        // 정산 대기 (생성됨, 아직 시작 안 함)
    IN_PROGRESS,    // 정산 진행중 (결제 요청 발송됨)
    COMPLETED,      // 정산 완료 (모든 멤버 결제 완료)
    CANCELED        // 정산 취소
}
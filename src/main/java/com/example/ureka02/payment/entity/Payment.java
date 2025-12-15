package com.example.ureka02.payment.entity;

import com.example.ureka02.payment.enums.PaymentStatus;
import com.example.ureka02.recruitment.entity.RecruitmentMember;
import com.example.ureka02.settlement.entity.Settlement;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long id;

    // Toss 결제 관련
    @Column(name = "order_id", unique = true, nullable = false)
    private String orderId;

    @Column(name = "payment_key")
    private String paymentKey;

    @Column(nullable = false)
    private Integer amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    // 정산 연동
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "settlement_id")
    private Settlement settlement;

    // 결제자 (멤버)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private RecruitmentMember member;

    // 결제 타입 및 메타데이터
    @Column(name = "payment_method")
    private String method;

    // 시간 정보
    @Column(name = "requested_at")
    private LocalDateTime requestedAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (requestedAt == null) {
            requestedAt = LocalDateTime.now();
        }
    }

    // 비즈니스 로직
    public void approve(String paymentKey, String method) {
        this.paymentKey = paymentKey;
        this.method = method;
        this.status = PaymentStatus.COMPLETED;
        this.approvedAt = LocalDateTime.now();
    }

    public void cancel() {
        this.status = PaymentStatus.CANCELED;
    }

    public void fail() {
        this.status = PaymentStatus.FAILED;
    }

    public boolean isCompleted() {
        return this.status == PaymentStatus.COMPLETED;
    }

    // Setter (양방향 관계 설정용)
    public void setMember(RecruitmentMember member) {
        this.member = member;
    }

    public void setSettlement(Settlement settlement) {
        this.settlement = settlement;
    }
}
package com.example.ureka02.settlement.controller;

import com.example.ureka02.settlement.dto.SettlementProgressResponse;
import com.example.ureka02.settlement.dto.SettlementStatusResponse;
import com.example.ureka02.settlement.service.SettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settlements")
@RequiredArgsConstructor
public class SettlementController {

    private final SettlementService settlementService;

    /**
     * 정산 시작 (팀장이 호출)
     * POST /api/settlements/{settlementId}/start
     */
    @PostMapping("/{settlementId}/start")
    public ResponseEntity<?> startSettlement(@PathVariable Long settlementId) {
        settlementService.startSettlement(settlementId);
        return ResponseEntity.ok().body("정산이 시작되었습니다. 모든 멤버에게 결제 요청이 전송되었습니다.");
    }

    /**
     * 정산 진행 현황 조회
     * GET /api/settlements/{settlementId}/progress
     */
    @GetMapping("/{settlementId}/progress")
    public ResponseEntity<SettlementProgressResponse> getProgress(@PathVariable Long settlementId) {
        SettlementProgressResponse response = settlementService.getSettlementProgress(settlementId);
        return ResponseEntity.ok(response);
    }

    /**
     * 정산 상세 상태 조회
     * GET /api/settlements/{settlementId}
     */
    @GetMapping("/{settlementId}")
    public ResponseEntity<SettlementStatusResponse> getStatus(@PathVariable Long settlementId) {
        SettlementStatusResponse response = settlementService.getSettlementStatus(settlementId);
        return ResponseEntity.ok(response);
    }
}
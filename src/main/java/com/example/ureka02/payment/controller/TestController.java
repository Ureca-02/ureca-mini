package com.example.ureka02.payment.controller;

import com.example.ureka02.recruitment.enums.MemberRole;
import com.example.ureka02.recruitment.entity.Recruitment;
import com.example.ureka02.recruitment.entity.RecruitmentMember;
import com.example.ureka02.recruitment.repository.RecruitmentMemberRepository;
import com.example.ureka02.recruitment.repository.RecruitmentRepository;
import com.example.ureka02.settlement.entity.Settlement;
import com.example.ureka02.settlement.service.SettlementService;
import com.example.ureka02.user.entity.User;
import com.example.ureka02.user.enums.AuthProvider;
import com.example.ureka02.user.enums.Role;
import com.example.ureka02.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 테스트용 컨트롤러
 */
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {

    private final UserRepository userRepository;
    private final RecruitmentRepository recruitmentRepository;
    private final RecruitmentMemberRepository memberRepository;
    private final SettlementService settlementService;

    /**
     * 테스트 페이지 (HTML)
     */
    @GetMapping(value = "", produces = "text/html")
    @ResponseBody
    public String testPage() {
        // test-links.html 내용을 직접 반환하거나
        // resources/static/test-links.html에 파일을 넣고 redirect
        return "redirect:/test-links.html";
    }

    /**
     * 테스트 데이터 생성
     */
    @GetMapping("/setup")
    public ResponseEntity<?> setupTestData() {
        // 1. 사용자 생성 (팀원 User 엔티티 구조에 맞춤)
        User leader = userRepository.save(User.builder()
                .name("김팀장")
                .email("leader@test.com")
                .provider(AuthProvider.LOCAL)
                .role(Role.USER)
                .password("password123")
                .build());

        User member1 = userRepository.save(User.builder()
                .name("이멤버")
                .email("member1@test.com")
                .provider(AuthProvider.LOCAL)
                .role(Role.USER)
                .password("password123")
                .build());

        User member2 = userRepository.save(User.builder()
                .name("박멤버")
                .email("member2@test.com")
                .provider(AuthProvider.LOCAL)
                .role(Role.USER)
                .password("password123")
                .build());

        User member3 = userRepository.save(User.builder()
                .name("최멤버")
                .email("member3@test.com")
                .provider(AuthProvider.LOCAL)
                .role(Role.USER)
                .password("password123")
                .build());

        // 2. 모집글 생성
        Recruitment recruitment = Recruitment.builder()
                .title("점심 식사 모집")
                .description("오늘 12시에 같이 점심 드실 분 모집합니다!")
                .totalSpots(4)
                .endTime(LocalDateTime.now().plusDays(1))
                .creator(leader)
                .build();
        recruitment = recruitmentRepository.save(recruitment);

        // 3. 멤버 추가
        RecruitmentMember leaderMember = RecruitmentMember.builder()
                .recruitment(recruitment)
                .user(leader)  // ⭐ member 필드 사용
                .role(MemberRole.LEADER)
                .build();
        memberRepository.save(leaderMember);
        recruitment.addMember(leaderMember);

        RecruitmentMember recruitMember1 = RecruitmentMember.builder()
                .recruitment(recruitment)
                .user(member1)
                .role(MemberRole.MEMBER)
                .build();
        memberRepository.save(recruitMember1);
        recruitment.addMember(recruitMember1);

        RecruitmentMember recruitMember2 = RecruitmentMember.builder()
                .recruitment(recruitment)
                .user(member2)
                .role(MemberRole.MEMBER)
                .build();
        memberRepository.save(recruitMember2);
        recruitment.addMember(recruitMember2);

        RecruitmentMember recruitMember3 = RecruitmentMember.builder()
                .recruitment(recruitment)
                .user(member3)
                .role(MemberRole.MEMBER)
                .build();
        memberRepository.save(recruitMember3);
        recruitment.addMember(recruitMember3);

        // 4. 정산 생성
        Settlement settlement = settlementService.createSettlement(recruitment, 40000);

        Map<String, Object> result = new HashMap<>();
        result.put("message", "테스트 데이터 생성 완료");
        result.put("recruitmentId", recruitment.getId());
        result.put("settlementId", settlement.getId());
        result.put("users", Map.of(
                "leader", leader.getId(),
                "member1", member1.getId(),
                "member2", member2.getId(),
                "member3", member3.getId()
        ));

        return ResponseEntity.ok(result);
    }

    /**
     * 정산 시작
     */
    @PostMapping("/settlement/{settlementId}/start")
    public ResponseEntity<?> startSettlement(@PathVariable Long settlementId) {
        settlementService.startSettlement(settlementId);

        // 결제 링크 생성
        Settlement settlement = settlementService.getSettlement(settlementId);
        Map<String, String> paymentLinks = new HashMap<>();

        for (var payment : settlement.getPayments()) {
            String memberName = payment.getMember().getUser().getName();
            String paymentUrl = "http://localhost:8080/payment/checkout?orderId=" + payment.getOrderId();
            paymentLinks.put(memberName, paymentUrl);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("message", "정산 시작 완료");
        result.put("settlementId", settlementId);
        result.put("paymentLinks", paymentLinks);

        return ResponseEntity.ok(result);
    }

    /**
     * 데이터 삭제
     */
    @DeleteMapping("/cleanup")
    public ResponseEntity<?> cleanup() {
        memberRepository.deleteAll();
        recruitmentRepository.deleteAll();
        userRepository.deleteAll();

        return ResponseEntity.ok(Map.of("message", "모든 테스트 데이터 삭제 완료"));
    }
}
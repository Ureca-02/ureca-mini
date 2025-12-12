package com.example.ureka02.recruitment.service;

import java.time.LocalDateTime;

import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ureka02.global.config.RedissionConfig;
import com.example.ureka02.global.error.CommonException;
import com.example.ureka02.global.error.ErrorCode;
import com.example.ureka02.recruitment.Enum.RecruitApplyStatus;
import com.example.ureka02.recruitment.dto.response.RecruitDetailResponse;
import com.example.ureka02.recruitment.entity.Recruitment;
import com.example.ureka02.recruitment.entity.RecruitmentApply;
import com.example.ureka02.recruitment.repository.RecruitApplyRepository;
import com.example.ureka02.recruitment.repository.RecruitRepository;
import com.example.ureka02.user.User;
import com.example.ureka02.user.UserRepository;

import lombok.RequiredArgsConstructor;

// 모집 신청 처리
// 사용자가 모집글에 신청할 때, Redis를 사용하여 인원 수를 체크하고,
// Redisson으로 중복 신청 방지를 처리

@Service
@RequiredArgsConstructor
public class RecruitApplyService {

   /*
    * private final RecruitRepository recruitRepository;
    * private final RecruitApplyRepository recruitApplyRepository;
    * private final UserRepository userRepository;
    * 
    * @Transactional
    * public void applyRecruitment(Long recruitmentId, Long userId) {
    * // 동시성 제어 로직 작성
    * 
    * 
    * // 사용자 및 유저 정보 체크
    * User applier = userRepository.findById(userId).orElse(()-> new
    * CommonException(ErrorCode.USER_NOT_FOUND));
    * Recruitment recruitment = recruitRepository.findById(recruitmentId)
    * .orElseThrow(() -> new CommonException(ErrorCode.RECRUITMENT_NOT_FOUND));
    * 
    * 
    * // 3. 마감 시간 체크
    * LocalDateTime now = LocalDateTime.now();
    * if (recruitment.getEndTime() != null &&
    * recruitment.getEndTime().isBefore(now)) {
    * throw new CommonException(ErrorCode.RECRUITMENT_EXPIRED);
    * }
    * 
    * 
    * 
    * // 4. 중복 신청 방지
    * boolean alreadyApplied =
    * recruitApplyRepository.existsByRecruitmentIdAndApplierIdAndStatus(
    * recruitmentId, userId, RecruitApplyStatus.APPLIED);
    * if(alreadyApplied){
    * throw new CommonException(ErrorCode.);
    * }
    * 
    * // 5. 모집 인원 증가
    * recruitment.increaseCurrentSpots();
    * 
    * 
    * // 5. 신청 정보 저장
    * RecruitmentApply recruitmentApply =
    * RecruitmentApply.builder().recruitment(recruitment).applier()
    * 
    * }
    * `
    */
   // 내가 신청한 모집리스트 조회
   /*
    * public Page<MyAppliedRecruitResponse> getMyAplliedRecruits() {
    * 
    * }
    */

}

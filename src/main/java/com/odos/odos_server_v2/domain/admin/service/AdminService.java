package com.odos.odos_server_v2.domain.admin.service;

import com.odos.odos_server_v2.domain.admin.dto.AdminMemberFilterRequest;
import com.odos.odos_server_v2.domain.admin.dto.AdminMemberResponseDto;
import com.odos.odos_server_v2.domain.admin.repository.AdminMemberRepository;
import com.odos.odos_server_v2.domain.member.entity.Enum.MemberStatus;
import com.odos.odos_server_v2.domain.member.entity.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AdminService {

  private final AdminMemberRepository adminMemberRepository;

  /**
   * 필터와 페이지네이션을 적용하여 회원 목록 조회
   *
   * @param filterRequest 필터 조건 (성별, 직업, 가입 경로)
   * @param pageable 페이지네이션 정보
   * @return 필터링된 회원 목록
   */
  public Page<AdminMemberResponseDto> getMembers(
      AdminMemberFilterRequest filterRequest, Pageable pageable) {

    log.info(
        "회원 목록 조회 - 필터: gender={}, job={}, signupRoute={}",
        filterRequest.getGender(),
        filterRequest.getJob(),
        filterRequest.getSignupRoute());

    Page<Member> memberPage =
        adminMemberRepository.findMembersWithFilters(
            filterRequest.getGender(),
            filterRequest.getJob(),
            filterRequest.getSignupRoute(),
            MemberStatus.ACTIVE,
            pageable);

    return memberPage.map(AdminMemberResponseDto::fromMember);
  }
}

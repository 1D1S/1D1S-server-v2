package com.odos.odos_server_v2.domain.admin;

import static org.assertj.core.api.Assertions.assertThat;

import com.odos.odos_server_v2.domain.admin.dto.AdminMemberResponseDto;
import com.odos.odos_server_v2.domain.member.entity.Enum.MemberRole;
import com.odos.odos_server_v2.domain.member.entity.Member;
import org.junit.jupiter.api.Test;

/**
 * 어드민 계약 락(계약 대조 #2): GET /admin/members 응답 DTO 는 어드민 화면이 권한 부여 버튼 활성/비활성에 쓰는 {@code isAdmin} 필드를
 * 반드시 포함해야 한다. AdminMemberResponseDto.fromMember 가 role 기준으로 isAdmin 을 채우는지 순수 단위로 고정한다.
 */
class AdminMemberResponseIsAdminTest {

  @Test
  void fromMember_admin_setsIsAdminTrue() {
    Member admin = Member.builder().email("admin@t.com").role(MemberRole.ADMIN).build();

    AdminMemberResponseDto dto = AdminMemberResponseDto.fromMember(admin);

    assertThat(dto.getIsAdmin()).isTrue();
  }

  @Test
  void fromMember_normalUser_setsIsAdminFalse() {
    Member user = Member.builder().email("user@t.com").role(MemberRole.USER).build();

    AdminMemberResponseDto dto = AdminMemberResponseDto.fromMember(user);

    assertThat(dto.getIsAdmin()).isFalse();
  }
}

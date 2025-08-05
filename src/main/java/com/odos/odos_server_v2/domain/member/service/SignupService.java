package com.odos.odos_server_v2.domain.member.service;

import com.odos.odos_server_v2.domain.member.dto.SignupInfoRequest;
import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.member.repository.MemberRepository;
import com.odos.odos_server_v2.domain.shared.Enum.Category;
import com.odos.odos_server_v2.exception.CustomException;
import com.odos.odos_server_v2.exception.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SignupService {

  private final MemberRepository memberRepository;

  @Transactional
  public void completeSignupInfo(Long memberId, SignupInfoRequest request) {
    Member member =
        memberRepository
            .findById(memberId)
            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

    List<Category> list = request.getCategory();
    if (list == null || list.isEmpty()) {
      throw new CustomException(ErrorCode.CATEGORY_EMPTY);
    }
    if (list.size() > 3) {
      throw new CustomException(ErrorCode.CATEGORY_TOO_MANY);
    }

    String regex = "^[가-힣a-zA-Z]{1,8}$";
    if (!request.getNickname().matches(regex)) {
      throw new CustomException(ErrorCode.INVALID_NICKNAME_FORMAT);
    }

    member.completeProfile(
        request.getNickname(),
        request.getProfileUrl(),
        request.getJob(),
        request.getBirth(),
        request.getGender(),
        request.getIsPublic());

    member.updateCategories(list);

    memberRepository.save(member);
  }
}

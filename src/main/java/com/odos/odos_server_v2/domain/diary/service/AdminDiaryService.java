package com.odos.odos_server_v2.domain.diary.service;

import com.odos.odos_server_v2.domain.challenge.dto.ChallengeSummaryResponse;
import com.odos.odos_server_v2.domain.challenge.service.ChallengeService;
import com.odos.odos_server_v2.domain.comment.repository.CommentRepository;
import com.odos.odos_server_v2.domain.diary.dto.DiaryDeleteAdminRequest;
import com.odos.odos_server_v2.domain.diary.dto.DiaryDeleteAdminResponse;
import com.odos.odos_server_v2.domain.diary.dto.DiaryResponse;
import com.odos.odos_server_v2.domain.diary.entity.Diary;
import com.odos.odos_server_v2.domain.diary.repository.DiaryRepository;
import com.odos.odos_server_v2.domain.member.CurrentUserContext;
import com.odos.odos_server_v2.domain.member.entity.Enum.MemberRole;
import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.member.repository.MemberRepository;
import com.odos.odos_server_v2.domain.shared.dto.OffsetPagination;
import com.odos.odos_server_v2.domain.shared.service.ImageService;
import com.odos.odos_server_v2.exception.CustomException;
import com.odos.odos_server_v2.exception.ErrorCode;
import jakarta.transaction.Transactional;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminDiaryService {
  private final DiaryRepository diaryRepository;
  private final MemberRepository memberRepository;
  private final ChallengeService challengeService;
  private final ImageService imageService;
  private final CommentRepository commentRepository;

  private Member requireAdmin() {
    Long memberId = CurrentUserContext.getCurrentMemberId();
    Member member =
        memberRepository
            .findById(memberId)
            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    if (!member.getRole().equals(MemberRole.ADMIN)) {
      throw new CustomException(ErrorCode.UNAUTHORIZED);
    }
    return member;
  }

  @Transactional
  public DiaryResponse getDiaryByAdmin(Long diaryId) {
    Member admin = requireAdmin();
    Diary diary =
        diaryRepository
            .findById(diaryId)
            .orElseThrow(() -> new CustomException(ErrorCode.DIARY_NOT_FOUND));

    return toDiaryResponse(admin, diary, commentRepository.countByDiaryId(diary.getId()));
  }

  @Transactional
  public OffsetPagination<DiaryResponse> getDiariesByAdmin(
      Pageable pageable, String filterType, String authorNickname, Long challengeId) {
    Member admin = requireAdmin();
    String normalizedAuthorNickname =
        authorNickname == null || authorNickname.isBlank() ? null : authorNickname.trim();
    Page<Diary> diaries =
        findAdminDiaries(pageable, filterType, normalizedAuthorNickname, challengeId);

    Map<Long, Long> commentCounts = getCommentCountMap(diaries.getContent());
    Page<DiaryResponse> result =
        diaries.map(
            diary -> toDiaryResponse(admin, diary, commentCounts.getOrDefault(diary.getId(), 0L)));
    return OffsetPagination.from(result);
  }

  @Transactional
  public OffsetPagination<DiaryResponse> getChallengeDiariesByAdmin(
      Long challengeId, Pageable pageable) {
    return getDiariesByAdmin(pageable, "challenge", null, challengeId);
  }

  @Transactional
  public DiaryDeleteAdminResponse deleteDiaryByAdmin(DiaryDeleteAdminRequest request) {
    requireAdmin();
    if (request == null || request.getDiaryId() == null) {
      throw new CustomException(ErrorCode.DIARY_NOT_FOUND);
    }
    if (request.getDeleteReason() == null || request.getDeleteReason().isBlank()) {
      throw new CustomException(ErrorCode.DIARY_NOT_ACCESS);
    }

    Diary diary =
        diaryRepository
            .findById(request.getDiaryId())
            .orElseThrow(() -> new CustomException(ErrorCode.DIARY_NOT_FOUND));
    diary.updateDeletedReason(request.getDeleteReason());
    diary.softDelete();
    Diary deletedDiary = diaryRepository.save(diary);
    return DiaryDeleteAdminResponse.from(deletedDiary);
  }

  private Page<Diary> findAdminDiaries(
      Pageable pageable, String filterType, String authorNickname, Long challengeId) {
    if (filterType == null || filterType.equalsIgnoreCase("latest")) {
      return diaryRepository.findAdminDiariesOrderByLatest(pageable);
    }
    if (filterType.equalsIgnoreCase("author")) {
      return diaryRepository.findAdminDiariesByAuthorNicknameOrderByLatest(
          authorNickname, pageable);
    }
    if (filterType.equalsIgnoreCase("challenge")) {
      return diaryRepository.findAdminDiariesByChallengeIdOrderByLatest(challengeId, pageable);
    }
    if (filterType.equalsIgnoreCase("like")
        || filterType.equalsIgnoreCase("likes")
        || filterType.equalsIgnoreCase("likeCount")) {
      return diaryRepository.findAdminDiariesOrderByLike(pageable);
    }
    return diaryRepository.findAdminDiariesOrderByLatest(pageable);
  }

  private DiaryResponse toDiaryResponse(Member admin, Diary diary, long commentCount) {
    ChallengeSummaryResponse summary =
        challengeService.toChallengeSummary(diary.getChallenge(), admin.getId());
    return DiaryResponse.from(
        admin,
        diary,
        summary,
        imageService.getFileUrl(diary.getMember().getProfileUrl()),
        commentCount);
  }

  private Map<Long, Long> getCommentCountMap(List<Diary> diaries) {
    if (diaries == null || diaries.isEmpty()) {
      return Collections.emptyMap();
    }

    List<Long> diaryIds = diaries.stream().map(Diary::getId).filter(Objects::nonNull).toList();
    if (diaryIds.isEmpty()) {
      return Collections.emptyMap();
    }

    Map<Long, Long> commentCounts = new HashMap<>();
    for (Object[] row : diaryRepository.countCommentsByDiaryIds(diaryIds)) {
      commentCounts.put((Long) row[0], (Long) row[1]);
    }
    return commentCounts;
  }
}

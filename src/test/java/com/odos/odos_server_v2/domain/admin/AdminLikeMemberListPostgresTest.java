package com.odos.odos_server_v2.domain.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import com.odos.odos_server_v2.domain.challenge.entity.Challenge;
import com.odos.odos_server_v2.domain.challenge.entity.ChallengeLike;
import com.odos.odos_server_v2.domain.challenge.entity.Enum.ChallengeType;
import com.odos.odos_server_v2.domain.challenge.entity.Enum.ParticipationType;
import com.odos.odos_server_v2.domain.challenge.repository.ChallengeLikeRepository;
import com.odos.odos_server_v2.domain.challenge.repository.ChallengeRepository;
import com.odos.odos_server_v2.domain.challenge.service.AdminChallengeService;
import com.odos.odos_server_v2.domain.challenge.service.ChallengeService;
import com.odos.odos_server_v2.domain.comment.repository.CommentRepository;
import com.odos.odos_server_v2.domain.diary.entity.Diary;
import com.odos.odos_server_v2.domain.diary.entity.DiaryLike;
import com.odos.odos_server_v2.domain.diary.repository.DiaryLikeRepository;
import com.odos.odos_server_v2.domain.diary.repository.DiaryRepository;
import com.odos.odos_server_v2.domain.diary.service.AdminDiaryService;
import com.odos.odos_server_v2.domain.member.entity.Enum.MemberRole;
import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.member.repository.MemberRepository;
import com.odos.odos_server_v2.domain.security.jwt.MemberPrincipal;
import com.odos.odos_server_v2.domain.shared.dto.LikeMemberResponse;
import com.odos.odos_server_v2.domain.shared.dto.OffsetPagination;
import com.odos.odos_server_v2.domain.shared.service.ImageService;
import com.odos.odos_server_v2.exception.CustomException;
import com.odos.odos_server_v2.exception.ErrorCode;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * 어드민 전용 "좋아요 누른 회원 목록" 조회 회귀: (a) 관리자는 회원 ID 순으로 좋아요 회원을 조회, (b) 비관리자는 UNAUTHORIZED, (c) 없는
 * 일지/챌린지는 404, (d) 응답에 회원이 중복 없이 담긴다(V36 유니크 제약이 보장). 실 Postgres + 실 Flyway.
 */
@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AdminLikeMemberListPostgresTest {

  @Container
  static final PostgreSQLContainer<?> POSTGRES =
      new PostgreSQLContainer<>("postgres:16-alpine")
          .withDatabaseName("odos")
          .withUsername("odos")
          .withPassword("odos");

  @DynamicPropertySource
  static void props(DynamicPropertyRegistry r) {
    r.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    r.add("spring.datasource.username", POSTGRES::getUsername);
    r.add("spring.datasource.password", POSTGRES::getPassword);
    r.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    r.add("spring.jpa.hibernate.ddl-auto", () -> "none");
    r.add("spring.jpa.properties.hibernate.default_schema", () -> "odos_dev");
    r.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
    r.add("spring.flyway.enabled", () -> "true");
    r.add("spring.flyway.schemas", () -> "odos_dev");
    r.add("spring.flyway.default-schema", () -> "odos_dev");
    r.add("spring.flyway.create-schemas", () -> "true");
  }

  @Autowired DiaryRepository diaryRepository;
  @Autowired DiaryLikeRepository diaryLikeRepository;
  @Autowired ChallengeRepository challengeRepository;
  @Autowired ChallengeLikeRepository challengeLikeRepository;
  @Autowired MemberRepository memberRepository;

  private AdminDiaryService diaryService() {
    return new AdminDiaryService(
        diaryRepository,
        diaryLikeRepository,
        memberRepository,
        mock(ChallengeService.class),
        mock(ImageService.class),
        mock(CommentRepository.class));
  }

  private AdminChallengeService challengeService() {
    return new AdminChallengeService(
        challengeRepository,
        challengeLikeRepository,
        memberRepository,
        mock(ChallengeService.class));
  }

  private void authenticateAs(Member member) {
    MemberPrincipal principal =
        new MemberPrincipal(member.getId(), member.getEmail(), "USER", null);
    SecurityContextHolder.getContext()
        .setAuthentication(
            new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
  }

  @AfterEach
  void clearContext() {
    SecurityContextHolder.clearContext();
  }

  private Member saveMember(String email, String nickname, MemberRole role) {
    return memberRepository.save(
        Member.builder().email(email).nickname(nickname).role(role).build());
  }

  private Challenge saveChallenge(Member host) {
    LocalDate today = LocalDate.now();
    return challengeRepository.save(
        Challenge.builder()
            .title("챌린지")
            .hostMember(host)
            .startDate(today.minusDays(1))
            .endDate(today.plusDays(30))
            .challengeType(ChallengeType.PUBLIC)
            .participationType(ParticipationType.GROUP)
            .maxParticipantsCnt(10L)
            .build());
  }

  @Test
  void diaryLikers_adminSeesLikersOrderedByMemberId() {
    Member admin = saveMember("admin@t.com", "관리자", MemberRole.ADMIN);
    Member author = saveMember("author@t.com", "작성자", MemberRole.USER);
    Member liker1 = saveMember("l1@t.com", "라이커1", MemberRole.USER);
    Member liker2 = saveMember("l2@t.com", "라이커2", MemberRole.USER);
    Challenge ch = saveChallenge(author);
    Diary diary =
        diaryRepository.save(
            Diary.builder()
                .member(author)
                .challenge(ch)
                .isPublic(true)
                .completedDate(LocalDate.now())
                .build());
    diaryLikeRepository.save(DiaryLike.builder().member(liker2).diary(diary).build());
    diaryLikeRepository.save(DiaryLike.builder().member(liker1).diary(diary).build());

    authenticateAs(admin);
    OffsetPagination<LikeMemberResponse> result =
        diaryService().getDiaryLikersByAdmin(diary.getId(), PageRequest.of(0, 20));

    List<LikeMemberResponse> items = result.getItems();
    assertThat(items).hasSize(2);
    // id 순 정렬: liker1(먼저 저장, 작은 id) → liker2
    assertThat(items.get(0).getMemberId()).isEqualTo(liker1.getId());
    assertThat(items.get(1).getMemberId()).isEqualTo(liker2.getId());
    assertThat(items.get(0).getNickname()).isEqualTo("라이커1");
    assertThat(result.getPageInfo().getTotalElements()).isEqualTo(2);
  }

  @Test
  void diaryLikers_nonAdminIsUnauthorized() {
    Member author = saveMember("author2@t.com", "작성자", MemberRole.USER);
    Member notAdmin = saveMember("user2@t.com", "일반", MemberRole.USER);
    Challenge ch = saveChallenge(author);
    Diary diary =
        diaryRepository.save(
            Diary.builder()
                .member(author)
                .challenge(ch)
                .isPublic(true)
                .completedDate(LocalDate.now())
                .build());

    authenticateAs(notAdmin);
    assertThatThrownBy(
            () -> diaryService().getDiaryLikersByAdmin(diary.getId(), PageRequest.of(0, 20)))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(ErrorCode.UNAUTHORIZED);
  }

  @Test
  void diaryLikers_unknownDiaryIsNotFound() {
    Member admin = saveMember("admin3@t.com", "관리자", MemberRole.ADMIN);
    authenticateAs(admin);
    assertThatThrownBy(() -> diaryService().getDiaryLikersByAdmin(999999L, PageRequest.of(0, 20)))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(ErrorCode.DIARY_NOT_FOUND);
  }

  @Test
  void challengeLikers_adminSeesDistinctLikers() {
    Member admin = saveMember("admin4@t.com", "관리자", MemberRole.ADMIN);
    Member host = saveMember("host4@t.com", "호스트", MemberRole.USER);
    Member liker1 = saveMember("cl1@t.com", "라이커1", MemberRole.USER);
    Member liker2 = saveMember("cl2@t.com", "라이커2", MemberRole.USER);
    Challenge ch = saveChallenge(host);
    challengeLikeRepository.save(ChallengeLike.builder().member(liker1).challenge(ch).build());
    challengeLikeRepository.save(ChallengeLike.builder().member(liker2).challenge(ch).build());

    authenticateAs(admin);
    OffsetPagination<LikeMemberResponse> result =
        challengeService().getChallengeLikersByAdmin(ch.getId(), PageRequest.of(0, 20));

    assertThat(result.getItems()).hasSize(2);
    assertThat(result.getItems())
        .extracting(LikeMemberResponse::getMemberId)
        .containsExactly(liker1.getId(), liker2.getId());
  }

  @Test
  void challengeLikers_nonAdminIsUnauthorized() {
    Member host = saveMember("host5@t.com", "호스트", MemberRole.USER);
    Member notAdmin = saveMember("user5@t.com", "일반", MemberRole.USER);
    Challenge ch = saveChallenge(host);

    authenticateAs(notAdmin);
    assertThatThrownBy(
            () -> challengeService().getChallengeLikersByAdmin(ch.getId(), PageRequest.of(0, 20)))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(ErrorCode.UNAUTHORIZED);
  }

  @Test
  void challengeLikers_unknownChallengeIsNotFound() {
    Member admin = saveMember("admin6@t.com", "관리자", MemberRole.ADMIN);
    authenticateAs(admin);
    assertThatThrownBy(
            () -> challengeService().getChallengeLikersByAdmin(999999L, PageRequest.of(0, 20)))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(ErrorCode.CHALLENGE_NOT_FOUND);
  }
}

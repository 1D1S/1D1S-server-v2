package com.odos.odos_server_v2.domain.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import com.odos.odos_server_v2.domain.challenge.entity.Challenge;
import com.odos.odos_server_v2.domain.challenge.entity.Enum.ChallengeType;
import com.odos.odos_server_v2.domain.challenge.entity.Enum.ParticipationType;
import com.odos.odos_server_v2.domain.challenge.repository.ChallengeGoalRepository;
import com.odos.odos_server_v2.domain.challenge.repository.ChallengeLikeRepository;
import com.odos.odos_server_v2.domain.challenge.repository.ChallengePokeRepository;
import com.odos.odos_server_v2.domain.challenge.repository.ChallengeRepository;
import com.odos.odos_server_v2.domain.challenge.repository.FixedChallengeGoalRepository;
import com.odos.odos_server_v2.domain.challenge.repository.ParticipantRepository;
import com.odos.odos_server_v2.domain.challenge.service.ChallengeRankingService;
import com.odos.odos_server_v2.domain.challenge.service.ChallengeService;
import com.odos.odos_server_v2.domain.comment.repository.CommentRepository;
import com.odos.odos_server_v2.domain.diary.entity.Diary;
import com.odos.odos_server_v2.domain.diary.repository.DiaryGoalRepository;
import com.odos.odos_server_v2.domain.diary.repository.DiaryImageRepository;
import com.odos.odos_server_v2.domain.diary.repository.DiaryLikeRepository;
import com.odos.odos_server_v2.domain.diary.repository.DiaryReportRepository;
import com.odos.odos_server_v2.domain.diary.repository.DiaryRepository;
import com.odos.odos_server_v2.domain.diary.service.DiaryService;
import com.odos.odos_server_v2.domain.friend.repository.FriendRepository;
import com.odos.odos_server_v2.domain.member.entity.Enum.MemberRole;
import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.member.repository.MemberRepository;
import com.odos.odos_server_v2.domain.notification.service.NotificationService;
import com.odos.odos_server_v2.domain.security.jwt.MemberPrincipal;
import com.odos.odos_server_v2.domain.shared.dto.OffsetPagination;
import com.odos.odos_server_v2.domain.shared.service.CursorService;
import com.odos.odos_server_v2.domain.shared.service.ImageService;
import com.odos.odos_server_v2.exception.CustomException;
import com.odos.odos_server_v2.exception.ErrorCode;
import java.time.LocalDate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * 관리자의 비공개 챌린지 읽기 접근 회귀: 비공개 챌린지의 통계는 (a) 비참여 일반 사용자에게는 여전히 차단(PRIVATE_CHALLENGE), (b) 관리자에게는 허용된다.
 * 실 Postgres + 실 Flyway + 실 ChallengeService.
 */
@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AdminPrivateChallengeAccessPostgresTest {

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

  @Autowired ParticipantRepository participantRepository;
  @Autowired DiaryGoalRepository diaryGoalRepository;
  @Autowired DiaryRepository diaryRepository;
  @Autowired ChallengeRepository challengeRepository;
  @Autowired MemberRepository memberRepository;

  private ChallengeService service() {
    return new ChallengeService(
        participantRepository,
        mock(ChallengeLikeRepository.class),
        mock(ChallengeGoalRepository.class),
        mock(FixedChallengeGoalRepository.class),
        mock(ChallengePokeRepository.class),
        diaryGoalRepository,
        diaryRepository,
        mock(ImageService.class),
        challengeRepository,
        memberRepository,
        mock(CursorService.class),
        mock(NotificationService.class),
        mock(ChallengeRankingService.class),
        mock(PasswordEncoder.class));
  }

  private DiaryService diaryService() {
    return new DiaryService(
        diaryRepository,
        memberRepository,
        mock(DiaryLikeRepository.class),
        mock(DiaryReportRepository.class),
        challengeRepository,
        diaryGoalRepository,
        service(),
        mock(CursorService.class),
        participantRepository,
        mock(ImageService.class),
        mock(DiaryImageRepository.class),
        mock(CommentRepository.class),
        mock(NotificationService.class),
        mock(FriendRepository.class));
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

  private Member saveMember(String email, MemberRole role) {
    return memberRepository.save(Member.builder().email(email).role(role).build());
  }

  private Challenge savePrivateChallenge(Member host) {
    LocalDate today = LocalDate.now();
    return challengeRepository.save(
        Challenge.builder()
            .title("비공개 챌린지")
            .hostMember(host)
            .startDate(today.minusDays(3))
            .endDate(today.plusDays(30))
            .challengeType(ChallengeType.PRIVATE)
            .participationType(ParticipationType.GROUP)
            .maxParticipantsCnt(10L)
            .build());
  }

  /** (a) 비참여 일반 사용자는 비공개 챌린지 통계에서 여전히 차단된다. */
  @Test
  void statistics_onPrivateChallenge_blocksNonParticipantUser() {
    Member host = saveMember("host@t.com", MemberRole.USER);
    Member outsider = saveMember("outsider@t.com", MemberRole.USER);
    Challenge ch = savePrivateChallenge(host);

    assertThatThrownBy(() -> service().getChallengeStatistics(ch.getId(), outsider.getId()))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(ErrorCode.PRIVATE_CHALLENGE);
  }

  /** (b) 관리자는 비공개 챌린지 통계를 조회할 수 있다(참여자가 아니어도). */
  @Test
  void statistics_onPrivateChallenge_allowsAdmin() {
    Member host = saveMember("host2@t.com", MemberRole.USER);
    Member admin = saveMember("admin@t.com", MemberRole.ADMIN);
    Challenge ch = savePrivateChallenge(host);

    assertThatCode(() -> service().getChallengeStatistics(ch.getId(), admin.getId()))
        .doesNotThrowAnyException();
  }

  /** (b) 관리자는 비공개 챌린지 상세도 조회할 수 있다. */
  @Test
  void detail_onPrivateChallenge_allowsAdmin_butBlocksOutsider() {
    Member host = saveMember("host3@t.com", MemberRole.USER);
    Member admin = saveMember("admin3@t.com", MemberRole.ADMIN);
    Member outsider = saveMember("outsider3@t.com", MemberRole.USER);
    Challenge ch = savePrivateChallenge(host);

    assertThatCode(() -> service().getChallenge(ch.getId(), admin.getId()))
        .doesNotThrowAnyException();
    assertThatThrownBy(() -> service().getChallenge(ch.getId(), outsider.getId()))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(ErrorCode.PRIVATE_CHALLENGE);
  }

  /** 비공개 일지가 있는 비공개 챌린지: 관리자는 비공개 일지까지 전부 조회, 비참여자는 공개만. (DiaryService 경유) */
  @Test
  void challengeDiaries_adminSeesPrivate_outsiderSeesPublicOnly() {
    Member host = saveMember("host4@t.com", MemberRole.USER);
    Member admin = saveMember("admin4@t.com", MemberRole.ADMIN);
    Member outsider = saveMember("outsider4@t.com", MemberRole.USER);
    Challenge ch = savePrivateChallenge(host);

    diaryRepository.save(
        Diary.builder()
            .member(host)
            .challenge(ch)
            .isPublic(true)
            .completedDate(LocalDate.now())
            .build());
    diaryRepository.save(
        Diary.builder()
            .member(host)
            .challenge(ch)
            .isPublic(false)
            .completedDate(LocalDate.now())
            .build());

    DiaryService diaryService = diaryService();
    PageRequest pageable = PageRequest.of(0, 10);

    authenticateAs(admin);
    OffsetPagination<?> adminView = diaryService.getChallengeDiaries(ch.getId(), null, pageable);

    authenticateAs(outsider);
    OffsetPagination<?> outsiderView = diaryService.getChallengeDiaries(ch.getId(), null, pageable);

    // 관리자: 공개+비공개 2건, 비참여자: 공개 1건.
    assertThat(adminView.getItems()).hasSize(2);
    assertThat(outsiderView.getItems()).hasSize(1);
  }
}

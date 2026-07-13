package com.odos.odos_server_v2.domain.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import com.odos.odos_server_v2.domain.challenge.dto.ChallengeResponse;
import com.odos.odos_server_v2.domain.challenge.entity.Challenge;
import com.odos.odos_server_v2.domain.challenge.entity.Enum.ChallengeType;
import com.odos.odos_server_v2.domain.challenge.entity.Enum.ParticipantStatus;
import com.odos.odos_server_v2.domain.challenge.entity.Enum.ParticipationType;
import com.odos.odos_server_v2.domain.challenge.repository.ChallengeGoalRepository;
import com.odos.odos_server_v2.domain.challenge.repository.ChallengeLikeRepository;
import com.odos.odos_server_v2.domain.challenge.repository.ChallengePokeRepository;
import com.odos.odos_server_v2.domain.challenge.repository.ChallengeRepository;
import com.odos.odos_server_v2.domain.challenge.repository.FixedChallengeGoalRepository;
import com.odos.odos_server_v2.domain.challenge.repository.ParticipantRepository;
import com.odos.odos_server_v2.domain.challenge.service.ChallengeRankingService;
import com.odos.odos_server_v2.domain.challenge.service.ChallengeService;
import com.odos.odos_server_v2.domain.diary.repository.DiaryGoalRepository;
import com.odos.odos_server_v2.domain.diary.repository.DiaryRepository;
import com.odos.odos_server_v2.domain.member.entity.Enum.MemberRole;
import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.member.repository.MemberRepository;
import com.odos.odos_server_v2.domain.notification.service.NotificationService;
import com.odos.odos_server_v2.domain.shared.Enum.Category;
import com.odos.odos_server_v2.domain.shared.service.CursorService;
import com.odos.odos_server_v2.domain.shared.service.ImageService;
import com.odos.odos_server_v2.exception.CustomException;
import com.odos.odos_server_v2.exception.ErrorCode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * 챌린지 상세/통계 게스트(비로그인, memberId==null) 접근 계약을 실 Postgres + 실 Flyway + 실 ChallengeService 로 고정한다.
 *
 * <ul>
 *   <li>공개/공식(노출 중) 상세·통계: 200. 개인화 필드는 기본값(myStatus=NONE, likedByMe=false).
 *   <li>비공개(PRIVATE) 상세·통계: 403(PRIVATE_CHALLENGE).
 *   <li>예약 노출 전(visibleFrom 미래) 공식 상세: 404(CHALLENGE_NOT_FOUND).
 * </ul>
 */
@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class GuestChallengeDetailPostgresTest {

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

  private static final Long GUEST = null;

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

  private Member saveHost(String email) {
    return memberRepository.save(Member.builder().email(email).role(MemberRole.USER).build());
  }

  private Challenge save(Member host, ChallengeType type, LocalDateTime visibleFrom) {
    LocalDate today = LocalDate.now();
    return challengeRepository.save(
        Challenge.builder()
            .title("게스트 상세 테스트")
            .category(Category.DEV)
            .hostMember(host)
            .startDate(today.minusDays(1))
            .endDate(today.plusDays(30))
            .challengeType(type)
            .participationType(ParticipationType.GROUP)
            .maxParticipantsCnt(10L)
            .visibleFrom(visibleFrom)
            .build());
  }

  @Test
  void publicDetail_guestReadsWithDefaultPersonalizationFields() {
    Challenge ch = save(saveHost("pub-host@t.com"), ChallengeType.PUBLIC, null);

    ChallengeResponse res = service().getChallenge(ch.getId(), GUEST);

    assertThat(res.getChallengeSummary().getTitle()).isEqualTo("게스트 상세 테스트");
    assertThat(res.getChallengeSummary().getLikeInfo().isLikedByMe()).isFalse();
    assertThat(res.getChallengeDetail().getMyStatus()).isEqualTo(ParticipantStatus.NONE);
  }

  @Test
  void officialVisibleDetail_guestReads() {
    Challenge ch = save(saveHost("off-host@t.com"), ChallengeType.OFFICIAL, null);

    assertThatCode(() -> service().getChallenge(ch.getId(), GUEST)).doesNotThrowAnyException();
  }

  @Test
  void publicStatistics_guestReads() {
    Challenge ch = save(saveHost("stat-host@t.com"), ChallengeType.PUBLIC, null);

    assertThatCode(() -> service().getChallengeStatistics(ch.getId(), GUEST))
        .doesNotThrowAnyException();
  }

  @Test
  void privateDetail_guestBlockedWith403() {
    Challenge ch = save(saveHost("priv-host@t.com"), ChallengeType.PRIVATE, null);

    assertThatThrownBy(() -> service().getChallenge(ch.getId(), GUEST))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(ErrorCode.PRIVATE_CHALLENGE);
  }

  @Test
  void privateStatistics_guestBlockedWith403() {
    Challenge ch = save(saveHost("priv-stat-host@t.com"), ChallengeType.PRIVATE, null);

    assertThatThrownBy(() -> service().getChallengeStatistics(ch.getId(), GUEST))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(ErrorCode.PRIVATE_CHALLENGE);
  }

  @Test
  void reservedOfficialDetail_guestGets404BeforeVisibleFrom() {
    Challenge ch =
        save(
            saveHost("reserved-host@t.com"),
            ChallengeType.OFFICIAL,
            LocalDateTime.now().plusDays(3));

    assertThatThrownBy(() -> service().getChallenge(ch.getId(), GUEST))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(ErrorCode.CHALLENGE_NOT_FOUND);
  }
}

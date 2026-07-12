package com.odos.odos_server_v2.domain.banner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.odos.odos_server_v2.domain.banner.dto.BannerCreateRequest;
import com.odos.odos_server_v2.domain.banner.dto.BannerResponse;
import com.odos.odos_server_v2.domain.banner.repository.BannerRepository;
import com.odos.odos_server_v2.domain.banner.service.BannerService;
import com.odos.odos_server_v2.domain.member.entity.Enum.MemberRole;
import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.member.repository.MemberRepository;
import com.odos.odos_server_v2.domain.security.jwt.MemberPrincipal;
import com.odos.odos_server_v2.exception.CustomException;
import com.odos.odos_server_v2.exception.ErrorCode;
import java.time.LocalDate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(BannerService.class)
class BannerPostgresTest {

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

  @Autowired BannerRepository bannerRepository;
  @Autowired BannerService bannerService;
  @Autowired MemberRepository memberRepository;

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void adminCreatesBanner() {
    Member admin = saveMember("admin@t.com", MemberRole.ADMIN);
    authenticateAs(admin);

    BannerResponse created = bannerService.create(validRequest());

    assertThat(created.id()).isNotNull();
    assertThat(created.title()).isEqualTo("Summer Event");
    assertThat(created.subtitle()).isEqualTo("July benefit");
    assertThat(created.imageUrl()).isEqualTo("https://cdn.example.com/banner.png");
    assertThat(created.linkUrl()).isEqualTo("https://1day1streak.com/event/1");
    assertThat(created.startDate()).isEqualTo(LocalDate.of(2026, 7, 1));
    assertThat(created.endDate()).isEqualTo(LocalDate.of(2026, 7, 9));
    assertThat(created.createdAt()).isNotNull();
    assertThat(bannerRepository.findById(created.id())).isPresent();
  }

  @Test
  void userCannotCreateBanner() {
    Member user = saveMember("user@t.com", MemberRole.USER);
    authenticateAs(user);

    assertThatThrownBy(() -> bannerService.create(validRequest()))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(ErrorCode.MEMBER_NOT_ADMIN);
  }

  @Test
  void createRejectsMissingRequiredFields() {
    Member admin = saveMember("admin2@t.com", MemberRole.ADMIN);
    authenticateAs(admin);

    BannerCreateRequest request = validRequest();
    request.setImageUrl(" ");

    assertThatThrownBy(() -> bannerService.create(request))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(ErrorCode.BANNER_REQUIRED_FIELD_MISSING);
  }

  @Test
  void createRejectsInvalidPeriod() {
    Member admin = saveMember("admin3@t.com", MemberRole.ADMIN);
    authenticateAs(admin);

    BannerCreateRequest request = validRequest();
    request.setStartDate(LocalDate.of(2026, 7, 10));
    request.setEndDate(LocalDate.of(2026, 7, 1));

    assertThatThrownBy(() -> bannerService.create(request))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(ErrorCode.INVALID_BANNER_PERIOD);
  }

  private Member saveMember(String email, MemberRole role) {
    return memberRepository.save(Member.builder().email(email).role(role).build());
  }

  private void authenticateAs(Member member) {
    MemberPrincipal principal =
        new MemberPrincipal(member.getId(), member.getEmail(), member.getRole().name(), null);
    SecurityContextHolder.getContext()
        .setAuthentication(
            new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
  }

  private BannerCreateRequest validRequest() {
    BannerCreateRequest request = new BannerCreateRequest();
    request.setTitle("Summer Event");
    request.setSubtitle("July benefit");
    request.setImageUrl("https://cdn.example.com/banner.png");
    request.setLinkUrl("https://1day1streak.com/event/1");
    request.setStartDate(LocalDate.of(2026, 7, 1));
    request.setEndDate(LocalDate.of(2026, 7, 9));
    return request;
  }
}

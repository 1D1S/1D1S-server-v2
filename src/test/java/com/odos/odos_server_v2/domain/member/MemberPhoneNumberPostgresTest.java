package com.odos.odos_server_v2.domain.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.odos.odos_server_v2.domain.member.dto.MyPageDto;
import com.odos.odos_server_v2.domain.member.dto.PhoneNumberRequest;
import com.odos.odos_server_v2.domain.member.dto.SignupInfoRequest;
import com.odos.odos_server_v2.domain.member.entity.Enum.Gender;
import com.odos.odos_server_v2.domain.member.entity.Enum.Job;
import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.member.repository.MemberRepository;
import com.odos.odos_server_v2.domain.shared.Enum.Category;
import jakarta.persistence.EntityManager;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * 전화번호 수집 기능 계약 고정:
 *
 * <ul>
 *   <li>V39 마이그레이션이 실제 Postgres 에 적용되고 엔티티(phone_number)와 스키마가 validate 로 일치한다.
 *   <li>저장 시 하이픈이 제거되어 숫자만으로 정규화된다(가입·수정 공통 경로).
 *   <li>가입 요청 DTO 는 전화번호가 필수(@NotBlank)이며 한국 휴대폰 형식(@Pattern)만 허용한다.
 *   <li>본인 마이페이지 응답에는 phoneNumber 가 포함되고, 타인 조회 응답(값 미설정)에는 직렬화되지 않는다.
 * </ul>
 */
@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MemberPhoneNumberPostgresTest {

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
    r.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    r.add("spring.jpa.properties.hibernate.default_schema", () -> "odos_dev");
    r.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
    r.add("spring.flyway.enabled", () -> "true");
    r.add("spring.flyway.schemas", () -> "odos_dev");
    r.add("spring.flyway.default-schema", () -> "odos_dev");
    r.add("spring.flyway.create-schemas", () -> "true");
  }

  @Autowired MemberRepository memberRepository;
  @Autowired EntityManager em;

  @Test
  void persistsAndNormalizesPhoneNumber_onSignupAndEdit() {
    Member member = memberRepository.save(Member.builder().email("phone@t.com").build());

    // 가입 완료 경로: 하이픈 포함 입력 → 숫자만 저장.
    member.completeProfile(
        "닉네임", null, Job.STUDENT, LocalDate.of(2000, 1, 1), Gender.MALE, true, "010-1234-5678");
    em.flush();
    em.clear();
    assertThat(memberRepository.findById(member.getId()).orElseThrow().getPhoneNumber())
        .isEqualTo("01012345678");

    // 수정 경로: 하이픈 없는 입력도 그대로 숫자만 저장(재변경 가능).
    Member reloaded = memberRepository.findById(member.getId()).orElseThrow();
    reloaded.updatePhoneNumber("01099998888");
    em.flush();
    em.clear();
    assertThat(memberRepository.findById(member.getId()).orElseThrow().getPhoneNumber())
        .isEqualTo("01099998888");
  }

  @Test
  void signupRequest_requiresValidKoreanPhoneNumber() {
    try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
      Validator validator = factory.getValidator();

      // 필수: 미입력이면 phoneNumber 위반 발생.
      assertThat(phoneViolations(validator, signup(null))).isNotEmpty();
      assertThat(phoneViolations(validator, signup(""))).isNotEmpty();
      // 형식: 잘못된 번호는 위반.
      assertThat(phoneViolations(validator, signup("01012"))).isNotEmpty();
      assertThat(phoneViolations(validator, signup("02-123-4567"))).isNotEmpty();
      // 정상: 하이픈 유무 모두 허용, phoneNumber 위반 없음.
      assertThat(phoneViolations(validator, signup("010-1234-5678"))).isEmpty();
      assertThat(phoneViolations(validator, signup("01012345678"))).isEmpty();

      // 수정 요청 DTO 도 동일 규칙.
      PhoneNumberRequest bad = new PhoneNumberRequest();
      setField(bad, "phoneNumber", "123");
      assertThat(
              validator.validate(bad).stream()
                  .anyMatch(v -> v.getPropertyPath().toString().equals("phoneNumber")))
          .isTrue();
    }
  }

  @Test
  void myPageDto_serializesPhoneNumberOnlyWhenPresent() throws Exception {
    ObjectMapper mapper = new ObjectMapper();

    // 본인 조회: phoneNumber 포함.
    MyPageDto mine = MyPageDto.builder().nickname("me").phoneNumber("01012345678").build();
    assertThat(mapper.writeValueAsString(mine)).contains("phoneNumber");

    // 타인 조회: 값이 없으면(getOtherMyPage 는 세팅하지 않음) 키 자체가 직렬화되지 않는다.
    MyPageDto other = MyPageDto.builder().nickname("other").build();
    assertThat(mapper.writeValueAsString(other)).doesNotContain("phoneNumber");
  }

  @Test
  void enforcesUniquePhoneNumber_andAllowsMultipleNulls() {
    // NULL 다중 허용: 전화번호 없는 회원 여럿 저장 가능(Postgres 는 NULL 을 distinct 취급).
    memberRepository.save(Member.builder().email("null1@t.com").build());
    memberRepository.save(Member.builder().email("null2@t.com").build());
    em.flush();

    // 하이픈 포함 입력이 숫자만으로 정규화되어 저장되고, 정규화된 값으로 존재 조회가 가능하다.
    Member a = memberRepository.save(Member.builder().email("a@t.com").build());
    a.updatePhoneNumber("010-1111-2222");
    em.flush();
    em.clear();
    assertThat(memberRepository.existsByPhoneNumber("01011112222")).isTrue();
    // 본인 제외 조회: 본인 번호는 중복으로 보지 않는다.
    assertThat(memberRepository.existsByPhoneNumberAndIdNot("01011112222", a.getId())).isFalse();
    assertThat(memberRepository.existsByPhoneNumberAndIdNot("01011112222", a.getId() + 999))
        .isTrue();

    // DB 유니크 최종 방어선: 다른 회원이 동일 번호로 저장하면 무결성 위반 예외(Spring 변환).
    Member b = memberRepository.save(Member.builder().email("b@t.com").build());
    b.updatePhoneNumber("01011112222");
    assertThatThrownBy(() -> memberRepository.saveAndFlush(b))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  private static SignupInfoRequest signup(String phone) {
    return SignupInfoRequest.builder()
        .nickname("닉네임")
        .job(Job.STUDENT)
        .birth(LocalDate.of(2000, 1, 1))
        .gender(Gender.MALE)
        .isPublic(true)
        .category(List.of(Category.DEV))
        .phoneNumber(phone)
        .build();
  }

  private static Set<String> phoneViolations(Validator validator, SignupInfoRequest req) {
    return validator.validate(req).stream()
        .map(v -> v.getPropertyPath().toString())
        .filter("phoneNumber"::equals)
        .collect(Collectors.toSet());
  }

  private static void setField(Object target, String name, Object value) {
    try {
      var f = target.getClass().getDeclaredField(name);
      f.setAccessible(true);
      f.set(target, value);
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }
  }
}

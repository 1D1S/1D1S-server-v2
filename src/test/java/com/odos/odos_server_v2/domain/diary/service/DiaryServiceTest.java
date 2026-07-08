package com.odos.odos_server_v2.domain.diary.service;

import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.odos.odos_server_v2.domain.challenge.entity.Challenge;
import com.odos.odos_server_v2.domain.challenge.entity.ChallengeGoal;
import com.odos.odos_server_v2.domain.challenge.entity.Enum.GoalType;
import com.odos.odos_server_v2.domain.challenge.entity.Enum.ParticipantStatus;
import com.odos.odos_server_v2.domain.challenge.entity.Enum.ParticipationType;
import com.odos.odos_server_v2.domain.challenge.entity.Participant;
import com.odos.odos_server_v2.domain.challenge.repository.ChallengeGoalRepository;
import com.odos.odos_server_v2.domain.challenge.repository.ChallengeRepository;
import com.odos.odos_server_v2.domain.challenge.repository.ParticipantRepository;
import com.odos.odos_server_v2.domain.challenge.service.ChallengeService;
import com.odos.odos_server_v2.domain.diary.dto.DiaryGoalDto;
import com.odos.odos_server_v2.domain.diary.dto.DiaryRequest;
import com.odos.odos_server_v2.domain.diary.dto.DiaryResponse;
import com.odos.odos_server_v2.domain.diary.entity.DiaryGoal;
import com.odos.odos_server_v2.domain.diary.entity.Enum.Feeling;
import com.odos.odos_server_v2.domain.diary.repository.DiaryGoalRepository;
import com.odos.odos_server_v2.domain.diary.repository.DiaryRepository;
import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.member.repository.MemberRepository;
import com.odos.odos_server_v2.domain.shared.Enum.Category;
import com.odos.odos_server_v2.domain.shared.service.ImageService;
import com.odos.odos_server_v2.exception.CustomException;
import com.odos.odos_server_v2.exception.ErrorCode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DiaryServiceTest {

  @Autowired private DiaryService diaryService;

  @Autowired private MemberRepository memberRepository;
  @Autowired private ChallengeRepository challengeRepository;
  @Autowired private ParticipantRepository participantRepository;
  @Autowired private ChallengeGoalRepository challengeGoalRepository;
  @Autowired private DiaryRepository diaryRepository;
  @Autowired private DiaryGoalRepository diaryGoalRepository;
  @MockitoBean @Autowired private ImageService imageService;
  @MockitoBean @Autowired private ChallengeService challengeService;

  @Test
  void createDiaryWithFixedChallenge() {
    // given
    Member host = memberRepository.save(createMember("host@test.com", "host"));
    Member guest = memberRepository.save(createMember("guest@test.com", "guest"));

    Challenge fixedChallenge = challengeRepository.save(createFixedChallenge(host));

    Participant hostParticipant =
        participantRepository.save(createParticipant(host, fixedChallenge));

    Participant guestParticipant =
        participantRepository.save(createParticipant(guest, fixedChallenge));

    // 호스트도 fixed_challenge_goal 로부터 복제된 본인의 challenge_goal 을 가진다.
    challengeGoalRepository.save(createChallengeGoal("물 2L 마시기", hostParticipant));
    challengeGoalRepository.save(createChallengeGoal("운동 30분 하기", hostParticipant));

    // 게스트도 참여 시 fixed_challenge_goal 로부터 본인의 challenge_goal 로 복제받은 상태.
    ChallengeGoal guestGoal1 =
        challengeGoalRepository.save(createChallengeGoal("물 2L 마시기", guestParticipant));
    ChallengeGoal guestGoal2 =
        challengeGoalRepository.save(createChallengeGoal("운동 30분 하기", guestParticipant));

    when(imageService.getFileUrl(any())).thenReturn("https://test.com/profile.png");

    when(challengeService.toChallengeSummary(eq(fixedChallenge), eq(guest.getId())))
        .thenReturn(null);

    DiaryRequest request = new DiaryRequest();
    request.setChallengeId(fixedChallenge.getId());
    request.setAchievedDate(LocalDate.of(2026, 3, 19));
    request.setTitle("오늘 일지");
    request.setContent("열심히 했다");
    request.setFeeling(Feeling.HAPPY);
    request.setIsPublic(true);

    // guest 본인 목표 2개 중 1개만 체크했다고 가정
    request.setAchievedGoalIds(List.of(guestGoal1.getId()));

    // when
    DiaryResponse response = diaryService.createDiary(guest.getId(), request);

    // then 1) FIXED 챌린지의 일지는 호스트가 아닌 guest 본인의 challenge_goal 에 연결되어야 한다.
    Set<Long> expectedGuestGoalIds = Set.of(guestGoal1.getId(), guestGoal2.getId());

    // then 2) 실제 저장된 DiaryGoal 조회
    List<DiaryGoal> savedDiaryGoals = diaryGoalRepository.findAll();

    Set<Long> actualDiaryGoalChallengeGoalIds =
        savedDiaryGoals.stream().map(dg -> dg.getChallengeGoal().getId()).collect(toSet());

    assertEquals(expectedGuestGoalIds, actualDiaryGoalChallengeGoalIds);

    Set<Long> completedGoalIds =
        savedDiaryGoals.stream()
            .filter(dg -> Boolean.TRUE.equals(dg.getIsCompleted()))
            .map(dg -> dg.getChallengeGoal().getId())
            .collect(toSet());

    assertEquals(Set.of(guestGoal1.getId()), completedGoalIds);

    // then 3) 생성된 diary의 작성자는 guest여야 함
    assertTrue(
        savedDiaryGoals.stream()
            .allMatch(dg -> dg.getDiary().getMember().getId().equals(guest.getId())));

    // response도 guest 본인 목표 기준으로 내려가야 한다.
    Set<Long> responseGoalIds =
        response.getDiaryInfo().getDiaryGoal().stream()
            .map(DiaryGoalDto::getChallengeGoalId)
            .collect(toSet());
    assertEquals(expectedGuestGoalIds, responseGoalIds);
  }

  // presigned fileUrl 프리픽스. validateImageUrls 는 imageService.getFileUrl("") 를 허용 프리픽스로 쓴다.
  private static final String STORAGE_PREFIX = "https://test.com/";

  @Test
  void createDiary_setsImagesButLeavesThumbnailNullWhenNotSelected() {
    long guestId = setUpGuestFixture();
    stubImageService();

    DiaryRequest request = baseImageRequest();
    request.setImageUrls(List.of(STORAGE_PREFIX + "a.jpg", STORAGE_PREFIX + "b.jpg"));
    // thumbnailUrl 미지정 -> 대표 미선택, 이미지가 있어도 null 유지

    DiaryResponse response = diaryService.createDiary(guestId, request);

    assertEquals(List.of(STORAGE_PREFIX + "a.jpg", STORAGE_PREFIX + "b.jpg"), response.getImgUrl());
    assertNull(response.getThumbnailUrl());
  }

  @Test
  void createDiary_usesExplicitThumbnailWhenInList() {
    long guestId = setUpGuestFixture();
    stubImageService();

    DiaryRequest request = baseImageRequest();
    request.setImageUrls(List.of(STORAGE_PREFIX + "a.jpg", STORAGE_PREFIX + "b.jpg"));
    request.setThumbnailUrl(STORAGE_PREFIX + "b.jpg");

    DiaryResponse response = diaryService.createDiary(guestId, request);

    assertEquals(STORAGE_PREFIX + "b.jpg", response.getThumbnailUrl());
  }

  @Test
  void createDiary_throwsWhenThumbnailNotInImageUrls() {
    long guestId = setUpGuestFixture();
    stubImageService();

    DiaryRequest request = baseImageRequest();
    request.setImageUrls(List.of(STORAGE_PREFIX + "a.jpg"));
    request.setThumbnailUrl(STORAGE_PREFIX + "other.jpg");

    CustomException ex =
        assertThrows(CustomException.class, () -> diaryService.createDiary(guestId, request));
    assertEquals(ErrorCode.DIARY_INVALID_THUMBNAIL_URL, ex.getErrorCode());
  }

  @Test
  void createDiary_throwsWhenImageUrlNotFromOurStorage() {
    long guestId = setUpGuestFixture();
    stubImageService();

    DiaryRequest request = baseImageRequest();
    request.setImageUrls(List.of("https://evil.com/x.jpg"));

    CustomException ex =
        assertThrows(CustomException.class, () -> diaryService.createDiary(guestId, request));
    assertEquals(ErrorCode.DIARY_INVALID_IMAGE_URL, ex.getErrorCode());
  }

  @Test
  void updateDiary_nullImageUrlsKeepsExistingImages() {
    long guestId = setUpGuestFixture();
    stubImageService();

    DiaryRequest createRequest = baseImageRequest();
    createRequest.setImageUrls(List.of(STORAGE_PREFIX + "a.jpg"));
    Long diaryId = diaryService.createDiary(guestId, createRequest).getId();

    // imageUrls == null -> 이미지/썸네일 유지
    DiaryRequest updateRequest = baseImageRequest();
    updateRequest.setTitle("수정됨");
    DiaryResponse response = diaryService.updateDiary(guestId, diaryId, updateRequest);

    assertEquals(List.of(STORAGE_PREFIX + "a.jpg"), response.getImgUrl());
    assertEquals(STORAGE_PREFIX + "a.jpg", response.getThumbnailUrl());
  }

  @Test
  void updateDiary_emptyImageUrlsClearsImagesAndThumbnail() {
    long guestId = setUpGuestFixture();
    stubImageService();

    DiaryRequest createRequest = baseImageRequest();
    createRequest.setImageUrls(List.of(STORAGE_PREFIX + "a.jpg"));
    Long diaryId = diaryService.createDiary(guestId, createRequest).getId();

    // imageUrls == [] -> 전부 삭제, 썸네일 null
    DiaryRequest updateRequest = baseImageRequest();
    updateRequest.setImageUrls(List.of());
    DiaryResponse response = diaryService.updateDiary(guestId, diaryId, updateRequest);

    assertTrue(response.getImgUrl().isEmpty());
    assertNull(response.getThumbnailUrl());
  }

  // getFileUrl("") -> 허용 프리픽스, 그 외 인자 -> 프로필 URL 로 스텁.
  // "" 스텁을 any() 뒤에 선언해 "" 인자에 대해서는 프리픽스가 우선하도록 한다.
  private void stubImageService() {
    when(imageService.getFileUrl(any())).thenReturn("https://test.com/profile.png");
    when(imageService.getFileUrl("")).thenReturn(STORAGE_PREFIX);
  }

  // host/guest/고정챌린지/참여자/목표 1개 세팅 후 guestId 반환. challengeId 는 필드에 보관.
  private long setUpGuestFixture() {
    Member host = memberRepository.save(createMember("host@test.com", "host"));
    Member guest = memberRepository.save(createMember("guest@test.com", "guest"));
    Challenge fixedChallenge = challengeRepository.save(createFixedChallenge(host));
    Participant guestParticipant =
        participantRepository.save(createParticipant(guest, fixedChallenge));
    challengeGoalRepository.save(createChallengeGoal("물 2L 마시기", guestParticipant));
    this.fixtureChallengeId = fixedChallenge.getId();
    return guest.getId();
  }

  private Long fixtureChallengeId;

  private DiaryRequest baseImageRequest() {
    DiaryRequest request = new DiaryRequest();
    request.setChallengeId(fixtureChallengeId);
    request.setAchievedDate(LocalDate.of(2026, 3, 19));
    request.setTitle("일지");
    request.setContent("내용");
    request.setFeeling(Feeling.HAPPY);
    request.setIsPublic(true);
    return request;
  }

  private Member createMember(String email, String nickname) {
    return Member.builder().email(email).nickname(nickname).profileUrl("profile.png").build();
  }

  private Challenge createFixedChallenge(Member host) {
    return Challenge.builder()
        .title("고정 목표 챌린지")
        .category(Category.EXERCISE) // 실제 값으로 수정
        .startDate(LocalDate.of(2026, 3, 1))
        .endDate(LocalDate.of(2026, 3, 31))
        .maxParticipantsCnt(10L)
        .goalType(GoalType.FIXED)
        .participationType(ParticipationType.GROUP)
        .description("host 목표 기준으로 진행")
        .hostMember(host)
        .participants(new ArrayList<>())
        .likes(new ArrayList<>())
        .diaries(new ArrayList<>())
        .build();
  }

  private Participant createParticipant(Member member, Challenge challenge) {
    return Participant.builder()
        .member(member)
        .challenge(challenge)
        .status(ParticipantStatus.PARTICIPANT)
        .challengeGoals(new ArrayList<>())
        .build();
  }

  private ChallengeGoal createChallengeGoal(String content, Participant participant) {
    return ChallengeGoal.builder()
        .content(content)
        .participant(participant)
        .diaryGoals(new ArrayList<>())
        .build();
  }
}

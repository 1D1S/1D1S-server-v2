package com.odos.odos_server_v2.domain.diary.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.odos.odos_server_v2.domain.challenge.entity.ChallengeGoal;
import com.odos.odos_server_v2.domain.diary.entity.DiaryGoal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class DiaryGoalDtoTest {

  @Nested
  @DisplayName("Builder tests")
  class BuilderTests {

    @Test
    @DisplayName("Should create DiaryGoalDto with all fields using builder")
    void shouldCreateDiaryGoalDtoWithAllFields() {
      // given
      Long expectedId = 1L;
      String expectedName = "Test Goal";
      Boolean expectedIsAchieved = true;

      // when
      DiaryGoalDto dto =
          DiaryGoalDto.builder()
              .challengeGoalId(expectedId)
              .challengeGoalName(expectedName)
              .isAchieved(expectedIsAchieved)
              .build();

      // then
      assertThat(dto).isNotNull();
      assertThat(dto.getChallengeGoalId()).isEqualTo(expectedId);
      assertThat(dto.getChallengeGoalName()).isEqualTo(expectedName);
      assertThat(dto.getIsAchieved()).isEqualTo(expectedIsAchieved);
    }

    @Test
    @DisplayName("Should create DiaryGoalDto with null fields")
    void shouldCreateDiaryGoalDtoWithNullFields() {
      // when
      DiaryGoalDto dto =
          DiaryGoalDto.builder()
              .challengeGoalId(null)
              .challengeGoalName(null)
              .isAchieved(null)
              .build();

      // then
      assertThat(dto).isNotNull();
      assertThat(dto.getChallengeGoalId()).isNull();
      assertThat(dto.getChallengeGoalName()).isNull();
      assertThat(dto.getIsAchieved()).isNull();
    }

    @Test
    @DisplayName("Should create DiaryGoalDto with partial fields")
    void shouldCreateDiaryGoalDtoWithPartialFields() {
      // given
      Long expectedId = 2L;

      // when
      DiaryGoalDto dto = DiaryGoalDto.builder().challengeGoalId(expectedId).build();

      // then
      assertThat(dto).isNotNull();
      assertThat(dto.getChallengeGoalId()).isEqualTo(expectedId);
      assertThat(dto.getChallengeGoalName()).isNull();
      assertThat(dto.getIsAchieved()).isNull();
    }
  }

  @Nested
  @DisplayName("from() factory method tests")
  class FromMethodTests {

    @Test
    @DisplayName("Should correctly map DiaryGoal to DiaryGoalDto with all fields")
    void shouldMapDiaryGoalToDtoWithAllFields() {
      // given
      Long expectedChallengeGoalId = 10L;
      String expectedChallengeGoalContent = "Complete 10 exercises";
      Boolean expectedIsCompleted = true;

      ChallengeGoal challengeGoal = mock(ChallengeGoal.class);
      when(challengeGoal.getId()).thenReturn(expectedChallengeGoalId);
      when(challengeGoal.getContent()).thenReturn(expectedChallengeGoalContent);

      DiaryGoal diaryGoal = mock(DiaryGoal.class);
      when(diaryGoal.getChallengeGoal()).thenReturn(challengeGoal);
      when(diaryGoal.getIsCompleted()).thenReturn(expectedIsCompleted);

      // when
      DiaryGoalDto dto = DiaryGoalDto.from(diaryGoal);

      // then
      assertThat(dto).isNotNull();
      assertThat(dto.getChallengeGoalId()).isEqualTo(expectedChallengeGoalId);
      assertThat(dto.getChallengeGoalName()).isEqualTo(expectedChallengeGoalContent);
      assertThat(dto.getIsAchieved()).isEqualTo(expectedIsCompleted);
    }

    @Test
    @DisplayName("Should map DiaryGoal with isCompleted false")
    void shouldMapDiaryGoalWithIsCompletedFalse() {
      // given
      Long expectedChallengeGoalId = 20L;
      String expectedChallengeGoalContent = "Read 5 books";
      Boolean expectedIsCompleted = false;

      ChallengeGoal challengeGoal = mock(ChallengeGoal.class);
      when(challengeGoal.getId()).thenReturn(expectedChallengeGoalId);
      when(challengeGoal.getContent()).thenReturn(expectedChallengeGoalContent);

      DiaryGoal diaryGoal = mock(DiaryGoal.class);
      when(diaryGoal.getChallengeGoal()).thenReturn(challengeGoal);
      when(diaryGoal.getIsCompleted()).thenReturn(expectedIsCompleted);

      // when
      DiaryGoalDto dto = DiaryGoalDto.from(diaryGoal);

      // then
      assertThat(dto).isNotNull();
      assertThat(dto.getChallengeGoalId()).isEqualTo(expectedChallengeGoalId);
      assertThat(dto.getChallengeGoalName()).isEqualTo(expectedChallengeGoalContent);
      assertThat(dto.getIsAchieved()).isFalse();
    }

    @Test
    @DisplayName("Should handle ChallengeGoal with null content")
    void shouldHandleChallengeGoalWithNullContent() {
      // given
      Long expectedChallengeGoalId = 30L;

      ChallengeGoal challengeGoal = mock(ChallengeGoal.class);
      when(challengeGoal.getId()).thenReturn(expectedChallengeGoalId);
      when(challengeGoal.getContent()).thenReturn(null);

      DiaryGoal diaryGoal = mock(DiaryGoal.class);
      when(diaryGoal.getChallengeGoal()).thenReturn(challengeGoal);
      when(diaryGoal.getIsCompleted()).thenReturn(true);

      // when
      DiaryGoalDto dto = DiaryGoalDto.from(diaryGoal);

      // then
      assertThat(dto).isNotNull();
      assertThat(dto.getChallengeGoalId()).isEqualTo(expectedChallengeGoalId);
      assertThat(dto.getChallengeGoalName()).isNull();
      assertThat(dto.getIsAchieved()).isTrue();
    }

    @Test
    @DisplayName("Should handle ChallengeGoal with null id")
    void shouldHandleChallengeGoalWithNullId() {
      // given
      String expectedChallengeGoalContent = "Empty ID Goal";

      ChallengeGoal challengeGoal = mock(ChallengeGoal.class);
      when(challengeGoal.getId()).thenReturn(null);
      when(challengeGoal.getContent()).thenReturn(expectedChallengeGoalContent);

      DiaryGoal diaryGoal = mock(DiaryGoal.class);
      when(diaryGoal.getChallengeGoal()).thenReturn(challengeGoal);
      when(diaryGoal.getIsCompleted()).thenReturn(false);

      // when
      DiaryGoalDto dto = DiaryGoalDto.from(diaryGoal);

      // then
      assertThat(dto).isNotNull();
      assertThat(dto.getChallengeGoalId()).isNull();
      assertThat(dto.getChallengeGoalName()).isEqualTo(expectedChallengeGoalContent);
      assertThat(dto.getIsAchieved()).isFalse();
    }

    @Test
    @DisplayName("Should throw NullPointerException when DiaryGoal is null")
    void shouldThrowNullPointerExceptionWhenDiaryGoalIsNull() {
      // when & then
      assertThatThrownBy(() -> DiaryGoalDto.from(null))
          .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Should throw NullPointerException when ChallengeGoal is null")
    void shouldThrowNullPointerExceptionWhenChallengeGoalIsNull() {
      // given
      DiaryGoal diaryGoal = mock(DiaryGoal.class);
      when(diaryGoal.getChallengeGoal()).thenReturn(null);
      when(diaryGoal.getIsCompleted()).thenReturn(true);

      // when & then
      assertThatThrownBy(() -> DiaryGoalDto.from(diaryGoal))
          .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Should map empty string content correctly")
    void shouldMapEmptyStringContent() {
      // given
      Long expectedChallengeGoalId = 40L;
      String expectedChallengeGoalContent = "";

      ChallengeGoal challengeGoal = mock(ChallengeGoal.class);
      when(challengeGoal.getId()).thenReturn(expectedChallengeGoalId);
      when(challengeGoal.getContent()).thenReturn(expectedChallengeGoalContent);

      DiaryGoal diaryGoal = mock(DiaryGoal.class);
      when(diaryGoal.getChallengeGoal()).thenReturn(challengeGoal);
      when(diaryGoal.getIsCompleted()).thenReturn(true);

      // when
      DiaryGoalDto dto = DiaryGoalDto.from(diaryGoal);

      // then
      assertThat(dto).isNotNull();
      assertThat(dto.getChallengeGoalId()).isEqualTo(expectedChallengeGoalId);
      assertThat(dto.getChallengeGoalName()).isEmpty();
      assertThat(dto.getIsAchieved()).isTrue();
    }

    @Test
    @DisplayName("Should map very long content correctly")
    void shouldMapVeryLongContent() {
      // given
      Long expectedChallengeGoalId = 50L;
      String expectedChallengeGoalContent = "A".repeat(1000); // Very long string

      ChallengeGoal challengeGoal = mock(ChallengeGoal.class);
      when(challengeGoal.getId()).thenReturn(expectedChallengeGoalId);
      when(challengeGoal.getContent()).thenReturn(expectedChallengeGoalContent);

      DiaryGoal diaryGoal = mock(DiaryGoal.class);
      when(diaryGoal.getChallengeGoal()).thenReturn(challengeGoal);
      when(diaryGoal.getIsCompleted()).thenReturn(false);

      // when
      DiaryGoalDto dto = DiaryGoalDto.from(diaryGoal);

      // then
      assertThat(dto).isNotNull();
      assertThat(dto.getChallengeGoalId()).isEqualTo(expectedChallengeGoalId);
      assertThat(dto.getChallengeGoalName()).isEqualTo(expectedChallengeGoalContent);
      assertThat(dto.getChallengeGoalName()).hasSize(1000);
      assertThat(dto.getIsAchieved()).isFalse();
    }

    @Test
    @DisplayName("Should map special characters in content correctly")
    void shouldMapSpecialCharactersInContent() {
      // given
      Long expectedChallengeGoalId = 60L;
      String expectedChallengeGoalContent = "Goal with 한글, emoji 😀, symbols !@#$%^&*()";

      ChallengeGoal challengeGoal = mock(ChallengeGoal.class);
      when(challengeGoal.getId()).thenReturn(expectedChallengeGoalId);
      when(challengeGoal.getContent()).thenReturn(expectedChallengeGoalContent);

      DiaryGoal diaryGoal = mock(DiaryGoal.class);
      when(diaryGoal.getChallengeGoal()).thenReturn(challengeGoal);
      when(diaryGoal.getIsCompleted()).thenReturn(true);

      // when
      DiaryGoalDto dto = DiaryGoalDto.from(diaryGoal);

      // then
      assertThat(dto).isNotNull();
      assertThat(dto.getChallengeGoalId()).isEqualTo(expectedChallengeGoalId);
      assertThat(dto.getChallengeGoalName()).isEqualTo(expectedChallengeGoalContent);
      assertThat(dto.getIsAchieved()).isTrue();
    }
  }

  @Nested
  @DisplayName("Getter tests")
  class GetterTests {

    @Test
    @DisplayName("Should access all getters without errors")
    void shouldAccessAllGetters() {
      // given
      DiaryGoalDto dto =
          DiaryGoalDto.builder()
              .challengeGoalId(100L)
              .challengeGoalName("Test")
              .isAchieved(true)
              .build();

      // when & then
      assertThat(dto.getChallengeGoalId()).isEqualTo(100L);
      assertThat(dto.getChallengeGoalName()).isEqualTo("Test");
      assertThat(dto.getIsAchieved()).isTrue();
    }

    @Test
    @DisplayName("Should return null for unset fields")
    void shouldReturnNullForUnsetFields() {
      // given
      DiaryGoalDto dto = DiaryGoalDto.builder().build();

      // when & then
      assertThat(dto.getChallengeGoalId()).isNull();
      assertThat(dto.getChallengeGoalName()).isNull();
      assertThat(dto.getIsAchieved()).isNull();
    }
  }

  @Nested
  @DisplayName("Boundary and edge case tests")
  class BoundaryTests {

    @Test
    @DisplayName("Should handle maximum Long value for challengeGoalId")
    void shouldHandleMaxLongValue() {
      // given
      Long maxLongValue = Long.MAX_VALUE;
      ChallengeGoal challengeGoal = mock(ChallengeGoal.class);
      when(challengeGoal.getId()).thenReturn(maxLongValue);
      when(challengeGoal.getContent()).thenReturn("Max ID Goal");

      DiaryGoal diaryGoal = mock(DiaryGoal.class);
      when(diaryGoal.getChallengeGoal()).thenReturn(challengeGoal);
      when(diaryGoal.getIsCompleted()).thenReturn(true);

      // when
      DiaryGoalDto dto = DiaryGoalDto.from(diaryGoal);

      // then
      assertThat(dto.getChallengeGoalId()).isEqualTo(maxLongValue);
    }

    @Test
    @DisplayName("Should handle minimum Long value for challengeGoalId")
    void shouldHandleMinLongValue() {
      // given
      Long minLongValue = Long.MIN_VALUE;
      ChallengeGoal challengeGoal = mock(ChallengeGoal.class);
      when(challengeGoal.getId()).thenReturn(minLongValue);
      when(challengeGoal.getContent()).thenReturn("Min ID Goal");

      DiaryGoal diaryGoal = mock(DiaryGoal.class);
      when(diaryGoal.getChallengeGoal()).thenReturn(challengeGoal);
      when(diaryGoal.getIsCompleted()).thenReturn(false);

      // when
      DiaryGoalDto dto = DiaryGoalDto.from(diaryGoal);

      // then
      assertThat(dto.getChallengeGoalId()).isEqualTo(minLongValue);
    }

    @Test
    @DisplayName("Should handle zero as challengeGoalId")
    void shouldHandleZeroAsChallengeGoalId() {
      // given
      Long zeroValue = 0L;
      ChallengeGoal challengeGoal = mock(ChallengeGoal.class);
      when(challengeGoal.getId()).thenReturn(zeroValue);
      when(challengeGoal.getContent()).thenReturn("Zero ID Goal");

      DiaryGoal diaryGoal = mock(DiaryGoal.class);
      when(diaryGoal.getChallengeGoal()).thenReturn(challengeGoal);
      when(diaryGoal.getIsCompleted()).thenReturn(true);

      // when
      DiaryGoalDto dto = DiaryGoalDto.from(diaryGoal);

      // then
      assertThat(dto.getChallengeGoalId()).isZero();
    }

    @Test
    @DisplayName("Should handle content with whitespace only")
    void shouldHandleWhitespaceOnlyContent() {
      // given
      String whitespaceContent = "   \t\n   ";
      ChallengeGoal challengeGoal = mock(ChallengeGoal.class);
      when(challengeGoal.getId()).thenReturn(70L);
      when(challengeGoal.getContent()).thenReturn(whitespaceContent);

      DiaryGoal diaryGoal = mock(DiaryGoal.class);
      when(diaryGoal.getChallengeGoal()).thenReturn(challengeGoal);
      when(diaryGoal.getIsCompleted()).thenReturn(false);

      // when
      DiaryGoalDto dto = DiaryGoalDto.from(diaryGoal);

      // then
      assertThat(dto.getChallengeGoalName()).isEqualTo(whitespaceContent);
    }
  }
}
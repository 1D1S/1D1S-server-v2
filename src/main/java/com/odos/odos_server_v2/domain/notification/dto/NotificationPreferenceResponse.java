package com.odos.odos_server_v2.domain.notification.dto;

import com.odos.odos_server_v2.domain.notification.entity.NotificationPreference;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationPreferenceResponse {
  private Boolean pushEnabled;
  private Boolean friendEnabled;
  private Boolean diaryEnabled;
  private Boolean challengeEnabled;

  public static NotificationPreferenceResponse from(NotificationPreference preference) {
    return NotificationPreferenceResponse.builder()
        .pushEnabled(preference.getPushEnabled())
        .friendEnabled(preference.getFriendEnabled())
        .diaryEnabled(preference.getDiaryEnabled())
        .challengeEnabled(preference.getChallengeEnabled())
        .build();
  }
}

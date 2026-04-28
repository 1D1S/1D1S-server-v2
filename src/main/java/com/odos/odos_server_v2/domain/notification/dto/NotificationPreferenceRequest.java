package com.odos.odos_server_v2.domain.notification.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class NotificationPreferenceRequest {
  private Boolean pushEnabled;
  private Boolean friendEnabled;
  private Boolean diaryEnabled;
  private Boolean challengeEnabled;
}

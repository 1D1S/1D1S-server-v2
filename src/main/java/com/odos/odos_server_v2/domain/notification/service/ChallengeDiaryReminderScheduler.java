package com.odos.odos_server_v2.domain.notification.service;

import com.odos.odos_server_v2.domain.challenge.entity.Enum.ParticipantStatus;
import com.odos.odos_server_v2.domain.challenge.entity.Participant;
import com.odos.odos_server_v2.domain.challenge.repository.ParticipantRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChallengeDiaryReminderScheduler {

  private final ParticipantRepository participantRepository;
  private final NotificationService notificationService;

  @Scheduled(cron = "0 30 18 * * *", zone = "Asia/Seoul")
  public void sendChallengeDiaryReminders() {
    LocalDate today = notificationService.todayInSeoul();
    List<Participant> targets =
        participantRepository.findTodayDiaryReminderTargets(
            List.of(ParticipantStatus.HOST, ParticipantStatus.PARTICIPANT), today);

    for (Participant target : targets) {
      notificationService.notifyChallengeDiaryReminder(
          target.getMember().getId(),
          target.getChallenge().getId(),
          target.getChallenge().getTitle(),
          today);
    }
  }
}

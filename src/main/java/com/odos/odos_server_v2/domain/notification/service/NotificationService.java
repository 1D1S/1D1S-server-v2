package com.odos.odos_server_v2.domain.notification.service;

import com.odos.odos_server_v2.config.WebPushProperties;
import com.odos.odos_server_v2.domain.diary.entity.Diary;
import com.odos.odos_server_v2.domain.diary.repository.DiaryRepository;
import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.member.repository.MemberRepository;
import com.odos.odos_server_v2.domain.notification.dto.NotificationEndpointDeleteRequest;
import com.odos.odos_server_v2.domain.notification.dto.NotificationEndpointResponse;
import com.odos.odos_server_v2.domain.notification.dto.NotificationEndpointUpsertRequest;
import com.odos.odos_server_v2.domain.notification.dto.NotificationPreferenceRequest;
import com.odos.odos_server_v2.domain.notification.dto.NotificationPreferenceResponse;
import com.odos.odos_server_v2.domain.notification.dto.NotificationResponse;
import com.odos.odos_server_v2.domain.notification.dto.UnreadCountResponse;
import com.odos.odos_server_v2.domain.notification.dto.WebPushPublicKeyResponse;
import com.odos.odos_server_v2.domain.notification.entity.DiaryLikeMilestoneState;
import com.odos.odos_server_v2.domain.notification.entity.Enum.NotificationCategory;
import com.odos.odos_server_v2.domain.notification.entity.Enum.NotificationTargetType;
import com.odos.odos_server_v2.domain.notification.entity.Enum.NotificationType;
import com.odos.odos_server_v2.domain.notification.entity.Notification;
import com.odos.odos_server_v2.domain.notification.entity.NotificationEndpoint;
import com.odos.odos_server_v2.domain.notification.entity.NotificationEvent;
import com.odos.odos_server_v2.domain.notification.entity.NotificationPreference;
import com.odos.odos_server_v2.domain.notification.repository.DiaryLikeMilestoneStateRepository;
import com.odos.odos_server_v2.domain.notification.repository.NotificationEndpointRepository;
import com.odos.odos_server_v2.domain.notification.repository.NotificationEventRepository;
import com.odos.odos_server_v2.domain.notification.repository.NotificationPreferenceRepository;
import com.odos.odos_server_v2.domain.notification.repository.NotificationRepository;
import com.odos.odos_server_v2.domain.shared.dto.OffsetPagination;
import com.odos.odos_server_v2.domain.shared.service.ImageService;
import com.odos.odos_server_v2.exception.CustomException;
import com.odos.odos_server_v2.exception.ErrorCode;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

  private static final int MAX_NOTIFICATIONS_PER_USER = 100;
  private static final int RETENTION_DAYS = 30;

  private final NotificationRepository notificationRepository;
  private final NotificationEventRepository notificationEventRepository;
  private final NotificationPreferenceRepository preferenceRepository;
  private final DiaryLikeMilestoneStateRepository milestoneStateRepository;
  private final MemberRepository memberRepository;
  private final DiaryRepository diaryRepository;
  private final ImageService imageService;
  private final NotificationEndpointRepository notificationEndpointRepository;
  private final NotificationDispatchService notificationDispatchService;
  private final WebPushProperties webPushProperties;

  @Transactional
  public OffsetPagination<NotificationResponse> getMyNotifications(
      Long memberId, int page, int size) {
    Member receiver = getMember(memberId);
    Pageable pageable = PageRequest.of(page, size);

    Page<NotificationResponse> responsePage =
        notificationRepository
            .findByReceiverOrderByCreatedAtDesc(receiver, pageable)
            .map(
                notification ->
                    NotificationResponse.from(
                        notification,
                        notification.getResolvedActor() == null
                            ? null
                            : imageService.getFileUrl(
                                notification.getResolvedActor().getProfileUrl())));

    return OffsetPagination.from(responsePage);
  }

  @Transactional
  public void markAsRead(Long memberId, Long notificationId) {
    Notification notification =
        notificationRepository
            .findById(notificationId)
            .orElseThrow(() -> new CustomException(ErrorCode.NOTIFICATION_NOT_FOUND));

    if (!notification.getReceiver().getId().equals(memberId)) {
      throw new CustomException(ErrorCode.NOTIFICATION_NOT_ACCESS);
    }

    if (!notification.getIsRead()) {
      notification.markAsRead();
    }
  }

  @Transactional
  public int markAllAsRead(Long memberId) {
    Member receiver = getMember(memberId);
    return notificationRepository.markAllAsRead(receiver, LocalDateTime.now());
  }

  @Transactional
  public UnreadCountResponse getUnreadCount(Long memberId) {
    Member receiver = getMember(memberId);
    return new UnreadCountResponse(notificationRepository.countByReceiverAndIsReadFalse(receiver));
  }

  public WebPushPublicKeyResponse getWebPushPublicKey() {
    return new WebPushPublicKeyResponse(webPushProperties.getPublicKey());
  }

  @Transactional
  public NotificationPreferenceResponse getPreference(Long memberId) {
    Member member = getMember(memberId);
    NotificationPreference preference =
        preferenceRepository
            .findByMember(member)
            .orElseGet(
                () ->
                    preferenceRepository.save(
                        NotificationPreference.builder().member(member).build()));

    return NotificationPreferenceResponse.from(preference);
  }

  @Transactional
  public NotificationPreferenceResponse updatePreference(
      Long memberId, NotificationPreferenceRequest request) {
    Member member = getMember(memberId);
    NotificationPreference preference =
        preferenceRepository
            .findByMember(member)
            .orElseGet(() -> NotificationPreference.builder().member(member).build());

    preference.update(
        request.getPushEnabled() == null ? preference.getPushEnabled() : request.getPushEnabled(),
        request.getFriendEnabled() == null
            ? preference.getFriendEnabled()
            : request.getFriendEnabled(),
        request.getDiaryEnabled() == null
            ? preference.getDiaryEnabled()
            : request.getDiaryEnabled(),
        request.getChallengeEnabled() == null
            ? preference.getChallengeEnabled()
            : request.getChallengeEnabled());

    NotificationPreference saved = preferenceRepository.save(preference);
    return NotificationPreferenceResponse.from(saved);
  }

  @Transactional
  public void notifyFriendRequest(Long actorId, Long receiverId, String actorNickname) {
    Member actor = getMember(actorId);
    Member receiver = getMember(receiverId);

    createNotification(
        receiver,
        actor,
        NotificationCategory.FRIEND,
        NotificationType.FRIEND_REQUEST,
        String.format("%s님이 친구 신청을 보냈어요.", actorNickname),
        NotificationTargetType.MEMBER_PROFILE,
        actor.getId(),
        null);
  }

  @Transactional
  public void notifyFriendAccept(Long actorId, Long receiverId, String actorNickname) {
    Member actor = getMember(actorId);
    Member receiver = getMember(receiverId);

    createNotification(
        receiver,
        actor,
        NotificationCategory.FRIEND,
        NotificationType.FRIEND_ACCEPT,
        String.format("%s님이 친구 신청을 수락했습니다. 이제 일지를 확인해보세요!", actorNickname),
        NotificationTargetType.MEMBER_PROFILE,
        actor.getId(),
        null);
  }

  @Transactional
  public void notifyFriendDiaryCreated(
      Long actorId, List<Long> receiverIds, Long diaryId, String actorNickname, String diaryTitle) {
    Member actor = getMember(actorId);

    for (Long receiverId : receiverIds) {
      if (receiverId.equals(actorId)) {
        continue;
      }
      Member receiver = getMember(receiverId);
      createNotification(
          receiver,
          actor,
          NotificationCategory.DIARY,
          NotificationType.FRIEND_DIARY_CREATED,
          String.format("%s님이 일지를 등록했어요: %s", actorNickname, diaryTitle),
          NotificationTargetType.DIARY_DETAIL,
          diaryId,
          null);
    }
  }

  @Transactional
  public void notifyMyDiaryCommented(
      Long actorId, Long receiverId, Long diaryId, String actorNickname, String commentContent) {
    Member actor = getMember(actorId);
    Member receiver = getMember(receiverId);

    createNotification(
        receiver,
        actor,
        NotificationCategory.DIARY,
        NotificationType.MY_DIARY_COMMENTED,
        String.format("%s님이 댓글을 달았습니다: %s", actorNickname, commentContent),
        NotificationTargetType.DIARY_COMMENT,
        diaryId,
        null);
  }

  @Transactional
  public void notifyMyCommentReplied(
      Long actorId, Long receiverId, Long diaryId, String actorNickname, String commentContent) {
    Member actor = getMember(actorId);
    Member receiver = getMember(receiverId);

    createNotification(
        receiver,
        actor,
        NotificationCategory.DIARY,
        NotificationType.MY_COMMENT_REPLIED,
        String.format("%s님이 내 댓글에 답글을 남겼습니다: %s", actorNickname, commentContent),
        NotificationTargetType.DIARY_COMMENT,
        diaryId,
        null);
  }

  @Transactional
  public void notifyChallengeApproved(Long receiverId, Long challengeId, String challengeName) {
    Member receiver = getMember(receiverId);

    createNotification(
        receiver,
        null,
        NotificationCategory.CHALLENGE,
        NotificationType.CHALLENGE_APPROVED,
        String.format("%s 챌린지원이 되었습니다! 열심히 참여해봐요!", challengeName),
        NotificationTargetType.CHALLENGE_DETAIL,
        challengeId,
        null);
  }

  @Transactional
  public void notifyChallengeRejected(Long receiverId, Long challengeId, String challengeName) {
    Member receiver = getMember(receiverId);

    createNotification(
        receiver,
        null,
        NotificationCategory.CHALLENGE,
        NotificationType.CHALLENGE_REJECTED,
        String.format("%s 챌린지 참여가 거절되었습니다.", challengeName),
        NotificationTargetType.CHALLENGE_LIST,
        challengeId,
        null);
  }

  @Transactional
  public boolean notifyDiaryLikeMilestone(Long diaryId, int currentLikeCount) {
    Diary diary =
        diaryRepository
            .findByIdAndIsDeletedFalse(diaryId)
            .orElseThrow(() -> new CustomException(ErrorCode.DIARY_NOT_FOUND));

    int milestone = resolveMilestone(currentLikeCount);
    if (milestone == 0) {
      return false;
    }

    DiaryLikeMilestoneState state =
        milestoneStateRepository
            .findByDiary(diary)
            .orElseGet(
                () ->
                    milestoneStateRepository.save(
                        DiaryLikeMilestoneState.builder().diary(diary).build()));

    if (milestone <= state.getLastNotifiedMilestone()) {
      return false;
    }

    Member receiver = diary.getMember();
    String message =
        milestone == 1
            ? "작성하신 일지가 좋아요를 받았어요! 🎉"
            : String.format("작성하신 일지의 좋아요가 %d개를 넘어갔어요! 🎉", milestone);

    createNotification(
        receiver,
        null,
        NotificationCategory.DIARY,
        NotificationType.DIARY_LIKE_MILESTONE,
        message,
        NotificationTargetType.DIARY_DETAIL,
        diaryId,
        null);

    state.updateLastNotifiedMilestone(milestone);
    return true;
  }

  @Transactional
  public void createNotification(
      Member receiver,
      Member actor,
      NotificationCategory category,
      NotificationType type,
      String message,
      NotificationTargetType targetType,
      Long targetId,
      Integer groupedCount) {
    NotificationPreference preference =
        preferenceRepository
            .findByMember(receiver)
            .orElseGet(
                () ->
                    preferenceRepository.save(
                        NotificationPreference.builder().member(receiver).build()));

    if (!isAllowed(preference, category)) {
      return;
    }

    Notification notification =
        Notification.builder()
            .receiver(receiver)
            .actor(actor)
            .category(category)
            .type(type)
            .message(message)
            .targetType(targetType)
            .targetId(targetId)
            .groupedCount(groupedCount)
            .event(
                notificationEventRepository.save(
                    NotificationEvent.builder()
                        .actor(actor)
                        .category(category)
                        .type(type)
                        .message(message)
                        .targetType(targetType)
                        .targetId(targetId)
                        .groupedCount(groupedCount)
                        .build()))
            .isRead(false)
            .expiresAt(LocalDateTime.now().plusDays(RETENTION_DAYS))
            .build();

    Notification saved = notificationRepository.save(notification);
    trimOldNotifications(receiver);
    notificationDispatchService.dispatch(saved);
  }

  @Transactional
  public NotificationEndpointResponse upsertEndpoint(
      Long memberId, NotificationEndpointUpsertRequest request) {
    Member member = getMember(memberId);

    validateEndpointRequest(request);

    NotificationEndpoint endpoint =
        notificationEndpointRepository
            .findByMemberIdAndEndpointUrl(memberId, request.getEndpointUrl())
            .orElseGet(() -> NotificationEndpoint.builder().member(member).build());

    endpoint.updateWebSubscription(
        request.getEndpointUrl(), request.getP256dh(), request.getAuthSecret());

    endpoint = notificationEndpointRepository.save(endpoint);
    return NotificationEndpointResponse.from(endpoint);
  }

  @Transactional
  public void deleteEndpoint(Long memberId, NotificationEndpointDeleteRequest request) {
    validateDeleteEndpointRequest(request);

    NotificationEndpoint endpoint =
        notificationEndpointRepository
            .findByMemberIdAndEndpointUrl(memberId, request.getEndpointUrl())
            .orElseThrow(() -> new CustomException(ErrorCode.NOTIFICATION_ENDPOINT_NOT_FOUND));

    endpoint.deactivate();
  }

  @Transactional
  public List<NotificationEndpointResponse> getEndpoints(Long memberId) {
    Member member = getMember(memberId);
    return notificationEndpointRepository.findByMemberAndIsActiveTrue(member).stream()
        .map(NotificationEndpointResponse::from)
        .toList();
  }

  @Scheduled(cron = "0 30 3 * * *")
  @Transactional
  public void deleteExpiredNotifications() {
    notificationRepository.deleteByExpiresAtBefore(LocalDateTime.now());
  }

  private boolean isAllowed(NotificationPreference preference, NotificationCategory category) {
    if (!preference.getPushEnabled()) {
      return false;
    }

    return switch (category) {
      case FRIEND -> preference.getFriendEnabled();
      case DIARY -> preference.getDiaryEnabled();
      case CHALLENGE -> preference.getChallengeEnabled();
    };
  }

  private int resolveMilestone(int likeCount) {
    if (likeCount < 1) {
      return 0;
    }

    int[] milestones = {1, 5, 10, 20, 50, 100, 1000};
    int result = 0;
    for (int milestone : milestones) {
      if (likeCount >= milestone) {
        result = milestone;
      }
    }

    if (likeCount >= 1000) {
      result = (likeCount / 1000) * 1000;
    }

    return result;
  }

  private void validateEndpointRequest(NotificationEndpointUpsertRequest request) {
    if (isBlank(request.getEndpointUrl())
        || isBlank(request.getP256dh())
        || isBlank(request.getAuthSecret())) {
      throw new CustomException(ErrorCode.NOTIFICATION_ENDPOINT_INVALID_REQUEST);
    }
  }

  private void validateDeleteEndpointRequest(NotificationEndpointDeleteRequest request) {
    if (isBlank(request.getEndpointUrl())) {
      throw new CustomException(ErrorCode.NOTIFICATION_ENDPOINT_INVALID_REQUEST);
    }
  }

  private boolean isBlank(String value) {
    return value == null || value.isBlank();
  }

  private Member getMember(Long memberId) {
    return memberRepository
        .findById(memberId)
        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
  }

  private void trimOldNotifications(Member receiver) {
    long total = notificationRepository.countByReceiver(receiver);
    if (total <= MAX_NOTIFICATIONS_PER_USER) {
      return;
    }

    int deleteCount = (int) (total - MAX_NOTIFICATIONS_PER_USER);
    Page<Notification> oldPage =
        notificationRepository.findByReceiverOrderByCreatedAtAsc(
            receiver, PageRequest.of(0, deleteCount));
    notificationRepository.deleteAll(oldPage.getContent());
  }
}

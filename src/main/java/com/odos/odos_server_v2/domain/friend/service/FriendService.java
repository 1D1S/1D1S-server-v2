package com.odos.odos_server_v2.domain.friend.service;

import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.odos.odos_server_v2.domain.friend.dto.*;
import com.odos.odos_server_v2.domain.friend.entity.BlockList;
import com.odos.odos_server_v2.domain.friend.entity.Enum.FriendRequestStatus;
import com.odos.odos_server_v2.domain.friend.entity.Friend;
import com.odos.odos_server_v2.domain.friend.entity.FriendRequest;
import com.odos.odos_server_v2.domain.friend.repository.BlockListRepository;
import com.odos.odos_server_v2.domain.friend.repository.FriendRepository;
import com.odos.odos_server_v2.domain.friend.repository.FriendRequestRepository;
import com.odos.odos_server_v2.domain.member.CurrentUserContext;
import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.member.repository.MemberRepository;
import com.odos.odos_server_v2.exception.CustomException;
import com.odos.odos_server_v2.exception.ErrorCode;

@Service
@RequiredArgsConstructor
public class FriendService {

  private final FriendRequestRepository friendRequestRepository;
  private final FriendRepository friendRepository;
  private final BlockListRepository blockListRepository;
  private final MemberRepository memberRepository;

  /** 친구 신청 */
  @Transactional
  public void sendFriendRequest(Long toMemberId) {
    Member fromMember = getCurrentMember();
    Member toMember =
        memberRepository
            .findById(toMemberId)
            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

    // 자기 자신에게 신청할 수 없음
    if (fromMember.getId().equals(toMemberId)) {
      throw new CustomException(ErrorCode.FRIEND_SELF_REQUEST);
    }

    // 차단당한 회원이면 신청 불가
    if (blockListRepository.existsByMemberAndBlockedMember(toMember, fromMember)) {
      throw new CustomException(ErrorCode.FRIEND_BLOCKED);
    }

    // 이미 친구 관계이면 신청 불가
    if (friendRepository.existsByMemberAndFriendMember(fromMember, toMember)) {
      throw new CustomException(ErrorCode.FRIEND_ALREADY_EXISTS);
    }

    // 기존 신청 확인 (대기 중)
    if (friendRequestRepository.existsByFromMemberAndToMemberAndStatus(
        fromMember, toMember, FriendRequestStatus.PENDING)) {
      throw new CustomException(ErrorCode.FRIEND_REQUEST_ALREADY_EXISTS);
    }

    // 거절된 신청이 있으면 상태를 PENDING으로 변경
    friendRequestRepository
        .findByFromMemberAndToMember(fromMember, toMember)
        .ifPresent(
            request -> {
              if (request.getStatus() == FriendRequestStatus.REJECTED) {
                request.cancel(); // 재사용을 위해 CANCELED로 변경 후 새 신청
                friendRequestRepository.delete(request);
              }
            });

    // 새로운 친구 신청 생성
    FriendRequest friendRequest =
        FriendRequest.builder()
            .fromMember(fromMember)
            .toMember(toMember)
            .status(FriendRequestStatus.PENDING)
            .build();

    friendRequestRepository.save(friendRequest);

    // TODO: 알림 발송 로직 추가
  }

  /** 친구 신청 취소 */
  @Transactional
  public void cancelFriendRequest(Long requestId) {
    Member fromMember = getCurrentMember();
    FriendRequest friendRequest =
        friendRequestRepository
            .findById(requestId)
            .orElseThrow(() -> new CustomException(ErrorCode.FRIEND_REQUEST_NOT_FOUND));

    // 본인이 보낸 신청만 취소 가능
    if (!friendRequest.getFromMember().getId().equals(fromMember.getId())) {
      throw new CustomException(ErrorCode.FRIEND_REQUEST_NOT_ACCESS);
    }

    // 대기 중인 신청만 취소 가능
    if (friendRequest.getStatus() != FriendRequestStatus.PENDING) {
      throw new CustomException(ErrorCode.FRIEND_REQUEST_NOT_PENDING);
    }

    friendRequestRepository.delete(friendRequest);
  }

  /** 친구 신청 수락 */
  @Transactional
  public void acceptFriendRequest(Long requestId) {
    Member currentMember = getCurrentMember();
    FriendRequest friendRequest =
        friendRequestRepository
            .findById(requestId)
            .orElseThrow(() -> new CustomException(ErrorCode.FRIEND_REQUEST_NOT_FOUND));

    // 수락할 권한이 있는지 확인 (내가 받은 신청인지)
    if (!friendRequest.getToMember().getId().equals(currentMember.getId())) {
      throw new CustomException(ErrorCode.FRIEND_REQUEST_NOT_ACCESS);
    }

    // 대기 중인 신청만 수락 가능
    if (friendRequest.getStatus() != FriendRequestStatus.PENDING) {
      throw new CustomException(ErrorCode.FRIEND_REQUEST_NOT_PENDING);
    }

    // 상태 변경
    friendRequest.accept();

    // 양쪽 친구 관계 생성 (맞팔로우)
    Member fromMember = friendRequest.getFromMember();
    Member toMember = friendRequest.getToMember();

    // A가 B를 친구로 추가
    Friend friend1 = Friend.builder().member(fromMember).friendMember(toMember).build();
    friendRepository.save(friend1);

    // B가 A를 친구로 추가
    Friend friend2 = Friend.builder().member(toMember).friendMember(fromMember).build();
    friendRepository.save(friend2);

    // TODO: 알림 발송 로직 추가
  }

  /** 친구 신청 거절 */
  @Transactional
  public void rejectFriendRequest(Long requestId) {
    Member currentMember = getCurrentMember();
    FriendRequest friendRequest =
        friendRequestRepository
            .findById(requestId)
            .orElseThrow(() -> new CustomException(ErrorCode.FRIEND_REQUEST_NOT_FOUND));

    // 거절할 권한이 있는지 확인 (내가 받은 신청인지)
    if (!friendRequest.getToMember().getId().equals(currentMember.getId())) {
      throw new CustomException(ErrorCode.FRIEND_REQUEST_NOT_ACCESS);
    }

    // 대기 중인 신청만 거절 가능
    if (friendRequest.getStatus() != FriendRequestStatus.PENDING) {
      throw new CustomException(ErrorCode.FRIEND_REQUEST_NOT_PENDING);
    }

    friendRequest.reject();
  }

  /** 친구 목록 조회 */
  @Transactional(readOnly = true)
  public List<FriendResponseDto> getFriendList() {
    Member currentMember = getCurrentMember();
    List<Friend> friends = friendRepository.findByMember(currentMember);

    return friends.stream()
        .map(
            friend ->
                FriendResponseDto.builder()
                    .memberId(friend.getFriendMember().getId())
                    .nickname(friend.getFriendMember().getNickname())
                    .profileUrl(friend.getFriendMember().getProfileUrl())
                    .build())
        .collect(Collectors.toList());
  }

  /** 받은 친구 신청 목록 조회 */
  @Transactional(readOnly = true)
  public List<FriendRequestResponseDto> getReceivedFriendRequests() {
    Member currentMember = getCurrentMember();
    List<FriendRequest> requests =
        friendRequestRepository.findByToMemberAndStatus(currentMember, FriendRequestStatus.PENDING);

    return requests.stream()
        .map(
            request ->
                FriendRequestResponseDto.builder()
                    .requestId(request.getId())
                    .fromMemberId(request.getFromMember().getId())
                    .fromMemberNickname(request.getFromMember().getNickname())
                    .fromMemberProfileUrl(request.getFromMember().getProfileUrl())
                    .status(request.getStatus().name())
                    .createdAt(request.getCreatedAt())
                    .build())
        .collect(Collectors.toList());
  }

  /** 보낸 친구 신청 목록 조회 */
  @Transactional(readOnly = true)
  public List<FriendRequestResponseDto> getSentFriendRequests() {
    Member currentMember = getCurrentMember();
    List<FriendRequest> requests = friendRequestRepository.findByFromMember(currentMember);

    return requests.stream()
        .filter(request -> request.getStatus() == FriendRequestStatus.PENDING)
        .map(
            request ->
                FriendRequestResponseDto.builder()
                    .requestId(request.getId())
                    .fromMemberId(request.getFromMember().getId())
                    .fromMemberNickname(request.getFromMember().getNickname())
                    .fromMemberProfileUrl(request.getFromMember().getProfileUrl())
                    .status(request.getStatus().name())
                    .createdAt(request.getCreatedAt())
                    .build())
        .collect(Collectors.toList());
  }

  /** 친구 삭제 (언팔로우) */
  @Transactional
  public void deleteFriend(Long friendMemberId) {
    Member currentMember = getCurrentMember();
    Member friendMember =
        memberRepository
            .findById(friendMemberId)
            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

    // 친구 관계 존재 확인
    Friend friend =
        friendRepository
            .findByMemberAndFriendMember(currentMember, friendMember)
            .orElseThrow(() -> new CustomException(ErrorCode.FRIEND_NOT_EXISTS));

    friendRepository.delete(friend);

    // 상대방의 친구 목록에서도 삭제 (양방향)
    friendRepository
        .findByMemberAndFriendMember(friendMember, currentMember)
        .ifPresent(friendRepository::delete);

    // 해당하는 친구 신청이 있으면 삭제
    friendRequestRepository
        .findByFromMemberAndToMember(currentMember, friendMember)
        .ifPresent(friendRequestRepository::delete);
    friendRequestRepository
        .findByFromMemberAndToMember(friendMember, currentMember)
        .ifPresent(friendRequestRepository::delete);
  }

  /** 차단 */
  @Transactional
  public void blockMember(Long blockedMemberId) {
    Member currentMember = getCurrentMember();
    Member blockedMember =
        memberRepository
            .findById(blockedMemberId)
            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

    // 자기 자신을 차단할 수 없음
    if (currentMember.getId().equals(blockedMemberId)) {
      throw new CustomException(ErrorCode.FRIEND_SELF_REQUEST);
    }

    // 이미 차단했는지 확인
    if (blockListRepository.existsByMemberAndBlockedMember(currentMember, blockedMember)) {
      throw new CustomException(ErrorCode.FRIEND_ALREADY_BLOCKED);
    }

    // 차단 목록에 추가
    BlockList blockList =
        BlockList.builder()
            .member(currentMember)
            .blockedMember(blockedMember)
            .build();

    blockListRepository.save(blockList);

    // 친구 관계가 있으면 해제
    friendRepository
        .findByMemberAndFriendMember(currentMember, blockedMember)
        .ifPresent(friendRepository::delete);
    friendRepository
        .findByMemberAndFriendMember(blockedMember, currentMember)
        .ifPresent(friendRepository::delete);

    // 해당하는 친구 신청이 있으면 삭제
    friendRequestRepository
        .findByFromMemberAndToMember(currentMember, blockedMember)
        .ifPresent(friendRequestRepository::delete);
    friendRequestRepository
        .findByFromMemberAndToMember(blockedMember, currentMember)
        .ifPresent(friendRequestRepository::delete);
  }

  /** 차단 해제 */
  @Transactional
  public void unblockMember(Long blockedMemberId) {
    Member currentMember = getCurrentMember();
    Member blockedMember =
        memberRepository
            .findById(blockedMemberId)
            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

    BlockList blockList =
        blockListRepository
            .findByMemberAndBlockedMember(currentMember, blockedMember)
            .orElseThrow(() -> new CustomException(ErrorCode.FRIEND_NOT_BLOCKED));

    blockListRepository.delete(blockList);
  }

  /** 차단 목록 조회 */
  @Transactional(readOnly = true)
  public List<FriendResponseDto> getBlockList() {
    Member currentMember = getCurrentMember();
    List<BlockList> blockList =
        blockListRepository.findByMember(currentMember);

    return blockList.stream()
        .map(
            block ->
                FriendResponseDto.builder()
                    .memberId(block.getBlockedMember().getId())
                    .nickname(block.getBlockedMember().getNickname())
                    .profileUrl(block.getBlockedMember().getProfileUrl())
                    .build())
        .collect(Collectors.toList());
  }

  /** 특정 회원과의 관계 상태 조회 */
  @Transactional(readOnly = true)
  public MemberRelationResponseDto getMemberRelation(Long targetMemberId) {
    Member currentMember = getCurrentMember();
    Member targetMember =
        memberRepository
            .findById(targetMemberId)
            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

    // 차단 여부 확인
    if (blockListRepository.existsByMemberAndBlockedMember(currentMember, targetMember)) {
      return MemberRelationResponseDto.builder()
          .memberId(targetMemberId)
          .relationStatus("BLOCKED")
          .build();
    }

    // 친구 여부 확인
    if (friendRepository.existsByMemberAndFriendMember(currentMember, targetMember)) {
      return MemberRelationResponseDto.builder()
          .memberId(targetMemberId)
          .relationStatus("FRIEND")
          .build();
    }

    // 내가 보낸 친구 신청 확인
    if (friendRequestRepository.existsByFromMemberAndToMemberAndStatus(
        currentMember, targetMember, FriendRequestStatus.PENDING)) {
      return MemberRelationResponseDto.builder()
          .memberId(targetMemberId)
          .relationStatus("REQUEST_SENT")
          .build();
    }

    // 내가 받은 친구 신청 확인
    if (friendRequestRepository.existsByFromMemberAndToMemberAndStatus(
        targetMember, currentMember, FriendRequestStatus.PENDING)) {
      return MemberRelationResponseDto.builder()
          .memberId(targetMemberId)
          .relationStatus("REQUEST_RECEIVED")
          .build();
    }

    // 관계 없음
    return MemberRelationResponseDto.builder()
        .memberId(targetMemberId)
        .relationStatus("NONE")
        .build();
  }

  private Member getCurrentMember() {
    Long memberId = CurrentUserContext.getCurrentMemberId();
    return memberRepository
        .findById(memberId)
        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
  }
}

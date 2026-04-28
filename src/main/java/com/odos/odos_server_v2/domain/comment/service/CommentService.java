package com.odos.odos_server_v2.domain.comment.service;

import com.odos.odos_server_v2.domain.comment.dto.CommentRequest;
import com.odos.odos_server_v2.domain.comment.dto.CommentResponse;
import com.odos.odos_server_v2.domain.comment.entity.Comment;
import com.odos.odos_server_v2.domain.comment.repository.CommentRepository;
import com.odos.odos_server_v2.domain.diary.entity.Diary;
import com.odos.odos_server_v2.domain.diary.repository.DiaryRepository;
import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.member.repository.MemberRepository;
import com.odos.odos_server_v2.domain.notification.service.NotificationService;
import com.odos.odos_server_v2.domain.shared.dto.OffsetPagination;
import com.odos.odos_server_v2.domain.shared.service.ImageService;
import com.odos.odos_server_v2.exception.CustomException;
import com.odos.odos_server_v2.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentService {

  private final CommentRepository commentRepository;
  private final DiaryRepository diaryRepository;
  private final MemberRepository memberRepository;
  private final ImageService imageService;
  private final NotificationService notificationService;

  @Transactional
  public CommentResponse createComment(Long memberId, Long diaryId, CommentRequest request) {
    Member member =
        memberRepository
            .findById(memberId)
            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    Diary diary =
        diaryRepository
            .findByIdAndIsDeletedFalse(diaryId)
            .orElseThrow(() -> new CustomException(ErrorCode.DIARY_NOT_FOUND));

    Comment comment =
        Comment.builder().content(request.getContent()).member(member).diary(diary).build();
    Comment saved = commentRepository.save(comment);

    if (!diary.getMember().getId().equals(memberId)) {
      notificationService.notifyMyDiaryCommented(
          memberId,
          diary.getMember().getId(),
          diary.getId(),
          member.getNickname(),
          request.getContent());
    }

    return CommentResponse.from(saved, imageService.getFileUrl(member.getProfileUrl()), 0L);
  }

  @Transactional
  public CommentResponse createReply(Long memberId, Long parentCommentId, CommentRequest request) {
    Member member =
        memberRepository
            .findById(memberId)
            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    Comment parent =
        commentRepository
            .findById(parentCommentId)
            .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

    if (parent.getIsDeleted()) {
      throw new CustomException(ErrorCode.COMMENT_NOT_FOUND);
    }
    if (parent.getParent() != null) {
      throw new CustomException(ErrorCode.CANNOT_REPLY_TO_REPLY);
    }

    Comment reply =
        Comment.builder()
            .content(request.getContent())
            .member(member)
            .diary(parent.getDiary())
            .parent(parent)
            .build();
    Comment saved = commentRepository.save(reply);

    if (!parent.getMember().getId().equals(memberId)) {
      notificationService.notifyMyCommentReplied(
          memberId,
          parent.getMember().getId(),
          parent.getDiary().getId(),
          member.getNickname(),
          request.getContent());
    }

    return CommentResponse.from(saved, imageService.getFileUrl(member.getProfileUrl()), 0L);
  }

  @Transactional
  public void deleteComment(Long memberId, Long commentId) {
    Comment comment =
        commentRepository
            .findById(commentId)
            .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

    if (!comment.getMember().getId().equals(memberId)) {
      throw new CustomException(ErrorCode.COMMENT_NOT_ACCESS);
    }

    comment.softDelete();
  }

  @Transactional
  public OffsetPagination<CommentResponse> getComments(Long diaryId, int page, int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    Page<Comment> comments = commentRepository.findByDiaryIdAndParentIsNull(diaryId, pageable);

    Page<CommentResponse> responsePage =
        comments.map(
            comment ->
                CommentResponse.from(
                    comment,
                    imageService.getFileUrl(comment.getMember().getProfileUrl()),
                    commentRepository.countByParentId(comment.getId())));

    return OffsetPagination.from(responsePage);
  }

  @Transactional
  public OffsetPagination<CommentResponse> getReplies(Long commentId, int page, int size) {
    commentRepository
        .findById(commentId)
        .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));
    Page<Comment> replies = commentRepository.findByParentId(commentId, pageable);

    Page<CommentResponse> responsePage =
        replies.map(
            reply ->
                CommentResponse.from(
                    reply, imageService.getFileUrl(reply.getMember().getProfileUrl()), 0L));

    return OffsetPagination.from(responsePage);
  }
}

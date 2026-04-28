package com.odos.odos_server_v2.response;

public class Message {
  public static String SIGN_UP_INFO = "회원가입 및 추가정보 입력 성공했습니다.";

  // security
  public static String TOKEN_REFRESH = "토큰 재발급 성공했습니다.";
  public static String LOGOUT = "로그아웃 성공했습니다.";
  public static String LOGIN_SUCCESS = "소셜 로그인 연결 성공했습니다.";

  // diary
  public static String DIARY_CREATE_SUCCESS = "다이어리 생성을 성공했습니다.";
  public static String DIARY_UPDATE_SUCCESS = "다이어리 수정을 성공했습니다.";
  public static String DIARY_GET_SUCCESS = "다이어리 단일조회를 성공했습니다.";
  public static String DIARY_GET_ALL_SUCCESS = "다이어리 모든 조회를 성공했습니다.";
  public static String DIARY_DELETE_SUCCESS = "다이어리 삭제를 성공했습니다.";
  public static String DIARY_ADDED_LIKE = "다이어리에 좋아요 누르기 성공했습니다.";
  public static String DIARY_CANCELED_LIKE = "다이어리에 좋아요 누르기 취소 성공했습니다.";
  public static String DIARY_VIEW_RANDOM = "다이어리 랜덤 조회 성공했습니다.";
  public static String DIARY_REPORT_CREATED = "다이어리 신고 생성 성공했습니다.";
  public static String DIARY_GET_MY_ALL_SUCCESS = "다이어리 중 작성자 본인 조회 성공했습니다.";
  public static String DIARY_IMAGE_UPLOADED = "다이어리 이미지 단일 업로드 성공했습니다.";
  public static String DIARY_IMAGES_UPLOADED = "다이어리 이미지 다중 업로드 성공했습니다.";
  public static String CHALLENGE_DIARY_GET = "특정 챌린지의 일지 리스트 조회 완료했습니다.";
  public static String DIARIES_BY_COMPLETED_DATE = "특정 목표 달성 날짜의 일지 리스트 조회 완료했습니다.";
  public static String DIARIES_BY_CREATED_DATE = "특정 생성된 날짜의 일지 리스트 조회 완료했습니다.";
  public static String DIARIES_BY_COMPLETED_DATE_WITH_RANGE = "특정 목표 달성 날짜 기간별 일지 리스트 조회 완료했습니다.";
  public static String DIARIES_BY_CREATED_DATE_WITH_RANGE = "특정 일지 생성 날짜 기간별 일지 리스트 조회 완료했습니다.";

  // challenge
  public static String CREATE_CHALLENGE = "챌린지 생성 성공했습니다.";
  public static String EDIT_CHALLENGE = "챌린지 수정 성공했습니다.";
  public static String GET_CHALLENGE = "챌린지 상세 조회 성공했습니다.";
  public static String APPLY_CHALLENGE = "챌린지 신청 성공했습니다.";
  public static String ACCEPT_PARTICIPANT = "참여자 수락 성공했습니다.";
  public static String REJECT_PARTICIPANT = "참여자 거절 성공했습니다.";
  public static String EDIT_CHALLENGE_GOAL = "챌린지 목표 수정 성공했습니다.";
  public static String GET_RANDOM_CHALLENGES = "챌린지 랜덤 불러오기 성공했습니다.";
  public static String LEAVE_CHALLENGE = "챌린지 탈퇴하기 성공했습니다.";
  public static String GET_CHALLENGE_LIST = "챌린지 리스트 불러오기 성공했습니다.";
  public static String ADD_CHALLENGE_LIKE = "챌린지 좋아요 성공했습니다.";
  public static String CANCEL_CHALLENGE_LIKE = "챌린지 좋아요 취소 성공했습니다.";
  public static String GET_CHALLENGES_BY_MEMBER = "진행중인 챌린지 불러오기 성공했습니다.";
  public static String GET_MY_CHALLENGE = "내 챌린지 목록 조회 성공했습니다.";
  public static String GET_MY_CHALLENGE_DIARY_WRITTEN = "특정 챌린지의 3일 이내의 일지 작성 날짜 조회 성공했습니다.";
  public static String JOIN_PRIVATE_CHALLENGE = "비공개 챌린지 참여 성공했습니다.";

  // member
  public static String GET_MYPAGE = "마이페이지 조회 성공했습니다.";
  public static String GET_SIDEBAR = "사이드바 조회 성공했습니다.";
  public static String UPDATE_NICKNAME = "닉네임 변경 성공했습니다.";
  public static String NICKNAME_AVAILABLE = "사용 가능한 닉네임입니다.";
  public static String UPDATE_PROFILE_IMAGE = "프로필 이미지 변경 성공했습니다.";
  public static String GET_OTHERS_PROFILE = "다른 사람 프로필 조회 성공했습니다.";
  public static String MEMBER_DELETE = "회원 탈퇴 요청 성공했습니다.";

  // image
  public static String CREATE_PRESIGNED_URL_SUCCESS = "presigned url 발급 성공했습니다.";

  // comment
  public static String COMMENT_CREATE_SUCCESS = "댓글 생성 성공했습니다.";
  public static String REPLY_CREATE_SUCCESS = "대댓글 생성 성공했습니다.";
  public static String COMMENT_DELETE_SUCCESS = "댓글 삭제 성공했습니다.";
  public static String COMMENT_GET_SUCCESS = "댓글 조회 성공했습니다.";
  public static String REPLY_GET_SUCCESS = "대댓글 조회 성공했습니다.";
  public static String COMMENT_REPORT_CREATED = "댓글 신고 생성 성공했습니다.";

  // friend
  public static String FRIEND_REQUEST_SEND = "친구 신청을 보냈습니다.";
  public static String FRIEND_REQUEST_CANCEL = "친구 신청을 취소했습니다.";
  public static String FRIEND_REQUEST_ACCEPT = "친구 신청을 수락했습니다.";
  public static String FRIEND_REQUEST_REJECT = "친구 신청을 거절했습니다.";
  public static String FRIEND_DELETE = "친구를 삭제했습니다.";
  public static String MEMBER_BLOCK = "회원을 차단했습니다.";
  public static String MEMBER_UNBLOCK = "차단을 해제했습니다.";
  public static String GET_FRIEND_LIST = "친구 목록 조회 성공했습니다.";
  public static String GET_FRIEND_REQUESTS = "친구 신청 목록 조회 성공했습니다.";
  public static String GET_BLOCK_LIST = "차단 목록 조회 성공했습니다.";
  public static String GET_MEMBER_RELATION = "회원 관계 상태 조회 성공했습니다.";
}

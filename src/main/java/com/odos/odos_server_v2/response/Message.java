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

  // challenge
  public static String CREATE_CHALLENGE = "챌린지 생성 성공했습니다.";
  public static String GET_CHALLENGE = "챌린지 상세 조회 성공했습니다.";
  public static String APPLY_CHALLENGE = "챌린지 신청 성공했습니다.";
  public static String ACCEPT_PARTICIPANT = "참여자 수락 성공했습니다.";
  public static String REJECT_PARTICIPANT = "참여자 거절 성공했습니다.";
  public static String GET_RANDOM_CHALLENGES = "챌린지 랜덤 불러오기 성공했습니다.";
  public static String LEAVE_CHALLENGE = "챌린지 탈퇴하기 성공했습니다.";
  public static String GET_CHALLENGE_LIST = "챌린지 리스트 불러오기 성공했습니다.";
  public static String ADD_CHALLENGE_LIKE = "챌린지 좋아요 성공했습니다.";
  public static String CANCEL_CHALLENGE_LIKE = "챌린지 좋아요 취소 성공했습니다.";
  public static String GET_CHALLENGES_BY_MEMBER = "진행중인 챌린지 불러오기 성공했습니다.";
}

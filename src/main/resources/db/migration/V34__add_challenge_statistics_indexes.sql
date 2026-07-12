-- 챌린지 통계 API(GET /challenges/{id}/statistics) 성능.
-- 통계/참여율/추이 쿼리는 모두 challenge_id 로 필터하지만 diary 에는 challenge_id 선두 인덱스가
-- 없어(기존 인덱스는 member_id 선두) 매 요청마다 diary 전체를 순차 스캔한다. 데이터가 쌓일수록 느려짐.
-- (challenge_id, is_deleted, completed_date): 참여율/전체목표 완료수 집계, 기간별 추이 group by 를 커버.
CREATE INDEX idx_diary_challenge_deleted_completed
    ON diary (challenge_id, is_deleted, completed_date);

-- diary_goal 은 FK(diary_id) 인덱스가 전혀 없어 diary_goal ⨯ diary 조인 시 diary_goal 을 풀스캔한다.
-- (diary_id, is_completed): 완료 목표 수 집계 조인을 인덱스 조회로 전환.
CREATE INDEX idx_diary_goal_diary_completed
    ON diary_goal (diary_id, is_completed);

-- 통계 API: member_id + completed_date 범위 집계(GROUP BY completed_date)용 인덱스.
-- 현재 completed_date 를 선두로 하는 인덱스가 없어 회원별 기간 집계 시 풀스캔이 발생하므로 추가한다.
CREATE INDEX idx_diary_member_completed_date ON diary (member_id, completed_date);

-- (member_id, created_at) 인덱스는 기존 idx_diary_member_created_at_is_deleted 가
-- prefix 로 이미 커버하므로 중복 인덱스는 추가하지 않는다.

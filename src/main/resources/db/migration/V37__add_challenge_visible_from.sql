-- 공식(OFFICIAL) 챌린지 예약 노출 시각. null = 즉시 노출(기존 데이터 포함).
-- 클라 조회 필터: (visible_from is null or visible_from <= now). now 는 KST 벽시계.
ALTER TABLE challenge ADD COLUMN visible_from timestamp(6);

-- ponytail: 인덱스 미추가. 필터가 (visible_from is null or visible_from <= now) OR-술어라
-- b-tree 로 sargable 하지 않고, 예약 대상은 OFFICIAL 소수 행뿐이며 목록 쿼리는 이미
-- deletedAt is null 등으로 풀스캔 없이 도는 소규모 테이블이다. 성장 시 부분 인덱스
-- (where visible_from is not null) 로 승급.

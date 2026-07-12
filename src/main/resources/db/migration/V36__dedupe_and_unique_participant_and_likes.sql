-- participant / challenge_like / diary_like 는 "회원당 챌린지/일지에 1건"이어야 하는 관계인데
-- 유니크 제약이 없어(V1 이후 추가된 적 없음) 더블 서브밋·경합으로 중복 행이 쌓였다.
-- 이 중복은 단건 조회 finder(findFirst.../findByChallengeIdAndMemberId/findDiaryLikeBy...)에서
-- NonUniqueResultException(500) 의 근본 원인이다. 중복을 정리(dedupe)한 뒤 유니크 제약을 건다.
--
-- dedupe 기준: 각 그룹에서 가장 오래된 행(= MIN(id), 최초 참여/최초 좋아요)을 남기고 나머지를 삭제.
-- member_id / challenge_id / diary_id 가 NULL 인 행은 "그룹"을 이룰 수 없어 dedupe 대상에서 제외한다.
-- (Postgres 유니크 제약은 NULL 을 서로 다른 값으로 취급하므로 NULL 포함 행은 제약 위반을 일으키지 않는다.)
--
-- 참조 무결성: participant.id 를 참조하는 유일한 자식은 challenge_goal.participant_id 다
-- (DiaryGoal 의 participant 참조는 코드에서 주석 처리되어 비활성). 스키마에 실제 FK 제약은 없지만,
-- 삭제될 중복 participant 의 challenge_goal 이 고아가 되지 않도록 남길 행(winner)으로 먼저 재지정한다.
-- 이렇게 하면 diary_goal → challenge_goal 링크(일지-목표 연결)도 보존된다.
-- challenge_like / diary_like 는 다른 테이블에서 참조되지 않는 leaf 라 재지정 없이 바로 삭제한다.

-- 1) participant: 중복 participant 의 challenge_goal 을 winner(MIN id)로 재지정
UPDATE challenge_goal cg
SET participant_id = w.keep_id
FROM (
  SELECT id,
         MIN(id) OVER (PARTITION BY member_id, challenge_id) AS keep_id
  FROM participant
  WHERE member_id IS NOT NULL AND challenge_id IS NOT NULL
) w
WHERE cg.participant_id = w.id
  AND w.id <> w.keep_id;

-- 2) participant: 중복 행(winner 이외) 삭제
DELETE FROM participant p
USING (
  SELECT id,
         MIN(id) OVER (PARTITION BY member_id, challenge_id) AS keep_id
  FROM participant
  WHERE member_id IS NOT NULL AND challenge_id IS NOT NULL
) w
WHERE p.id = w.id
  AND w.id <> w.keep_id;

-- 3) participant: 유니크 제약 추가. V35 의 비유니크 인덱스(member_id, challenge_id)는
--    유니크 인덱스가 동일 컬럼·순서를 커버하므로 중복이라 제거한다.
DROP INDEX IF EXISTS idx_participant_member_challenge;
ALTER TABLE participant
  ADD CONSTRAINT uq_participant_member_challenge UNIQUE (member_id, challenge_id);

-- 4) challenge_like: 중복(챌린지+회원) 삭제 후 유니크 제약. 참조 없는 leaf 라 재지정 불필요.
DELETE FROM challenge_like cl
USING (
  SELECT id,
         MIN(id) OVER (PARTITION BY challenge_id, member_id) AS keep_id
  FROM challenge_like
  WHERE challenge_id IS NOT NULL AND member_id IS NOT NULL
) w
WHERE cl.id = w.id
  AND w.id <> w.keep_id;

DROP INDEX IF EXISTS idx_challenge_like_challenge_member;
ALTER TABLE challenge_like
  ADD CONSTRAINT uq_challenge_like_challenge_member UNIQUE (challenge_id, member_id);

-- 5) diary_like: 중복(일지+회원) 삭제 후 유니크 제약. 참조 없는 leaf.
DELETE FROM diary_like dl
USING (
  SELECT id,
         MIN(id) OVER (PARTITION BY diary_id, member_id) AS keep_id
  FROM diary_like
  WHERE diary_id IS NOT NULL AND member_id IS NOT NULL
) w
WHERE dl.id = w.id
  AND w.id <> w.keep_id;

DROP INDEX IF EXISTS idx_diary_like_diary_member;
ALTER TABLE diary_like
  ADD CONSTRAINT uq_diary_like_diary_member UNIQUE (diary_id, member_id);

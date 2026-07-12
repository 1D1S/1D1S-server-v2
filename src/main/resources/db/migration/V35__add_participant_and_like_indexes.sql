-- participant / challenge_like / diary_like / challenge_goal 는 가장 자주 조인·필터되는
-- 테이블인데 FK 컬럼에 인덱스가 하나도 없어(V1 이후 추가된 적 없음) 모든 참여자/좋아요 조회가
-- 풀스캔이다. challenge 목록 요약(toChallengeSummary)은 아이템 1건당 참여자수 COUNT +
-- like exists/COUNT 를 발사하므로, 이 풀스캔들이 목록 크기만큼 곱해져 목록 API 를 크게 느리게 한다.

-- participant: 챌린지별 상태 조회(findByChallengeIdAndStatusIn / countByChallengeIdAndStatusIn /
-- existsByChallengeIdAndMemberIdAndStatus) 커버.
CREATE INDEX IF NOT EXISTS idx_participant_challenge_status
    ON participant (challenge_id, status);

-- participant: 회원별 참여 조회(findByMemberIdAndStatusIn) 및 회원+챌린지 단건 조회
-- (findFirstByMemberIdAndChallengeIdOrderByIdAsc) 커버.
CREATE INDEX IF NOT EXISTS idx_participant_member_challenge
    ON participant (member_id, challenge_id);

-- challenge_goal: 참여자별 목표 조회/삭제(findAllByParticipant_Member_Id, deleteAllByParticipant) 커버.
CREATE INDEX IF NOT EXISTS idx_challenge_goal_participant
    ON challenge_goal (participant_id);

-- challenge_like: countByChallengeId(선두 challenge_id) 와 existsByChallengeIdAndMemberId(둘 다)를
-- 단일 복합 인덱스로 커버.
CREATE INDEX IF NOT EXISTS idx_challenge_like_challenge_member
    ON challenge_like (challenge_id, member_id);

-- diary_like: countByDiaryId 와 diaryId+memberId 존재 조회를 단일 복합 인덱스로 커버.
CREATE INDEX IF NOT EXISTS idx_diary_like_diary_member
    ON diary_like (diary_id, member_id);

-- 챌린지 종료 후 유예 기간(종료일+2일) 내 일지 작성 허용 여부.
-- 기존 챌린지는 옵션 OFF(종료 후 작성 불가)로 호환.
ALTER TABLE challenge ADD COLUMN post_end_write_allowed BOOLEAN NOT NULL DEFAULT false;

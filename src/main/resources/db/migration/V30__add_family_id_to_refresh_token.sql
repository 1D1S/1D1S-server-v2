-- RefreshToken Rotation 의 재사용 감지 범위를 "세션(로그인) 단위"로 좁히기 위한 family_id 추가.
-- 로그인 1회 = 새 family, 회전 시 같은 family_id 상속. 재사용 감지 시 해당 family 만 무효화한다.
ALTER TABLE refresh_token ADD COLUMN family_id VARCHAR(36);

-- 기존 활성 토큰은 각자 독립된 세션으로 보고 개별 family_id 를 부여한다.
UPDATE refresh_token SET family_id = gen_random_uuid()::text WHERE family_id IS NULL;

ALTER TABLE refresh_token ALTER COLUMN family_id SET NOT NULL;

CREATE INDEX idx_refresh_token_family_id ON refresh_token (family_id);

-- 전화번호 중복 불가. NULL 은 Postgres 에서 서로 distinct 하여 값 없는 기존 회원은 다중 허용된다.
-- 컬럼(V39)이 방금 추가되어 값이 대부분 NULL 이므로 중복 충돌 없이 적용된다.
ALTER TABLE member
    ADD CONSTRAINT uk_member_phone_number UNIQUE (phone_number);

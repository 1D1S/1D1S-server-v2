-- 상품 발송용 전화번호 수집. 기존 회원은 값이 없으므로 nullable.
-- 저장 형식은 숫자만(하이픈 제거, 예: 01012345678). 중복 허용 → 유니크 제약 없음.
ALTER TABLE member
    ADD COLUMN phone_number VARCHAR(20) NULL;

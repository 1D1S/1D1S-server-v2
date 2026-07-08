ALTER TABLE diary ADD COLUMN thumbnail_url VARCHAR(1024) DEFAULT NULL;

-- objectKey 가 UUID_원본파일명 이라 varchar(255) 로는 긴 파일명에서 잘릴 수 있어 함께 확장
ALTER TABLE diary_image ALTER COLUMN url TYPE VARCHAR(1024);

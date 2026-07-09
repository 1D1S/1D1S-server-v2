-- 1회성 백필: 이미지가 있는 기존 일지의 대표 썸네일을 첫 번째(가장 먼저 등록된, 최소 id) 이미지로 지정.
-- 이후 신규 일지는 thumbnailUrl 미선택 시 null 로 남는다(이 마이그레이션 실행 시점 이전 데이터만 대상).
UPDATE diary d
SET thumbnail_url = (
    SELECT di.url
    FROM diary_image di
    WHERE di.diary_id = d.id
      AND di.url IS NOT NULL
    ORDER BY di.id
    LIMIT 1
)
WHERE d.thumbnail_url IS NULL
  AND EXISTS (
    SELECT 1 FROM diary_image di2
    WHERE di2.diary_id = d.id
      AND di2.url IS NOT NULL
  );

-- 카테고리 개편: 제거되는 값(MUSIC, LEISURE, STUDY, ECONOMY)을 신규 값으로 재매핑.
-- Category enum(EnumType.STRING)을 저장하는 모든 컬럼: challenge.category, interest.category.
--   MUSIC   -> HOBBY
--   LEISURE -> HOBBY
--   STUDY   -> SELF_DEV
--   ECONOMY -> ETC

UPDATE challenge SET category = 'HOBBY'    WHERE category = 'MUSIC';
UPDATE challenge SET category = 'HOBBY'    WHERE category = 'LEISURE';
UPDATE challenge SET category = 'SELF_DEV' WHERE category = 'STUDY';
UPDATE challenge SET category = 'ETC'      WHERE category = 'ECONOMY';

UPDATE interest SET category = 'HOBBY'    WHERE category = 'MUSIC';
UPDATE interest SET category = 'HOBBY'    WHERE category = 'LEISURE';
UPDATE interest SET category = 'SELF_DEV' WHERE category = 'STUDY';
UPDATE interest SET category = 'ETC'      WHERE category = 'ECONOMY';

-- 재매핑 후 구값이 0건인지 검증. 남아 있으면 마이그레이션 실패시킴.
DO $$
DECLARE
  leftover BIGINT;
BEGIN
  SELECT count(*) INTO leftover
  FROM (
    SELECT category FROM challenge
    UNION ALL
    SELECT category FROM interest
  ) c
  WHERE category IN ('MUSIC', 'LEISURE', 'STUDY', 'ECONOMY');

  IF leftover > 0 THEN
    RAISE EXCEPTION '카테고리 재매핑 후 구값이 % 건 남아 있습니다.', leftover;
  END IF;
END $$;

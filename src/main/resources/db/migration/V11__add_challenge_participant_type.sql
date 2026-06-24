ALTER TABLE challenge
    RENAME COLUMN "type" TO goal_type;
ALTER TABLE challenge
    ADD COLUMN participation_type VARCHAR(20);

UPDATE challenge
SET participation_type = CASE
                             WHEN max_participants_cnt IN (0, 1) THEN 'INDIVIDUAL'
                             ELSE 'GROUP'
                         END;

ALTER TABLE challenge ALTER COLUMN participation_type SET NOT NULL;

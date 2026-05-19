CREATE TABLE notification_event (
                                    id BIGINT NOT NULL AUTO_INCREMENT,
                                    created_at DATETIME(6) NOT NULL,
                                    actor_member_id BIGINT NULL,
                                    category VARCHAR(20) NOT NULL,
                                    type VARCHAR(40) NOT NULL,
                                    message TEXT NOT NULL,
                                    target_type VARCHAR(30) NOT NULL,
                                    target_id BIGINT NULL,
                                    grouped_count INT NULL,
                                    PRIMARY KEY (id),
                                    CONSTRAINT fk_notification_event_actor_member FOREIGN KEY (actor_member_id) REFERENCES member (member_id)
);

ALTER TABLE notification
    ADD COLUMN notification_event_id BIGINT NULL,
    ADD CONSTRAINT fk_notification_event FOREIGN KEY (notification_event_id) REFERENCES notification_event (id);

INSERT INTO notification_event (created_at, actor_member_id, category, type, message, target_type, target_id, grouped_count)
SELECT created_at, actor_member_id, category, type, message, target_type, target_id, grouped_count
FROM notification;

UPDATE notification n
    JOIN notification_event e
ON e.created_at = n.created_at
    AND ((e.actor_member_id = n.actor_member_id) OR (e.actor_member_id IS NULL AND n.actor_member_id IS NULL))
    AND e.category = n.category
    AND e.type = n.type
    AND e.target_type = n.target_type
    AND ((e.target_id = n.target_id) OR (e.target_id IS NULL AND n.target_id IS NULL))
    SET n.notification_event_id = e.id
WHERE n.notification_event_id IS NULL;

CREATE INDEX idx_notification_event_created_at ON notification_event (created_at);

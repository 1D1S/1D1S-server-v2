CREATE TABLE notification_endpoint (
                                       id BIGINT NOT NULL AUTO_INCREMENT,
                                       created_at DATETIME(6) NOT NULL,
                                       member_id BIGINT NOT NULL,
                                       endpoint_url VARCHAR(1024) NULL,
                                       p256dh VARCHAR(512) NULL,
                                       auth_secret VARCHAR(512) NULL,
                                       is_active BIT(1) NOT NULL,
                                       last_seen_at DATETIME(6) NULL,
                                       PRIMARY KEY (id),
                                       CONSTRAINT fk_notification_endpoint_member FOREIGN KEY (member_id) REFERENCES member (member_id)
);

CREATE INDEX idx_notification_endpoint_member_active
    ON notification_endpoint (member_id, is_active);
CREATE UNIQUE INDEX uq_notification_endpoint_member_url
    ON notification_endpoint (member_id, endpoint_url);

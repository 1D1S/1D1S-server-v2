CREATE TABLE notification (
                              id BIGINT NOT NULL AUTO_INCREMENT,
                              created_at DATETIME(6) NOT NULL,
                              receiver_member_id BIGINT NOT NULL,
                              actor_member_id BIGINT NULL,
                              category VARCHAR(20) NOT NULL,
                              type VARCHAR(40) NOT NULL,
                              message TEXT NOT NULL,
                              target_type VARCHAR(30) NOT NULL,
                              target_id BIGINT NULL,
                              is_read BIT(1) NOT NULL,
                              read_at DATETIME(6) NULL,
                              grouped_count INT NULL,
                              expires_at DATETIME(6) NOT NULL,
                              PRIMARY KEY (id),
                              CONSTRAINT fk_notification_receiver_member FOREIGN KEY (receiver_member_id) REFERENCES member (member_id),
                              CONSTRAINT fk_notification_actor_member FOREIGN KEY (actor_member_id) REFERENCES member (member_id)
);

CREATE INDEX idx_notification_receiver_created_at ON notification (receiver_member_id, created_at);
CREATE INDEX idx_notification_expires_at ON notification (expires_at);
CREATE INDEX idx_notification_receiver_is_read ON notification (receiver_member_id, is_read);

CREATE TABLE notification_preference (
                                         id BIGINT NOT NULL AUTO_INCREMENT,
                                         member_id BIGINT NOT NULL,
                                         push_enabled BIT(1) NOT NULL,
                                         friend_enabled BIT(1) NOT NULL,
                                         diary_enabled BIT(1) NOT NULL,
                                         challenge_enabled BIT(1) NOT NULL,
                                         PRIMARY KEY (id),
                                         CONSTRAINT uk_notification_preference_member UNIQUE (member_id),
                                         CONSTRAINT fk_notification_preference_member FOREIGN KEY (member_id) REFERENCES member (member_id)
);

CREATE TABLE diary_like_milestone_state (
                                            id BIGINT NOT NULL AUTO_INCREMENT,
                                            diary_id BIGINT NOT NULL,
                                            last_notified_milestone INT NOT NULL,
                                            PRIMARY KEY (id),
                                            CONSTRAINT uk_diary_like_milestone_state_diary UNIQUE (diary_id),
                                            CONSTRAINT fk_diary_like_milestone_state_diary FOREIGN KEY (diary_id) REFERENCES diary (id)
);

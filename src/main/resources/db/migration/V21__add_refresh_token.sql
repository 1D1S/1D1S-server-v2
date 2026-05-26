CREATE TABLE `refresh_token` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `member_id` BIGINT NOT NULL,
    `refresh_token` VARCHAR(512) NOT NULL,
    `revoked` BIT(1) NOT NULL DEFAULT b'0',
    `revoked_at` DATETIME(6) DEFAULT NULL,
    `expires_at` DATETIME(6) NOT NULL,
    `created_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_refresh_token_token` (`refresh_token`),
    KEY `idx_refresh_token_member_id` (`member_id`),
    CONSTRAINT `fk_refresh_token_member`
        FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

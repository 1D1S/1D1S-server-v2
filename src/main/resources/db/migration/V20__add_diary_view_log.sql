-- 일지 시청 기록 테이블 생성
-- V14__add_diary_view_log.sql

CREATE TABLE `diary_view_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `member_id` BIGINT NOT NULL,
    `diary_id` BIGINT NOT NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_diary_view_log_member_diary` (`member_id`, `diary_id`),
    KEY `fk_diary_view_log_member` (`member_id`),
    KEY `fk_diary_view_log_diary` (`diary_id`),
    CONSTRAINT `fk_diary_view_log_member` FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`) ON DELETE CASCADE,
    CONSTRAINT `fk_diary_view_log_diary` FOREIGN KEY (`diary_id`) REFERENCES `diary` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
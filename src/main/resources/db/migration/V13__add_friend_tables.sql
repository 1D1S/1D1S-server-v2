-- 친구 기능 테이블 생성
-- V13__add_friend_tables.sql

-- 1. 친구 신청 관리 테이블
CREATE TABLE `friend_request` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `from_member_id` BIGINT NOT NULL,
    `to_member_id` BIGINT NOT NULL,
    `status` ENUM('PENDING', 'ACCEPTED', 'REJECTED', 'CANCELED') NOT NULL DEFAULT 'PENDING',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_friend_request_from_to` (`from_member_id`, `to_member_id`),
    KEY `fk_friend_request_from_member` (`from_member_id`),
    KEY `fk_friend_request_to_member` (`to_member_id`),
    CONSTRAINT `fk_friend_request_from_member` FOREIGN KEY (`from_member_id`) REFERENCES `member` (`member_id`) ON DELETE CASCADE,
    CONSTRAINT `fk_friend_request_to_member` FOREIGN KEY (`to_member_id`) REFERENCES `member` (`member_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 2. 친구 관계 테이블 (맞팔로우)
CREATE TABLE `friend` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `member_id` BIGINT NOT NULL,
    `friend_member_id` BIGINT NOT NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_friend_member_friend` (`member_id`, `friend_member_id`),
    KEY `fk_friend_member` (`member_id`),
    KEY `fk_friend_friend_member` (`friend_member_id`),
    CONSTRAINT `fk_friend_member` FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`) ON DELETE CASCADE,
    CONSTRAINT `fk_friend_friend_member` FOREIGN KEY (`friend_member_id`) REFERENCES `member` (`member_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 3. 차단 목록 테이블
CREATE TABLE `block_list` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `member_id` BIGINT NOT NULL,
    `blocked_member_id` BIGINT NOT NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_block_list_member_blocked` (`member_id`, `blocked_member_id`),
    KEY `fk_block_list_member` (`member_id`),
    KEY `fk_block_list_blocked_member` (`blocked_member_id`),
    CONSTRAINT `fk_block_list_member` FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`) ON DELETE CASCADE,
    CONSTRAINT `fk_block_list_blocked_member` FOREIGN KEY (`blocked_member_id`) REFERENCES `member` (`member_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
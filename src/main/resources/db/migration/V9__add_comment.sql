CREATE TABLE `comment` (
  `id`         bigint       NOT NULL AUTO_INCREMENT,
  `content`    text         NOT NULL,
  `is_deleted` bit(1)       NOT NULL DEFAULT b'0',
  `created_at` datetime(6)  NOT NULL,
  `member_id`  bigint       NOT NULL,
  `diary_id`   bigint       NOT NULL,
  `parent_id`  bigint       DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_comment_member`  (`member_id`),
  KEY `FK_comment_diary`   (`diary_id`),
  KEY `FK_comment_parent`  (`parent_id`),
  CONSTRAINT `FK_comment_member`  FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`),
  CONSTRAINT `FK_comment_diary`   FOREIGN KEY (`diary_id`)  REFERENCES `diary`  (`id`),
  CONSTRAINT `FK_comment_parent`  FOREIGN KEY (`parent_id`) REFERENCES `comment` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

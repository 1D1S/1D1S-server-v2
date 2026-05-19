CREATE TABLE `comment_report` (
  `id`         bigint       NOT NULL AUTO_INCREMENT,
  `type`       varchar(20)  DEFAULT NULL,
  `content`    varchar(255) DEFAULT NULL,
  `created_at` datetime(6)  NOT NULL,
  `member_id`  bigint       NOT NULL,
  `comment_id` bigint       NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_comment_report_member`  (`member_id`),
  KEY `FK_comment_report_comment` (`comment_id`),
  CONSTRAINT `FK_comment_report_member`  FOREIGN KEY (`member_id`)  REFERENCES `member`  (`member_id`),
  CONSTRAINT `FK_comment_report_comment` FOREIGN KEY (`comment_id`) REFERENCES `comment` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

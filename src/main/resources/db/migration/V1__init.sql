CREATE TABLE `member` (
                          `member_id` bigint NOT NULL AUTO_INCREMENT,
                          `birth` date DEFAULT NULL,
                          `email` varchar(255) NOT NULL,
                          `gender` enum('ETC','FEMALE','MALE') DEFAULT NULL,
                          `is_public` bit(1) DEFAULT NULL,
                          `job` enum('STUDENT','WORKER') DEFAULT NULL,
                          `nickname` varchar(255) DEFAULT NULL,
                          `nickname_last_modified_at` datetime(6) DEFAULT NULL,
                          `profile_url` varchar(255) DEFAULT NULL,
                          `refresh_token` varchar(255) DEFAULT NULL,
                          `role` enum('ADMIN','GUEST','USER') DEFAULT NULL,
                          `signup_route` enum('APPLE','GOOGLE','KAKAO','NAVER') DEFAULT NULL,
                          `social_id` varchar(255) DEFAULT NULL,
                          PRIMARY KEY (`member_id`),
                          UNIQUE KEY `UKmbmcqelty0fbrvxp1q58dn57t` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `challenge` (
                             `id` bigint NOT NULL AUTO_INCREMENT,
                             `category` enum('BOOK','DEV','ECONOMY','EXERCISE','LEISURE','MUSIC','STUDY') DEFAULT NULL,
                             `description` varchar(255) DEFAULT NULL,
                             `end_date` date DEFAULT NULL,
                             `max_participants_cnt` bigint DEFAULT NULL,
                             `start_date` date DEFAULT NULL,
                             `title` varchar(255) DEFAULT NULL,
                             `type` enum('FIXED','FLEXIBLE') DEFAULT NULL,
                             `host_member_id` bigint DEFAULT NULL,
                             PRIMARY KEY (`id`),
                             KEY `FK19py0gjl0otrq0l14aqkkgy2s` (`host_member_id`),
                             CONSTRAINT `FK19py0gjl0otrq0l14aqkkgy2s` FOREIGN KEY (`host_member_id`) REFERENCES `member` (`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `participant` (
                               `id` bigint NOT NULL AUTO_INCREMENT,
                               `status` enum('HOST','LEAVE','NONE','PARTICIPANT','PENDING','REJECTED') DEFAULT NULL,
                               `challenge_id` bigint DEFAULT NULL,
                               `member_id` bigint DEFAULT NULL,
                               PRIMARY KEY (`id`),
                               KEY `FK9bihkakkpkw1udh8ov82d5oth` (`challenge_id`),
                               KEY `FKltl2dk33cemkl32l5fo3jmh85` (`member_id`),
                               CONSTRAINT `FK9bihkakkpkw1udh8ov82d5oth` FOREIGN KEY (`challenge_id`) REFERENCES `challenge` (`id`),
                               CONSTRAINT `FKltl2dk33cemkl32l5fo3jmh85` FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `challenge_goal` (
                                  `id` bigint NOT NULL AUTO_INCREMENT,
                                  `content` varchar(255) DEFAULT NULL,
                                  `participant_id` bigint DEFAULT NULL,
                                  PRIMARY KEY (`id`),
                                  KEY `FKbrcjmn71184wxrhr059sxm9bm` (`participant_id`),
                                  CONSTRAINT `FKbrcjmn71184wxrhr059sxm9bm` FOREIGN KEY (`participant_id`) REFERENCES `participant` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `challenge_like` (
                                  `id` bigint NOT NULL AUTO_INCREMENT,
                                  `challenge_id` bigint DEFAULT NULL,
                                  `member_id` bigint DEFAULT NULL,
                                  PRIMARY KEY (`id`),
                                  KEY `FKj62k3nx428e77lfhv91g7hkjw` (`challenge_id`),
                                  KEY `FKruk0uw2n2hqexms7xbly0683l` (`member_id`),
                                  CONSTRAINT `FKj62k3nx428e77lfhv91g7hkjw` FOREIGN KEY (`challenge_id`) REFERENCES `challenge` (`id`),
                                  CONSTRAINT `FKruk0uw2n2hqexms7xbly0683l` FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `diary` (
                         `id` bigint NOT NULL AUTO_INCREMENT,
                         `completed_date` date DEFAULT NULL,
                         `content` text,
                         `created_date` date DEFAULT NULL,
                         `feeling` enum('HAPPY','NONE','NORMAL','SAD') DEFAULT NULL,
                         `is_all_goals_completed` bit(1) DEFAULT NULL,
                         `is_deleted` bit(1) DEFAULT NULL,
                         `is_public` bit(1) DEFAULT NULL,
                         `title` varchar(255) DEFAULT NULL,
                         `challenge_id` bigint DEFAULT NULL,
                         `member_id` bigint DEFAULT NULL,
                         PRIMARY KEY (`id`),
                         KEY `FK9x8gxi4kdi4cv7j3gjs02gisq` (`challenge_id`),
                         KEY `FKbyluyva0mxnf5jitf297oxlxd` (`member_id`),
                         CONSTRAINT `FK9x8gxi4kdi4cv7j3gjs02gisq` FOREIGN KEY (`challenge_id`) REFERENCES `challenge` (`id`),
                         CONSTRAINT `FKbyluyva0mxnf5jitf297oxlxd` FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `diary_goal` (
                              `id` bigint NOT NULL AUTO_INCREMENT,
                              `is_completed` bit(1) DEFAULT NULL,
                              `challenge_goal_id` bigint DEFAULT NULL,
                              `diary_id` bigint DEFAULT NULL,
                              PRIMARY KEY (`id`),
                              KEY `FKceva8x9ercvmrjkkhqv6dhxx9` (`challenge_goal_id`),
                              KEY `FKnvw26ci45aq61y5xd3vvlw459` (`diary_id`),
                              CONSTRAINT `FKceva8x9ercvmrjkkhqv6dhxx9` FOREIGN KEY (`challenge_goal_id`) REFERENCES `challenge_goal` (`id`),
                              CONSTRAINT `FKnvw26ci45aq61y5xd3vvlw459` FOREIGN KEY (`diary_id`) REFERENCES `diary` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `diary_image` (
                               `id` bigint NOT NULL AUTO_INCREMENT,
                               `image_url` varchar(255) DEFAULT NULL,
                               `diary_id` bigint DEFAULT NULL,
                               PRIMARY KEY (`id`),
                               KEY `FKlqq77k7ifchoc8dcak4jyb06y` (`diary_id`),
                               CONSTRAINT `FKlqq77k7ifchoc8dcak4jyb06y` FOREIGN KEY (`diary_id`) REFERENCES `diary` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `diary_like` (
                              `id` bigint NOT NULL AUTO_INCREMENT,
                              `diary_id` bigint DEFAULT NULL,
                              `member_id` bigint DEFAULT NULL,
                              PRIMARY KEY (`id`),
                              KEY `FKf9vqiq1mrm6xa7d1cdccg2m7e` (`diary_id`),
                              KEY `FKdywrufxe5m8fopkv5nbv8kjku` (`member_id`),
                              CONSTRAINT `FKf9vqiq1mrm6xa7d1cdccg2m7e` FOREIGN KEY (`diary_id`) REFERENCES `diary` (`id`),
                              CONSTRAINT `FKdywrufxe5m8fopkv5nbv8kjku` FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `diary_report` (
                                `id` bigint NOT NULL AUTO_INCREMENT,
                                `created_date` date DEFAULT NULL,
                                `diary_id` bigint DEFAULT NULL,
                                `member_id` bigint DEFAULT NULL,
                                PRIMARY KEY (`id`),
                                KEY `FKf0u7b0hp2cq5s8h7d3p7d00v1` (`diary_id`),
                                KEY `FKhv1n6fougjf9w5tr6g1f3gb7v` (`member_id`),
                                CONSTRAINT `FKf0u7b0hp2cq5s8h7d3p7d00v1` FOREIGN KEY (`diary_id`) REFERENCES `diary` (`id`),
                                CONSTRAINT `FKhv1n6fougjf9w5tr6g1f3gb7v` FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `interest` (
                            `id` bigint NOT NULL AUTO_INCREMENT,
                            `category` enum('BOOK','DEV','ECONOMY','EXERCISE','LEISURE','MUSIC','STUDY') DEFAULT NULL,
                            `member_id` bigint DEFAULT NULL,
                            PRIMARY KEY (`id`),
                            KEY `FKmejotk04k93xwh9v101agbduv` (`member_id`),
                            CONSTRAINT `FKmejotk04k93xwh9v101agbduv` FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `challenge_poke` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `challenge_id` BIGINT NOT NULL,
    `actor_member_id` BIGINT NOT NULL,
    `receiver_member_id` BIGINT NOT NULL,
    `poked_date` DATE NOT NULL,
    `created_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_challenge_poke_daily` (
        `challenge_id`,
        `actor_member_id`,
        `receiver_member_id`,
        `poked_date`
    ),
    KEY `idx_challenge_poke_receiver_date` (`receiver_member_id`, `poked_date`),
    CONSTRAINT `fk_challenge_poke_challenge`
        FOREIGN KEY (`challenge_id`) REFERENCES `challenge` (`id`),
    CONSTRAINT `fk_challenge_poke_actor`
        FOREIGN KEY (`actor_member_id`) REFERENCES `member` (`member_id`),
    CONSTRAINT `fk_challenge_poke_receiver`
        FOREIGN KEY (`receiver_member_id`) REFERENCES `member` (`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

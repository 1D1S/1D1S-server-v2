ALTER TABLE `challenge`
  ADD COLUMN `challenge_type` varchar(20) NOT NULL DEFAULT 'PUBLIC',
  ADD COLUMN `password`       varchar(255) DEFAULT NULL;

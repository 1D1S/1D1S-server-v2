-- MySQL dump 10.13  Distrib 8.0.44, for macos15 (arm64)
--
-- Host: localhost    Database: odos
-- ------------------------------------------------------
-- Server version	8.4.7

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `challenge`
--

DROP TABLE IF EXISTS `challenge`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `challenge`
--

LOCK TABLES `challenge` WRITE;
/*!40000 ALTER TABLE `challenge` DISABLE KEYS */;
/*!40000 ALTER TABLE `challenge` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `challenge_goal`
--

DROP TABLE IF EXISTS `challenge_goal`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `challenge_goal` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `content` varchar(255) DEFAULT NULL,
  `participant_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKbrcjmn71184wxrhr059sxm9bm` (`participant_id`),
  CONSTRAINT `FKbrcjmn71184wxrhr059sxm9bm` FOREIGN KEY (`participant_id`) REFERENCES `participant` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `challenge_goal`
--

LOCK TABLES `challenge_goal` WRITE;
/*!40000 ALTER TABLE `challenge_goal` DISABLE KEYS */;
/*!40000 ALTER TABLE `challenge_goal` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `challenge_like`
--

DROP TABLE IF EXISTS `challenge_like`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `challenge_like`
--

LOCK TABLES `challenge_like` WRITE;
/*!40000 ALTER TABLE `challenge_like` DISABLE KEYS */;
/*!40000 ALTER TABLE `challenge_like` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `diary`
--

DROP TABLE IF EXISTS `diary`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `diary`
--

LOCK TABLES `diary` WRITE;
/*!40000 ALTER TABLE `diary` DISABLE KEYS */;
/*!40000 ALTER TABLE `diary` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `diary_goal`
--

DROP TABLE IF EXISTS `diary_goal`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `diary_goal`
--

LOCK TABLES `diary_goal` WRITE;
/*!40000 ALTER TABLE `diary_goal` DISABLE KEYS */;
/*!40000 ALTER TABLE `diary_goal` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `diary_image`
--

DROP TABLE IF EXISTS `diary_image`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `diary_image` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `url` varchar(255) DEFAULT NULL,
  `diary_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKlqq77k7ifchoc8dcak4jyb06y` (`diary_id`),
  CONSTRAINT `FKlqq77k7ifchoc8dcak4jyb06y` FOREIGN KEY (`diary_id`) REFERENCES `diary` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `diary_image`
--

LOCK TABLES `diary_image` WRITE;
/*!40000 ALTER TABLE `diary_image` DISABLE KEYS */;
/*!40000 ALTER TABLE `diary_image` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `diary_like`
--

DROP TABLE IF EXISTS `diary_like`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `diary_like` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `diary_id` bigint DEFAULT NULL,
  `member_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKo0ulysilqhe457lore9mdmokx` (`diary_id`),
  KEY `FKdywrufxe5m8fopkv5nbv8kjku` (`member_id`),
  CONSTRAINT `FKdywrufxe5m8fopkv5nbv8kjku` FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`),
  CONSTRAINT `FKo0ulysilqhe457lore9mdmokx` FOREIGN KEY (`diary_id`) REFERENCES `diary` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `diary_like`
--

LOCK TABLES `diary_like` WRITE;
/*!40000 ALTER TABLE `diary_like` DISABLE KEYS */;
/*!40000 ALTER TABLE `diary_like` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `diary_report`
--

DROP TABLE IF EXISTS `diary_report`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `diary_report` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `content` varchar(255) DEFAULT NULL,
  `type` enum('BAD_IMAGE','BAD_TITLE_CONTENT','ETC') DEFAULT NULL,
  `diary_id` bigint DEFAULT NULL,
  `member_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKbweb2p32isvd8btt0fowjnhwk` (`diary_id`),
  KEY `FK7pdjwa8h1ubvsik77a7cbjoxs` (`member_id`),
  CONSTRAINT `FK7pdjwa8h1ubvsik77a7cbjoxs` FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`),
  CONSTRAINT `FKbweb2p32isvd8btt0fowjnhwk` FOREIGN KEY (`diary_id`) REFERENCES `diary` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `diary_report`
--

LOCK TABLES `diary_report` WRITE;
/*!40000 ALTER TABLE `diary_report` DISABLE KEYS */;
/*!40000 ALTER TABLE `diary_report` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `interest`
--

DROP TABLE IF EXISTS `interest`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `interest` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `category` enum('BOOK','DEV','ECONOMY','EXERCISE','LEISURE','MUSIC','STUDY') DEFAULT NULL,
  `member_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKmejotk04k93xwh9v101agbduv` (`member_id`),
  CONSTRAINT `FKmejotk04k93xwh9v101agbduv` FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `interest`
--

LOCK TABLES `interest` WRITE;
/*!40000 ALTER TABLE `interest` DISABLE KEYS */;
/*!40000 ALTER TABLE `interest` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `member`
--

DROP TABLE IF EXISTS `member`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `member`
--

LOCK TABLES `member` WRITE;
/*!40000 ALTER TABLE `member` DISABLE KEYS */;
/*!40000 ALTER TABLE `member` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `participant`
--

DROP TABLE IF EXISTS `participant`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `participant`
--

LOCK TABLES `participant` WRITE;
/*!40000 ALTER TABLE `participant` DISABLE KEYS */;
/*!40000 ALTER TABLE `participant` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-02-05 17:14:32

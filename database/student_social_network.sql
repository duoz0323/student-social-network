-- MySQL dump 10.13  Distrib 8.0.45, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: student_social_network
-- ------------------------------------------------------
-- Server version	8.0.36

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
-- Table structure for table `account_status_histories`
--

DROP TABLE IF EXISTS `account_status_histories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `account_status_histories` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint unsigned NOT NULL,
  `old_status` enum('ACTIVE','BLOCKED') NOT NULL,
  `new_status` enum('ACTIVE','BLOCKED') NOT NULL,
  `changed_by` bigint unsigned NOT NULL,
  `reason` varchar(500) NOT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  KEY `fk_account_status_histories_changed_by` (`changed_by`),
  KEY `idx_account_status_histories_user_created` (`user_id`,`created_at` DESC,`id` DESC),
  CONSTRAINT `fk_account_status_histories_changed_by` FOREIGN KEY (`changed_by`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_account_status_histories_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `chk_account_status_histories_changed` CHECK ((`old_status` <> `new_status`))
) ENGINE=InnoDB AUTO_INCREMENT=29 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `account_status_histories`
--

LOCK TABLES `account_status_histories` WRITE;
/*!40000 ALTER TABLE `account_status_histories` DISABLE KEYS */;
INSERT INTO `account_status_histories` VALUES (9,10,'ACTIVE','BLOCKED',15,'SPAM','2026-07-14 23:38:01.312526'),(10,10,'BLOCKED','ACTIVE',15,'ADMIN_UNBLOCK','2026-07-14 23:38:20.551588'),(11,10,'ACTIVE','BLOCKED',15,'SPAM','2026-07-14 23:38:38.754308'),(20,8,'ACTIVE','BLOCKED',15,'SPAM','2026-07-15 15:07:36.310503');
/*!40000 ALTER TABLE `account_status_histories` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `admin_actions`
--

DROP TABLE IF EXISTS `admin_actions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `admin_actions` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `admin_id` bigint unsigned NOT NULL,
  `action_type` enum('BLOCK_USER','UNBLOCK_USER','HIDE_POST','RESTORE_POST','RESOLVE_REPORT','REJECT_REPORT') NOT NULL,
  `target_type` enum('USER','POST','REPORT') NOT NULL,
  `target_id` bigint unsigned NOT NULL,
  `note` varchar(1000) DEFAULT NULL,
  `old_data` json DEFAULT NULL,
  `new_data` json DEFAULT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  KEY `idx_admin_actions_target_created` (`target_type`,`target_id`,`created_at` DESC,`id` DESC),
  KEY `idx_admin_actions_admin_created` (`admin_id`,`created_at` DESC,`id` DESC),
  CONSTRAINT `fk_admin_actions_admin` FOREIGN KEY (`admin_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=80 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `admin_actions`
--

LOCK TABLES `admin_actions` WRITE;
/*!40000 ALTER TABLE `admin_actions` DISABLE KEYS */;
INSERT INTO `admin_actions` VALUES (7,15,'BLOCK_USER','USER',10,'SPAM',NULL,NULL,'2026-07-14 23:38:01.317031'),(8,15,'UNBLOCK_USER','USER',10,'ADMIN_UNBLOCK',NULL,NULL,'2026-07-14 23:38:20.555891'),(9,15,'BLOCK_USER','USER',10,'SPAM',NULL,NULL,'2026-07-14 23:38:38.758109'),(27,15,'HIDE_POST','POST',5,'VIOLENCE',NULL,NULL,'2026-07-15 14:58:14.109560'),(28,15,'RESTORE_POST','POST',5,'ADMIN_RESTORE',NULL,NULL,'2026-07-15 15:02:31.995043'),(29,15,'BLOCK_USER','USER',8,'SPAM',NULL,NULL,'2026-07-15 15:07:36.315380'),(77,15,'REJECT_REPORT','REPORT',1,'Không phát hiện nội dung vi phạm',NULL,NULL,'2026-07-16 00:30:23.350958'),(78,15,'RESOLVE_REPORT','REPORT',2,'Báo cáo hợp lệ, bài viết chứa nội dung spam',NULL,NULL,'2026-07-16 00:41:08.632901'),(79,15,'HIDE_POST','POST',1,'SPAM',NULL,NULL,'2026-07-16 00:41:08.636668');
/*!40000 ALTER TABLE `admin_actions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `comments`
--

DROP TABLE IF EXISTS `comments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `comments` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `post_id` bigint unsigned NOT NULL,
  `user_id` bigint unsigned NOT NULL,
  `parent_comment_id` bigint unsigned DEFAULT NULL,
  `content` varchar(1000) NOT NULL,
  `status` enum('PUBLISHED','DELETED') NOT NULL DEFAULT 'PUBLISHED',
  `deleted_at` datetime(6) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  KEY `fk_comments_parent` (`parent_comment_id`),
  KEY `idx_comments_post_parent_created` (`post_id`,`parent_comment_id`,`status`,`created_at`,`id`),
  KEY `idx_comments_user_created` (`user_id`,`status`,`created_at` DESC,`id` DESC),
  CONSTRAINT `fk_comments_parent` FOREIGN KEY (`parent_comment_id`) REFERENCES `comments` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_comments_post` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_comments_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `chk_comments_content_not_empty` CHECK ((char_length(trim(`content`)) > 0)),
  CONSTRAINT `chk_comments_deleted_state` CHECK ((((`status` = _utf8mb4'DELETED') and (`deleted_at` is not null)) or ((`status` = _utf8mb4'PUBLISHED') and (`deleted_at` is null))))
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `comments`
--

LOCK TABLES `comments` WRITE;
/*!40000 ALTER TABLE `comments` DISABLE KEYS */;
INSERT INTO `comments` VALUES (1,3,8,NULL,'Binh luan dau tien tren bai viet nay','DELETED','2026-07-10 15:35:06.981962','2026-07-10 15:32:34.887118','2026-07-10 15:35:07.012372'),(2,3,8,NULL,'hom nay ban the nao','PUBLISHED',NULL,'2026-07-10 15:33:46.070398','2026-07-10 15:33:46.070398'),(3,76,9,NULL,'thật tuyệt vời','PUBLISHED',NULL,'2026-07-18 07:02:31.281239','2026-07-18 07:02:31.281239'),(4,76,7,3,'Tôi cũng nghĩ vậy','PUBLISHED',NULL,'2026-07-18 07:04:09.775361','2026-07-18 07:04:09.775361'),(5,76,12,NULL,'thật tuyệt vời','PUBLISHED',NULL,'2026-07-18 20:21:08.887865','2026-07-18 20:21:08.887865');
/*!40000 ALTER TABLE `comments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `follows`
--

DROP TABLE IF EXISTS `follows`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `follows` (
  `follower_id` bigint unsigned NOT NULL,
  `following_id` bigint unsigned NOT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`follower_id`,`following_id`),
  KEY `idx_follows_following_created_at` (`following_id`,`created_at` DESC,`follower_id` DESC),
  KEY `idx_follows_follower_created_at` (`follower_id`,`created_at` DESC,`following_id` DESC),
  CONSTRAINT `fk_follows_follower` FOREIGN KEY (`follower_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_follows_following` FOREIGN KEY (`following_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `chk_follows_not_self` CHECK ((`follower_id` <> `following_id`))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `follows`
--

LOCK TABLES `follows` WRITE;
/*!40000 ALTER TABLE `follows` DISABLE KEYS */;
INSERT INTO `follows` VALUES (8,4,'2026-07-12 16:58:12.516149'),(8,5,'2026-07-12 16:58:40.511789'),(8,6,'2026-07-12 16:58:47.656400'),(12,7,'2026-07-18 20:11:22.991843'),(9,8,'2026-07-12 17:13:58.000954'),(8,9,'2026-07-12 16:58:59.224449'),(8,10,'2026-07-12 16:59:03.127881'),(8,11,'2026-07-12 16:59:06.534235');
/*!40000 ALTER TABLE `follows` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `hashtags`
--

DROP TABLE IF EXISTS `hashtags`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `hashtags` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `normalized_name` varchar(100) NOT NULL,
  `display_name` varchar(100) NOT NULL,
  `post_count` int unsigned NOT NULL DEFAULT '0',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_hashtags_normalized_name` (`normalized_name`),
  KEY `idx_hashtags_post_count` (`post_count` DESC,`id` DESC),
  CONSTRAINT `chk_hashtags_not_empty` CHECK ((char_length(`normalized_name`) > 0))
) ENGINE=InnoDB AUTO_INCREMENT=29 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `hashtags`
--

LOCK TABLES `hashtags` WRITE;
/*!40000 ALTER TABLE `hashtags` DISABLE KEYS */;
INSERT INTO `hashtags` VALUES (1,'sinhvien','sinhvien',5,'2026-07-02 19:17:46.756496','2026-07-07 16:57:38.349633'),(5,'ngay7thang7','ngay7thang7',0,'2026-07-07 09:37:29.458061','2026-07-17 07:34:37.475461'),(6,'ronaldo','ronaldo',0,'2026-07-07 09:37:29.472068','2026-07-17 07:34:37.475461'),(12,'messi','messi',0,'2026-07-07 16:51:35.043986','2026-07-17 07:34:37.475461'),(26,'luận văn','luận văn',3,'2026-07-17 15:30:18.064501','2026-07-18 05:45:57.204274');
/*!40000 ALTER TABLE `hashtags` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notifications`
--

DROP TABLE IF EXISTS `notifications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notifications` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `recipient_id` bigint unsigned NOT NULL,
  `actor_id` bigint unsigned DEFAULT NULL,
  `type` enum('FOLLOW','POST_LIKE','POST_COMMENT','COMMENT_REPLY','REPORT_RESOLVED','REPORT_REJECTED','POST_HIDDEN_BY_ADMIN','POST_RESTORED_BY_ADMIN','ACCOUNT_BLOCKED','ACCOUNT_UNBLOCKED') NOT NULL,
  `post_id` bigint unsigned DEFAULT NULL,
  `comment_id` bigint unsigned DEFAULT NULL,
  `report_id` bigint unsigned DEFAULT NULL,
  `payload` json DEFAULT NULL,
  `read_at` datetime(6) DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  KEY `fk_notifications_recipient` (`recipient_id`),
  KEY `fk_notifications_actor` (`actor_id`),
  KEY `fk_notifications_post` (`post_id`),
  KEY `fk_notifications_comment` (`comment_id`),
  KEY `fk_notifications_report` (`report_id`),
  CONSTRAINT `fk_notifications_actor` FOREIGN KEY (`actor_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_notifications_comment` FOREIGN KEY (`comment_id`) REFERENCES `comments` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_notifications_post` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_notifications_recipient` FOREIGN KEY (`recipient_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_notifications_report` FOREIGN KEY (`report_id`) REFERENCES `reports` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `chk_notifications_actor_by_type` CHECK ((((`type` in (_utf8mb4'FOLLOW',_utf8mb4'POST_LIKE',_utf8mb4'POST_COMMENT',_utf8mb4'COMMENT_REPLY')) and (`actor_id` is not null)) or ((`type` in (_utf8mb4'REPORT_RESOLVED',_utf8mb4'REPORT_REJECTED',_utf8mb4'POST_HIDDEN_BY_ADMIN',_utf8mb4'POST_RESTORED_BY_ADMIN',_utf8mb4'ACCOUNT_BLOCKED',_utf8mb4'ACCOUNT_UNBLOCKED')) and (`actor_id` is null)))),
  CONSTRAINT `chk_notifications_not_self` CHECK (((`actor_id` is null) or (`actor_id` <> `recipient_id`)))
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notifications`
--

LOCK TABLES `notifications` WRITE;
/*!40000 ALTER TABLE `notifications` DISABLE KEYS */;
INSERT INTO `notifications` VALUES (1,7,12,'POST_LIKE',76,NULL,NULL,NULL,'2026-07-18 20:14:03.439701',NULL,'2026-07-18 20:08:26.976098','2026-07-18 20:14:03.448161'),(2,7,12,'FOLLOW',NULL,NULL,NULL,NULL,NULL,NULL,'2026-07-18 20:11:22.997508','2026-07-18 20:11:22.997508'),(3,7,12,'POST_COMMENT',76,5,NULL,NULL,NULL,NULL,'2026-07-18 20:21:08.901683','2026-07-18 20:21:08.901683');
/*!40000 ALTER TABLE `notifications` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `password_reset_tokens`
--

DROP TABLE IF EXISTS `password_reset_tokens`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `password_reset_tokens` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint unsigned NOT NULL,
  `delivery_channel` enum('EMAIL','SMS') NOT NULL,
  `token_hash` char(64) NOT NULL,
  `expires_at` datetime(6) NOT NULL,
  `used_at` datetime(6) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_password_reset_tokens_hash` (`token_hash`),
  KEY `idx_password_reset_tokens_user_state` (`user_id`,`used_at`,`expires_at`,`id`),
  CONSTRAINT `fk_password_reset_tokens_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `chk_password_reset_tokens_expiry` CHECK ((`expires_at` > `created_at`)),
  CONSTRAINT `chk_password_reset_tokens_used_at` CHECK (((`used_at` is null) or (`used_at` >= `created_at`)))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `password_reset_tokens`
--

LOCK TABLES `password_reset_tokens` WRITE;
/*!40000 ALTER TABLE `password_reset_tokens` DISABLE KEYS */;
/*!40000 ALTER TABLE `password_reset_tokens` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `post_hashtags`
--

DROP TABLE IF EXISTS `post_hashtags`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `post_hashtags` (
  `post_id` bigint unsigned NOT NULL,
  `hashtag_id` bigint unsigned NOT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`post_id`,`hashtag_id`),
  UNIQUE KEY `uq_post_hashtags_post` (`post_id`),
  KEY `idx_post_hashtags_hashtag_post` (`hashtag_id`,`post_id` DESC),
  CONSTRAINT `fk_post_hashtags_hashtag` FOREIGN KEY (`hashtag_id`) REFERENCES `hashtags` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_post_hashtags_post` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `post_hashtags`
--

LOCK TABLES `post_hashtags` WRITE;
/*!40000 ALTER TABLE `post_hashtags` DISABLE KEYS */;
INSERT INTO `post_hashtags` VALUES (2,1,'2026-07-02 19:17:46.953892'),(3,1,'2026-07-02 19:19:58.285777'),(4,1,'2026-07-07 08:59:04.182672'),(5,1,'2026-07-07 09:37:29.591392'),(6,1,'2026-07-07 16:57:38.349633'),(74,26,'2026-07-17 15:30:18.101507'),(75,26,'2026-07-17 19:11:11.811375'),(76,26,'2026-07-18 05:45:57.204274');
/*!40000 ALTER TABLE `post_hashtags` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `post_likes`
--

DROP TABLE IF EXISTS `post_likes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `post_likes` (
  `user_id` bigint unsigned NOT NULL,
  `post_id` bigint unsigned NOT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`user_id`,`post_id`),
  KEY `idx_post_likes_post_created` (`post_id`,`created_at` DESC,`user_id` DESC),
  CONSTRAINT `fk_post_likes_post` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_post_likes_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `post_likes`
--

LOCK TABLES `post_likes` WRITE;
/*!40000 ALTER TABLE `post_likes` DISABLE KEYS */;
INSERT INTO `post_likes` VALUES (8,1,'2026-07-10 14:42:35.759539'),(12,76,'2026-07-18 20:08:26.968619');
/*!40000 ALTER TABLE `post_likes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `post_media`
--

DROP TABLE IF EXISTS `post_media`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `post_media` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `post_id` bigint unsigned NOT NULL,
  `media_url` varchar(1000) NOT NULL,
  `storage_public_id` varchar(255) NOT NULL,
  `media_type` enum('IMAGE','VIDEO') NOT NULL,
  `mime_type` enum('image/jpeg','image/png','image/webp','video/mp4','video/webm') NOT NULL,
  `file_size_bytes` bigint unsigned NOT NULL,
  `width_px` int unsigned DEFAULT NULL,
  `height_px` int unsigned DEFAULT NULL,
  `duration_seconds` int unsigned DEFAULT NULL,
  `thumbnail_url` varchar(1000) DEFAULT NULL,
  `display_order` tinyint unsigned NOT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_post_media_storage_public_id` (`storage_public_id`),
  UNIQUE KEY `uq_post_media_post_order` (`post_id`,`display_order`),
  CONSTRAINT `fk_post_media_post` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `chk_post_media_display_order` CHECK ((`display_order` <= 3)),
  CONSTRAINT `chk_post_media_duration` CHECK ((((`media_type` = _utf8mb4'IMAGE') and (`duration_seconds` is null)) or ((`media_type` = _utf8mb4'VIDEO') and (`duration_seconds` between 1 and 180)))),
  CONSTRAINT `chk_post_media_file_size` CHECK ((`file_size_bytes` > 0))
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `post_media`
--

LOCK TABLES `post_media` WRITE;
/*!40000 ALTER TABLE `post_media` DISABLE KEYS */;
INSERT INTO `post_media` VALUES (1,3,'https://res.cloudinary.com/xsypft9k/image/upload/v1783020003/student-social-network/posts/kx1ljvovafmddcph9uq6.jpg','student-social-network/posts/kx1ljvovafmddcph9uq6','IMAGE','image/jpeg',54992,800,679,NULL,NULL,0,'2026-07-02 19:19:58.244863'),(2,4,'https://res.cloudinary.com/xsypft9k/image/upload/v1783414744/student-social-network/posts/aai4mexicnjzsdawtr9a.jpg','student-social-network/posts/aai4mexicnjzsdawtr9a','IMAGE','image/jpeg',1487703,1920,2560,NULL,NULL,0,'2026-07-07 08:59:04.074074'),(3,5,'https://res.cloudinary.com/xsypft9k/image/upload/v1783417050/student-social-network/posts/rkcp2ncgrlsafclcoxhh.jpg','student-social-network/posts/rkcp2ncgrlsafclcoxhh','IMAGE','image/jpeg',1487703,1920,2560,NULL,NULL,0,'2026-07-07 09:37:29.375675'),(4,6,'https://res.cloudinary.com/xsypft9k/image/upload/v1783417786/student-social-network/posts/ovr5ksgfwdbzgyuk9k8n.jpg','student-social-network/posts/ovr5ksgfwdbzgyuk9k8n','IMAGE','image/jpeg',1487703,1920,2560,NULL,NULL,0,'2026-07-07 16:49:45.935289'),(5,6,'https://res.cloudinary.com/xsypft9k/image/upload/v1783418256/student-social-network/posts/wyhainxbe6rl3rxan2gi.jpg','student-social-network/posts/wyhainxbe6rl3rxan2gi','IMAGE','image/jpeg',674740,1920,2560,NULL,NULL,1,'2026-07-07 16:57:38.210681'),(6,6,'https://res.cloudinary.com/xsypft9k/image/upload/v1783418259/student-social-network/posts/f664lhf0sfn2apcmss9s.jpg','student-social-network/posts/f664lhf0sfn2apcmss9s','IMAGE','image/jpeg',745384,1920,2560,NULL,NULL,2,'2026-07-07 16:57:38.217963'),(17,75,'https://res.cloudinary.com/xsypft9k/video/upload/v1784290279/student-social-network/posts/stdxxifv9h9mkhwtaiv9.mp4','student-social-network/posts/stdxxifv9h9mkhwtaiv9','VIDEO','video/mp4',4929544,720,1280,19,'https://res.cloudinary.com/xsypft9k/video/upload/v1/student-social-network/posts/stdxxifv9h9mkhwtaiv9.jpg',0,'2026-07-17 19:11:11.707985'),(19,76,'https://res.cloudinary.com/xsypft9k/image/upload/v1784328359/student-social-network/posts/qchjlwrea4xlbxpxtfcc.jpg','student-social-network/posts/qchjlwrea4xlbxpxtfcc','IMAGE','image/jpeg',1921319,2362,3543,NULL,NULL,0,'2026-07-18 05:45:57.139064'),(20,76,'https://res.cloudinary.com/xsypft9k/image/upload/v1784328362/student-social-network/posts/d0psufnpp1svgcu10lgn.jpg','student-social-network/posts/d0psufnpp1svgcu10lgn','IMAGE','image/jpeg',1921319,2362,3543,NULL,NULL,1,'2026-07-18 05:45:57.142102'),(21,76,'https://res.cloudinary.com/xsypft9k/image/upload/v1784328365/student-social-network/posts/jxwahoewdvwwmc03b1au.jpg','student-social-network/posts/jxwahoewdvwwmc03b1au','IMAGE','image/jpeg',1921319,2362,3543,NULL,NULL,2,'2026-07-18 05:45:57.145590');
/*!40000 ALTER TABLE `post_media` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `posts`
--

DROP TABLE IF EXISTS `posts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `posts` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `author_id` bigint unsigned NOT NULL,
  `content` varchar(500) DEFAULT NULL,
  `status` enum('PUBLISHED','HIDDEN','DELETED') NOT NULL DEFAULT 'PUBLISHED',
  `is_edited` tinyint(1) NOT NULL DEFAULT '0',
  `like_count` int unsigned NOT NULL DEFAULT '0',
  `comment_count` int unsigned NOT NULL DEFAULT '0',
  `published_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `hidden_by` bigint unsigned DEFAULT NULL,
  `hidden_at` datetime(6) DEFAULT NULL,
  `hidden_reason` varchar(500) DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  KEY `fk_posts_hidden_by` (`hidden_by`),
  KEY `idx_posts_author_status_published` (`author_id`,`status`,`published_at` DESC,`id` DESC),
  KEY `idx_posts_status_published` (`status`,`published_at` DESC,`id` DESC),
  KEY `idx_posts_status_engagement` (`status`,`like_count` DESC,`comment_count` DESC,`published_at` DESC,`id` DESC),
  FULLTEXT KEY `ftx_posts_content` (`content`),
  CONSTRAINT `fk_posts_author` FOREIGN KEY (`author_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_posts_hidden_by` FOREIGN KEY (`hidden_by`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `chk_posts_deleted_state` CHECK ((((`status` = _utf8mb4'DELETED') and (`deleted_at` is not null)) or (`status` <> _utf8mb4'DELETED'))),
  CONSTRAINT `chk_posts_hidden_state` CHECK ((((`status` = _utf8mb4'HIDDEN') and (`hidden_at` is not null) and (`hidden_by` is not null)) or (`status` <> _utf8mb4'HIDDEN')))
) ENGINE=InnoDB AUTO_INCREMENT=77 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `posts`
--

LOCK TABLES `posts` WRITE;
/*!40000 ALTER TABLE `posts` DISABLE KEYS */;
INSERT INTO `posts` VALUES (1,9,'Hôm nay học Spring Boot rất ổn','HIDDEN',0,1,0,'2026-07-02 19:09:51.786017',15,'2026-07-16 00:41:08.622601','SPAM',NULL,'2026-07-02 19:09:51.786017','2026-07-16 00:41:08.644216'),(2,9,'Hôm nay học Spring Boot rất ổn','PUBLISHED',0,0,0,'2026-07-02 19:17:46.685460',NULL,NULL,NULL,NULL,'2026-07-02 19:17:46.685460','2026-07-10 14:44:44.427797'),(3,9,'Hôm nay học Spring Boot Khong ổn','PUBLISHED',0,0,1,'2026-07-02 19:19:58.229237',NULL,NULL,NULL,NULL,'2026-07-02 19:19:58.229237','2026-07-10 15:35:07.012372'),(4,8,'Hom nay la 7/7','PUBLISHED',0,0,0,'2026-07-07 08:59:04.047192',NULL,NULL,NULL,NULL,'2026-07-07 08:59:04.047192','2026-07-07 08:59:04.047192'),(5,8,'Hom nay la 7/7 ronaldo bi loai','PUBLISHED',0,0,0,'2026-07-07 09:37:29.358919',NULL,NULL,NULL,NULL,'2026-07-07 09:37:29.358919','2026-07-15 15:02:32.001150'),(6,8,'Hom nay la 7/7 messi chuan bị đá vào lúc 22 giờ','DELETED',1,0,0,'2026-07-07 16:49:45.911734',NULL,NULL,NULL,'2026-07-07 17:15:46.389023','2026-07-07 16:49:45.911734','2026-07-07 17:15:46.397264'),(74,7,'Chia sẻ kinh nghiệm làm luận văn tốt nghiệp','PUBLISHED',0,0,0,'2026-07-17 15:30:17.984902',NULL,NULL,NULL,NULL,'2026-07-17 15:30:17.984902','2026-07-17 15:30:17.984902'),(75,7,'Những ngày tháng làm luận văn','PUBLISHED',0,0,0,'2026-07-17 19:11:11.679539',NULL,NULL,NULL,NULL,'2026-07-17 19:11:11.679539','2026-07-17 19:11:11.679539'),(76,7,'Sáng sớm ngày 18 tháng 7 hehe','PUBLISHED',1,1,3,'2026-07-18 05:45:57.109134',NULL,NULL,NULL,NULL,'2026-07-18 05:45:57.109134','2026-07-18 20:21:08.887865');
/*!40000 ALTER TABLE `posts` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `refresh_tokens`
--

DROP TABLE IF EXISTS `refresh_tokens`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `refresh_tokens` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint unsigned NOT NULL,
  `token_hash` char(64) NOT NULL,
  `device_id` varchar(100) DEFAULT NULL,
  `device_info` varchar(500) DEFAULT NULL,
  `ip_address` varchar(45) DEFAULT NULL,
  `expires_at` datetime(6) NOT NULL,
  `revoked_at` datetime(6) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_refresh_tokens_hash` (`token_hash`),
  KEY `idx_refresh_tokens_user_expiry` (`user_id`,`revoked_at`,`expires_at`,`id`),
  CONSTRAINT `fk_refresh_tokens_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `chk_refresh_tokens_expiry` CHECK ((`expires_at` > `created_at`)),
  CONSTRAINT `chk_refresh_tokens_revoked_at` CHECK (((`revoked_at` is null) or (`revoked_at` >= `created_at`)))
) ENGINE=InnoDB AUTO_INCREMENT=136 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `refresh_tokens`
--

LOCK TABLES `refresh_tokens` WRITE;
/*!40000 ALTER TABLE `refresh_tokens` DISABLE KEYS */;
INSERT INTO `refresh_tokens` VALUES (1,3,'9993270ffda17d0d6d90a059f5135776f53e5696ca35a945cbfc0b8a7a7d0832',NULL,NULL,NULL,'2026-07-31 16:25:53.737240',NULL,'2026-07-01 09:25:53.745597'),(2,4,'9a8933bb91102fd0d953a1555a72a0201aa3fbae995a48255de6deda5c08bf5e',NULL,NULL,NULL,'2026-07-31 16:29:53.912133',NULL,'2026-07-01 09:29:53.914840'),(3,5,'6c1f3ef7f6f876e044db9d3bc7c01a1a273b79798ef11464564f918ac09cb385',NULL,NULL,NULL,'2026-08-01 01:59:51.101478',NULL,'2026-07-01 18:59:51.112597'),(4,6,'14fb96cad6802820129bf6cd2708a1269907ff0758ce167b1cfcd2b817c4fede',NULL,NULL,NULL,'2026-08-01 02:03:13.792891',NULL,'2026-07-01 19:03:13.796477'),(5,7,'a7e6cb6981db3cc47bacb3723bb709795b11fddbddb04aad54721ad9469e28a2',NULL,NULL,NULL,'2026-08-01 10:46:51.629677',NULL,'2026-07-02 03:46:51.644152'),(6,8,'4c5368ee5046c53c0018878ce48d99a48e8c1e06c698639e1c54f3e9a2cfc987',NULL,NULL,NULL,'2026-08-01 11:16:51.239774','2026-07-15 15:07:36.285276','2026-07-02 04:16:51.248196'),(7,9,'87a704642de46d2d97cdc4e012b60a808c1abc7d2f39d1212d6d7591249f9c77',NULL,NULL,NULL,'2026-08-01 11:34:55.835972',NULL,'2026-07-02 04:34:55.838757'),(8,6,'a652d4c2d65b191b240c2c9e16ad121296174b05bcbe42c0f1b295e6c4912796','postman-device-1','Postman on Windows','0:0:0:0:0:0:0:1','2026-08-01 13:36:40.429268',NULL,'2026-07-02 06:36:40.470456'),(9,6,'656fc71b05d6d3352fe496b6c87e62ff71d094db71e209c56754af77b3bbd2f4','postman-device-1','Postman on Windows','0:0:0:0:0:0:0:1','2026-08-01 13:56:23.969068',NULL,'2026-07-02 06:56:24.013995'),(10,6,'44baae843cbc747ce496def5209a7b1c16ba83265b6334f8a8de0b9a72ce7c96','postman-device-1','Postman on Windows','0:0:0:0:0:0:0:1','2026-08-01 13:56:50.424802',NULL,'2026-07-02 06:56:50.428612'),(11,6,'41638d9fc2f0b2d6ce5e8f1bcf8c6ca7e19e05fe8b8246974e562469f8a6db3a','postman-device-1','Postman on Windows','0:0:0:0:0:0:0:1','2026-08-01 13:56:55.157526',NULL,'2026-07-02 06:56:55.159276'),(12,6,'783e24fc073a77d7888ad0afee803ea2e45ce516584df407997e5fe8ea45c73f','postman-device-1','Postman on Windows','0:0:0:0:0:0:0:1','2026-08-01 13:58:46.119728',NULL,'2026-07-02 06:58:46.122446'),(13,6,'09a29da50474ba7979651b9ff20773596c0d66a7ef4d7b9eea040e7610f7c6f3','postman-device-1','Postman on Windows','0:0:0:0:0:0:0:1','2026-08-01 14:01:04.924232',NULL,'2026-07-02 07:01:04.926544'),(14,6,'02baffc4db2e64bc398615f46db2d9557a177805c8ba5fe4fb523f0cde66eb6e','postman-device-1','Postman on Windows','0:0:0:0:0:0:0:1','2026-08-01 14:01:37.007402',NULL,'2026-07-02 07:01:37.009873'),(15,6,'ead162ccbaf83a6cc7773530e7f77aa565f51ff4f6a649074792dd902d080b34','postman-device-1','Postman on Windows','0:0:0:0:0:0:0:1','2026-08-01 14:02:45.257021','2026-07-02 14:05:30.241787','2026-07-02 07:02:45.260964'),(16,10,'41c42519c8def1f0e8759e8fcb5e53acd2f15d57d9cbd61cd8f398721ab8122c',NULL,NULL,NULL,'2026-08-01 22:21:04.345317','2026-07-14 23:38:01.283853','2026-07-02 15:21:04.353283'),(17,11,'75e46607c498d9275f66f18ad22a8fa76f66e5993db31603bb4ab93c240194df',NULL,NULL,NULL,'2026-08-01 22:29:44.244150',NULL,'2026-07-02 15:29:44.246541'),(18,11,'ce2efb4923160966aabe82d51bcc448c3c0717b7ca2eb62aa384ca1c16d889e8',NULL,NULL,'0:0:0:0:0:0:0:1','2026-08-02 01:06:12.061354',NULL,'2026-07-02 18:06:12.089002'),(19,9,'267d40becf26d347f5d960faea427049ac87437c55e143655a1d3102082a3f2d',NULL,NULL,'0:0:0:0:0:0:0:1','2026-08-02 01:06:33.139075',NULL,'2026-07-02 18:06:33.143019'),(20,9,'c8e919aea88c5d9f25fcba0ef455e9ecaac14f5149d236903c78914b19356247',NULL,NULL,'0:0:0:0:0:0:0:1','2026-08-02 02:09:30.302094',NULL,'2026-07-02 19:09:30.305174'),(21,12,'caeea74fc1157a721a184abb6c90e0221746ba674f9fe9abe0dbb99b6cbb6a75',NULL,NULL,NULL,'2026-08-02 15:54:30.370360',NULL,'2026-07-03 08:54:30.378803'),(22,9,'ae682297bb82bbcf998016a3547a59e3b916a5451066248e66b790606b15df1c',NULL,NULL,'0:0:0:0:0:0:0:1','2026-08-06 15:52:41.959747',NULL,'2026-07-07 08:52:41.996530'),(23,8,'cf36128342cb5390d36240a1e1c42596cd530064620f07de5fe73f129d6cd86a',NULL,NULL,'0:0:0:0:0:0:0:1','2026-08-06 15:58:16.517577','2026-07-15 15:07:36.285276','2026-07-07 08:58:16.520965'),(24,8,'3f3ba033a0321b67da94b61c14ce007d1ad9bd006136ba576866225ca8b6cb8e',NULL,NULL,'0:0:0:0:0:0:0:1','2026-08-06 16:33:43.568875','2026-07-15 15:07:36.285276','2026-07-07 09:33:43.609494'),(25,8,'0966106baa183c01e78b07a3d50d5a5a66a65b29704d5f297b42ec9945973c6e',NULL,NULL,'0:0:0:0:0:0:0:1','2026-08-06 16:48:48.371232','2026-07-15 15:07:36.285276','2026-07-07 16:48:48.414193'),(26,8,'32e4b7dcdebbcb9a33ba5f1cc817d978de08305344ab5a5d7501c8b59a4872d2',NULL,NULL,'0:0:0:0:0:0:0:1','2026-08-06 17:15:22.927530','2026-07-15 15:07:36.285276','2026-07-07 17:15:22.934553'),(27,8,'79830da8748a177553789ca81d887aeac5c2d2d76c9f64f2e8961f7e330b715d',NULL,NULL,'0:0:0:0:0:0:0:1','2026-08-08 20:57:25.822258','2026-07-15 15:07:36.285276','2026-07-09 20:57:25.850944'),(28,8,'488432289f843df41c5b5f4faead050b5df73a2cecce4693469c15a0956db913',NULL,NULL,'0:0:0:0:0:0:0:1','2026-08-09 14:37:35.158852','2026-07-15 15:07:36.285276','2026-07-10 14:37:35.243298'),(29,8,'ead49aec107402c5bb93b178c31141a00fc278b4c3d031c724842733a8e9c38e',NULL,NULL,'0:0:0:0:0:0:0:1','2026-08-09 15:28:05.930713','2026-07-15 15:07:36.285276','2026-07-10 15:28:05.961692'),(30,8,'242e51456f631625817ac2467c4a9d5fab6cd0049cc5582f4c5828b8d4eb4046',NULL,NULL,'0:0:0:0:0:0:0:1','2026-08-09 15:32:03.704872','2026-07-15 15:07:36.285276','2026-07-10 15:32:03.709048'),(31,8,'65d8b686e1cc4fedfd262c4f943c90d40c915b4724c8b3608eaf6ccb3b40eb94',NULL,NULL,'0:0:0:0:0:0:0:1','2026-08-11 14:32:27.904455','2026-07-15 15:07:36.285276','2026-07-12 14:32:27.937813'),(32,8,'4f258cd00a1d08ad535fee2febe4cfc6e970eeafd5103ec1b60fabe081b74afd',NULL,NULL,'0:0:0:0:0:0:0:1','2026-08-11 16:57:34.904134','2026-07-15 15:07:36.285276','2026-07-12 16:57:34.933796'),(33,8,'b5cf35c827d5f988a6f27df4864503fdc691ab544b8f20e045cdaddf1145025f',NULL,NULL,'0:0:0:0:0:0:0:1','2026-08-11 17:10:20.263972','2026-07-15 15:07:36.285276','2026-07-12 17:10:20.292362'),(34,9,'6e177e30cbcaae4c4ab699b9f42419ba906794ec2041ee5c2672c2d03063a2a9',NULL,NULL,'0:0:0:0:0:0:0:1','2026-08-11 17:13:06.105607',NULL,'2026-07-12 17:13:06.108834'),(35,9,'1c38fc12d853b2575da1abdc45830c09586becfa79bbb34a47afc313b6df457d',NULL,NULL,'0:0:0:0:0:0:0:1','2026-08-11 23:49:12.165078',NULL,'2026-07-12 23:49:12.236740'),(36,9,'53ffaa66c2eb154b20043c4bb9d349b95f29ab51694b0b9850226a849032c054',NULL,NULL,'0:0:0:0:0:0:0:1','2026-08-12 00:41:49.605694',NULL,'2026-07-13 00:41:49.630674'),(37,9,'ce173ee739430e7983ee8de1b46f51672a85be8133d4a5332acf468805182aa3',NULL,NULL,'0:0:0:0:0:0:0:1','2026-08-12 01:15:55.510780',NULL,'2026-07-13 01:15:55.540673'),(38,15,'8a05b71f0e558618bf1d366f3fedeb2b1bfcda6382900b728a446bedf813b95c',NULL,NULL,'0:0:0:0:0:0:0:1','2026-08-13 12:41:46.953706',NULL,'2026-07-14 12:41:46.959648'),(39,15,'10ade28e6805daffb7be9fd968c5f5eac4edf833bd5ece95a980b766e9fbd2a6',NULL,NULL,'0:0:0:0:0:0:0:1','2026-08-13 22:58:04.233170',NULL,'2026-07-14 22:58:04.256426'),(64,15,'10bc1d69de43547db6d08db49f3f8a26a137847bd5e25fb1846e0528dcc9b24a',NULL,NULL,'0:0:0:0:0:0:0:1','2026-08-13 23:34:16.416512',NULL,'2026-07-14 23:34:16.445121'),(77,15,'c7c1f1c3ecbcd6112b7fafa34e570a9f9fa00e82cd3d0421cca997eb241aa967',NULL,NULL,'0:0:0:0:0:0:0:1','2026-08-14 12:39:59.153279',NULL,'2026-07-15 12:39:59.189296'),(90,15,'facc36275d72fdb7419b3628e8857fa96e11540e2dee6fc27fdef71ceaadda4e',NULL,NULL,'0:0:0:0:0:0:0:1','2026-08-14 14:55:32.739867',NULL,'2026-07-15 14:55:32.773431'),(91,8,'3ba3dc9c9e583d4bf357a2820d44b17aa634e5a570cdec17df277d8d5d182e44',NULL,NULL,'0:0:0:0:0:0:0:1','2026-08-14 14:59:11.129325','2026-07-15 15:07:36.285276','2026-07-15 14:59:11.130463'),(92,9,'b1a46a54eb3a7b325ad61aa56851f3df6c365184c2625fc9c325f5f419f1ea7b',NULL,NULL,'0:0:0:0:0:0:0:1','2026-08-14 15:08:11.959357',NULL,'2026-07-15 15:08:11.961571'),(105,15,'008d34081f936f0679e6f447889b47ba14781c79524e89fbf5bccfe670572c9f',NULL,NULL,'0:0:0:0:0:0:0:1','2026-08-14 22:52:56.907537',NULL,'2026-07-15 22:52:56.946909'),(118,15,'b78b5abfcb53c2606e892ebc432d8caaaf35906c39619803d881494bdce7db11',NULL,NULL,'0:0:0:0:0:0:0:1','2026-08-15 00:29:40.282081',NULL,'2026-07-16 00:29:40.335048'),(119,15,'86a7eeaf512ec0c969c0bfeaa001ab46b2d03905ab11fbb9a27de9c9113ace98',NULL,NULL,'0:0:0:0:0:0:0:1','2026-08-15 15:37:03.145193',NULL,'2026-07-16 15:37:03.183713'),(120,11,'db8226448cf042f59904b7b46ae28f80e1cc1528a4a2ab9f7782414721c4ec72',NULL,NULL,'0:0:0:0:0:0:0:1','2026-08-16 15:24:58.900759',NULL,'2026-07-17 15:24:58.933601'),(121,7,'66aadc768994f1872b6e7b9e81b534ffb1b6beec0aa84f42a8e4838554d9c33a',NULL,NULL,'0:0:0:0:0:0:0:1','2026-08-16 15:30:04.721019',NULL,'2026-07-17 15:30:04.727937'),(122,7,'0e9a82956ab52e11fc7d22fd48e4aed66524e3732dc17b396f847c6b221fbed7',NULL,NULL,'0:0:0:0:0:0:0:1','2026-08-16 19:07:59.106101',NULL,'2026-07-17 19:07:59.169178'),(123,7,'fd497ba76f805cededb0a2308111ef8612f0fb66d963d8d39a0e28d017c5e0f7',NULL,NULL,'0:0:0:0:0:0:0:1','2026-08-17 05:43:33.688893',NULL,'2026-07-18 05:43:33.708444'),(124,11,'43f1b3418f203ccb9e1cfb220dac372482fb50cbb5e06a9a342ff69e8ebacfbb',NULL,NULL,'0:0:0:0:0:0:0:1','2026-08-17 06:57:35.737976',NULL,'2026-07-18 06:57:35.770410'),(125,9,'51e302fa383e5da6672efdc81f14474042475da6275b89f4d2420b04863fb58c',NULL,NULL,'0:0:0:0:0:0:0:1','2026-08-17 07:02:20.664692',NULL,'2026-07-18 07:02:20.666170'),(126,7,'4138d30d6af72803edec53961826fa88edbf78e8e6fbcb9d49ba1398dfb06268',NULL,NULL,'0:0:0:0:0:0:0:1','2026-08-17 07:03:21.627561',NULL,'2026-07-18 07:03:21.629240'),(127,7,'cb416226e091cc0041222335b3590828c41ecbb922db65734431e39d7e41a0f6',NULL,NULL,'0:0:0:0:0:0:0:1','2026-08-17 20:01:31.385860',NULL,'2026-07-18 20:01:31.408990'),(128,6,'955b75fbd600325ab2fdef7ea45b04dd8997c50f12d1e766cc6e175ec8efc8ac',NULL,NULL,'0:0:0:0:0:0:0:1','2026-08-17 20:02:22.326622',NULL,'2026-07-18 20:02:22.328988'),(129,7,'864e6d46c728b65795f9bcb032034c9f539b03f6e66d6c1343f712aafd4bd0a5',NULL,NULL,'0:0:0:0:0:0:0:1','2026-08-17 20:02:42.130372',NULL,'2026-07-18 20:02:42.132871'),(130,6,'2fdcfe3f92b2e6d8c6b6beb90c8dbcb223584b8551863c2a30d281ab9d55114a',NULL,NULL,'0:0:0:0:0:0:0:1','2026-08-17 20:03:17.275755',NULL,'2026-07-18 20:03:17.280123'),(131,11,'6ac62f159557ab5a69c3fff5d82c407f3e21d6aa2ad154bb8ce0383b12460d0e',NULL,NULL,'0:0:0:0:0:0:0:1','2026-08-17 20:05:48.214444',NULL,'2026-07-18 20:05:48.217173'),(132,9,'81140b35bc4c4df3d2ac4ec9d044177d93e75a1d3d747ad1dac406b087f43e75',NULL,NULL,'0:0:0:0:0:0:0:1','2026-08-17 20:06:25.468966',NULL,'2026-07-18 20:06:25.470475'),(133,12,'98ee1ac96cf167c93ede6f71faeadbaebf3ca35a91cc507554cba30746826332',NULL,NULL,'0:0:0:0:0:0:0:1','2026-08-17 20:07:25.005839',NULL,'2026-07-18 20:07:25.008112'),(134,7,'1ff878b938c99d17e6bea2a8ebee8f62b659bc083e64405d6b0b4f40540515b4',NULL,NULL,'0:0:0:0:0:0:0:1','2026-08-17 20:21:30.257600',NULL,'2026-07-18 20:21:30.262446'),(135,15,'ce6d7742391368214590976a51a6796576a90bd897db99f176bea42cf53cdf72',NULL,NULL,'0:0:0:0:0:0:0:1','2026-08-17 21:15:04.957757',NULL,'2026-07-18 21:15:04.985276');
/*!40000 ALTER TABLE `refresh_tokens` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `reports`
--

DROP TABLE IF EXISTS `reports`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reports` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `reporter_id` bigint unsigned NOT NULL,
  `post_id` bigint unsigned NOT NULL,
  `reason` enum('SPAM','HARASSMENT','HARMFUL_CONTENT','VIOLENCE','MISINFORMATION','INAPPROPRIATE','OTHER') NOT NULL,
  `description` varchar(1000) DEFAULT NULL,
  `status` enum('PENDING','RESOLVED','REJECTED') NOT NULL DEFAULT 'PENDING',
  `resolved_by` bigint unsigned DEFAULT NULL,
  `resolved_at` datetime(6) DEFAULT NULL,
  `resolution_note` varchar(1000) DEFAULT NULL,
  `post_content_snapshot` varchar(500) DEFAULT NULL,
  `post_media_snapshot` json DEFAULT NULL,
  `pending_report_key` varchar(100) GENERATED ALWAYS AS ((case when (`status` = _utf8mb4'PENDING') then concat(`reporter_id`,_utf8mb3':',`post_id`) else NULL end)) STORED,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_reports_pending_key` (`pending_report_key`),
  KEY `fk_reports_resolved_by` (`resolved_by`),
  KEY `idx_reports_status_created` (`status`,`created_at`,`id`),
  KEY `idx_reports_post_status` (`post_id`,`status`,`created_at` DESC,`id` DESC),
  KEY `idx_reports_reporter_created` (`reporter_id`,`created_at` DESC,`id` DESC),
  CONSTRAINT `fk_reports_post` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_reports_reporter` FOREIGN KEY (`reporter_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_reports_resolved_by` FOREIGN KEY (`resolved_by`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `chk_reports_resolution_state` CHECK ((((`status` = _utf8mb4'PENDING') and (`resolved_by` is null) and (`resolved_at` is null)) or ((`status` in (_utf8mb4'RESOLVED',_utf8mb4'REJECTED')) and (`resolved_by` is not null) and (`resolved_at` is not null))))
) ENGINE=InnoDB AUTO_INCREMENT=77 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reports`
--

LOCK TABLES `reports` WRITE;
/*!40000 ALTER TABLE `reports` DISABLE KEYS */;
INSERT INTO `reports` (`id`, `reporter_id`, `post_id`, `reason`, `description`, `status`, `resolved_by`, `resolved_at`, `resolution_note`, `post_content_snapshot`, `post_media_snapshot`, `created_at`, `updated_at`) VALUES (1,8,2,'SPAM',NULL,'REJECTED',15,'2026-07-16 00:30:23.346429','Không phát hiện nội dung vi phạm','Hôm nay học Spring Boot rất ổn','[]','2026-07-12 14:35:40.115670','2026-07-16 00:30:23.364057'),(2,8,1,'SPAM',NULL,'RESOLVED',15,'2026-07-16 00:41:08.622601','Báo cáo hợp lệ, bài viết chứa nội dung spam','Hôm nay học Spring Boot rất ổn','[]','2026-07-12 14:36:29.622761','2026-07-16 00:41:08.640293'),(3,8,3,'SPAM','Nội dung quảng cáo lặp lại.','PENDING',NULL,NULL,NULL,'Hôm nay học Spring Boot Khong ổn','[\"https://res.cloudinary.com/xsypft9k/image/upload/v1783020003/student-social-network/posts/kx1ljvovafmddcph9uq6.jpg\"]','2026-07-12 14:36:46.899707','2026-07-12 14:36:46.899707');
/*!40000 ALTER TABLE `reports` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `saved_posts`
--

DROP TABLE IF EXISTS `saved_posts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `saved_posts` (
  `user_id` bigint unsigned NOT NULL,
  `post_id` bigint unsigned NOT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`user_id`,`post_id`),
  KEY `idx_saved_posts_user_created` (`user_id`,`created_at` DESC,`post_id` DESC),
  KEY `idx_saved_posts_post_created` (`post_id`,`created_at` DESC,`user_id` DESC),
  CONSTRAINT `fk_saved_posts_post` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_saved_posts_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `saved_posts`
--

LOCK TABLES `saved_posts` WRITE;
/*!40000 ALTER TABLE `saved_posts` DISABLE KEYS */;
INSERT INTO `saved_posts` VALUES (9,5,'2026-07-12 23:52:27.579542'),(9,2,'2026-07-12 23:50:13.565671');
/*!40000 ALTER TABLE `saved_posts` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_profiles`
--

DROP TABLE IF EXISTS `user_profiles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_profiles` (
  `user_id` bigint unsigned NOT NULL,
  `display_name` varchar(100) DEFAULT NULL,
  `avatar_url` varchar(1000) DEFAULT NULL,
  `avatar_public_id` varchar(255) DEFAULT NULL,
  `bio` varchar(500) DEFAULT NULL,
  `date_of_birth` date DEFAULT NULL,
  `profile_completed_at` datetime(6) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`user_id`),
  FULLTEXT KEY `ftx_user_profiles_display_name` (`display_name`),
  CONSTRAINT `fk_user_profiles_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `chk_user_profiles_completion_consistency` CHECK (((`profile_completed_at` is null) or (`display_name` is not null))),
  CONSTRAINT `chk_user_profiles_display_name_not_blank` CHECK (((`display_name` is null) or (char_length(trim(`display_name`)) > 0)))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_profiles`
--

LOCK TABLES `user_profiles` WRITE;
/*!40000 ALTER TABLE `user_profiles` DISABLE KEYS */;
INSERT INTO `user_profiles` VALUES (3,NULL,NULL,NULL,NULL,NULL,NULL,'2026-07-01 09:25:53.535802','2026-07-01 09:25:53.535802'),(4,NULL,NULL,NULL,NULL,NULL,NULL,'2026-07-01 09:29:53.909260','2026-07-01 09:29:53.909260'),(5,NULL,NULL,NULL,NULL,NULL,NULL,'2026-07-01 18:59:50.895020','2026-07-01 18:59:50.895020'),(6,NULL,NULL,NULL,NULL,NULL,NULL,'2026-07-01 19:03:13.789179','2026-07-01 19:03:13.789179'),(7,'Nguyen Van A',NULL,NULL,'Sinh viên yêu thích công nghệ','2002-01-01','2026-07-02 10:49:59.265113','2026-07-02 03:46:51.475912','2026-07-02 03:50:52.110595'),(8,'Nguyen Van B',NULL,NULL,'Sinh viên yêu thích AI','2003-01-01','2026-07-02 11:17:48.622125','2026-07-02 04:16:51.186994','2026-07-02 04:17:48.637947'),(9,'Nguyen Van c','https://res.cloudinary.com/xsypft9k/image/upload/v1782967057/student-social-network/avatars/qsoz7oowf74xtonxrvuu.jpg','student-social-network/avatars/qsoz7oowf74xtonxrvuu','Sinh viên yêu thích ban hang','2006-01-01','2026-07-02 11:36:12.994682','2026-07-02 04:34:55.834032','2026-07-02 04:37:31.057784'),(10,'Nguyen Van aaaaaaaaa',NULL,NULL,'Sinh viên yêu thích công nghệ','2000-01-01','2026-07-02 22:22:41.086256','2026-07-02 15:21:04.073223','2026-07-02 15:22:41.092589'),(11,NULL,NULL,NULL,NULL,NULL,NULL,'2026-07-02 15:29:44.241085','2026-07-02 15:29:44.241085'),(12,'Nguyen Van A','https://res.cloudinary.com/xsypft9k/image/upload/v1783069258/student-social-network/avatars/o5gb6rsk4mowbayrh61e.jpg','student-social-network/avatars/o5gb6rsk4mowbayrh61e','Sinh vien nam 3','2000-01-01','2026-07-03 16:00:50.341897','2026-07-03 08:54:30.200715','2026-07-03 09:00:50.345974'),(15,'Quản trị viên',NULL,NULL,NULL,NULL,'2026-07-14 12:38:32.195689','2026-07-14 12:38:32.202546','2026-07-14 12:38:32.202546');
/*!40000 ALTER TABLE `user_profiles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `email` varchar(255) DEFAULT NULL,
  `phone_number` varchar(20) DEFAULT NULL,
  `email_verified_at` datetime(6) DEFAULT NULL,
  `phone_verified_at` datetime(6) DEFAULT NULL,
  `password_hash` varchar(255) NOT NULL,
  `role` enum('USER','ADMIN') NOT NULL DEFAULT 'USER',
  `status` enum('ACTIVE','BLOCKED') NOT NULL DEFAULT 'ACTIVE',
  `blocked_at` datetime(6) DEFAULT NULL,
  `blocked_reason` varchar(500) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_users_email` (`email`),
  UNIQUE KEY `uq_users_phone_number` (`phone_number`),
  KEY `idx_users_status_created_at` (`status`,`created_at` DESC,`id` DESC),
  CONSTRAINT `chk_users_blocked_data` CHECK ((((`status` = _utf8mb4'ACTIVE') and (`blocked_at` is null)) or ((`status` = _utf8mb4'BLOCKED') and (`blocked_at` is not null)))),
  CONSTRAINT `chk_users_contact_required` CHECK (((`email` is not null) or (`phone_number` is not null))),
  CONSTRAINT `chk_users_email_not_blank` CHECK (((`email` is null) or (char_length(trim(`email`)) > 0))),
  CONSTRAINT `chk_users_email_verification_consistency` CHECK (((`email` is not null) or (`email_verified_at` is null))),
  CONSTRAINT `chk_users_phone_not_blank` CHECK (((`phone_number` is null) or (char_length(trim(`phone_number`)) > 0))),
  CONSTRAINT `chk_users_phone_verification_consistency` CHECK (((`phone_number` is not null) or (`phone_verified_at` is null)))
) ENGINE=InnoDB AUTO_INCREMENT=352 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (3,'student01@gmail.com',NULL,NULL,NULL,'$2a$10$ewm7P2WqTPdYGVZGM1LMBO5FaCHkFQnVYxJHpAQZWjPFLPqC20hHy','USER','ACTIVE',NULL,NULL,'2026-07-01 09:25:53.485548','2026-07-01 09:25:53.485548'),(4,NULL,'0912345678',NULL,NULL,'$2a$10$8sUAeP5sDcqI0WYNuNZ9Ou45OZ5.0j2b674ApGZlfM3Wk2/B.vCU2','USER','ACTIVE',NULL,NULL,'2026-07-01 09:29:53.903846','2026-07-01 09:29:53.903846'),(5,NULL,'0123456789',NULL,NULL,'$2a$10$A8MCwlN6GiDgXor7zPKxk.lLtbM6YTrJxSify1Gwe3aUk4AOJJndm','USER','ACTIVE',NULL,NULL,'2026-07-01 18:59:50.854555','2026-07-01 18:59:50.854555'),(6,'student01@example.com',NULL,NULL,NULL,'$2a$10$9PRi6lx8StB.HBtOpGaERunfY4EM04.sZf9p/qCPH0V02Ft0cBBJW','USER','ACTIVE',NULL,NULL,'2026-07-01 19:03:13.782408','2026-07-01 19:03:13.782408'),(7,'student02@example.com',NULL,NULL,NULL,'$2a$10$ZkhP2zahbEMmA4T3P917Z.e/2H9IRfcLfPmo0FlgjSgtTux8F60lm','USER','ACTIVE',NULL,NULL,'2026-07-02 03:46:51.441408','2026-07-02 03:46:51.441408'),(8,'student03@example.com',NULL,NULL,NULL,'$2a$10$htggGiX2K5MTrxOM7rdEVuca7DtWfWKhykH/ekxx.IeXwA1sGkEam','USER','BLOCKED','2026-07-15 15:07:36.285276','SPAM','2026-07-02 04:16:51.156647','2026-07-15 15:07:36.289433'),(9,'student04@example.com',NULL,NULL,NULL,'$2a$10$TsYowuoGdPVpuAEc1UQuJeXi3LpZJqhS3tPdKiQmAUsccsGuIV1P.','USER','ACTIVE',NULL,NULL,'2026-07-02 04:34:55.829717','2026-07-02 04:34:55.829717'),(10,'student05@example.com',NULL,NULL,NULL,'$2a$10$QbWJ/wEJdBgdNUZ3prwK0eDTmHiJ.aGo9KSSAFyE43I2k/Y4tdwZq','USER','BLOCKED','2026-07-14 23:38:38.743669','SPAM','2026-07-02 15:21:04.029231','2026-07-14 23:38:38.746321'),(11,'student06@example.com',NULL,NULL,NULL,'$2a$10$clZY/JRSid1VpLeE7Df1xeA2N7r27OBc1/siFJ0eCeGwlkTU7L6PK','USER','ACTIVE',NULL,NULL,'2026-07-02 15:29:44.236709','2026-07-02 15:29:44.236709'),(12,'student07@example.com',NULL,NULL,NULL,'$2a$10$VPCWxGHI2de1lYRTplWwyO59oCGWbh6XDZOWeX65huNigu1z604O6','USER','ACTIVE',NULL,NULL,'2026-07-03 08:54:30.169621','2026-07-03 08:54:30.169621'),(15,'xoay0120khanh@gmail.com',NULL,NULL,NULL,'$2a$10$hSY9cvHQbX7QpQcCDYr3qubi/vQxLITEIN5ky0WPS4.z9oGvRgbz6','ADMIN','ACTIVE',NULL,NULL,'2026-07-14 12:38:32.178212','2026-07-14 12:38:32.178212');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Temporary view structure for view `v_active_posts`
--

DROP TABLE IF EXISTS `v_active_posts`;
/*!50001 DROP VIEW IF EXISTS `v_active_posts`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `v_active_posts` AS SELECT 
 1 AS `post_id`,
 1 AS `content`,
 1 AS `published_at`,
 1 AS `like_count`,
 1 AS `comment_count`,
 1 AS `is_edited`,
 1 AS `author_id`,
 1 AS `display_name`,
 1 AS `avatar_url`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `v_feed_posts`
--

DROP TABLE IF EXISTS `v_feed_posts`;
/*!50001 DROP VIEW IF EXISTS `v_feed_posts`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `v_feed_posts` AS SELECT 
 1 AS `post_id`,
 1 AS `author_id`,
 1 AS `content`,
 1 AS `is_edited`,
 1 AS `like_count`,
 1 AS `comment_count`,
 1 AS `published_at`,
 1 AS `display_name`,
 1 AS `avatar_url`*/;
SET character_set_client = @saved_cs_client;

--
-- Final view structure for view `v_active_posts`
--

/*!50001 DROP VIEW IF EXISTS `v_active_posts`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb3 */;
/*!50001 SET character_set_results     = utf8mb3 */;
/*!50001 SET collation_connection      = utf8mb3_general_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`%` SQL SECURITY DEFINER */
/*!50001 VIEW `v_active_posts` AS select `p`.`id` AS `post_id`,`p`.`content` AS `content`,`p`.`published_at` AS `published_at`,`p`.`like_count` AS `like_count`,`p`.`comment_count` AS `comment_count`,`p`.`is_edited` AS `is_edited`,`u`.`id` AS `author_id`,`up`.`display_name` AS `display_name`,`up`.`avatar_url` AS `avatar_url` from ((`posts` `p` join `users` `u` on((`u`.`id` = `p`.`author_id`))) join `user_profiles` `up` on((`up`.`user_id` = `u`.`id`))) where ((`p`.`status` = 'PUBLISHED') and (`u`.`status` = 'ACTIVE') and (`up`.`profile_completed_at` is not null)) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `v_feed_posts`
--

/*!50001 DROP VIEW IF EXISTS `v_feed_posts`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb3 */;
/*!50001 SET character_set_results     = utf8mb3 */;
/*!50001 SET collation_connection      = utf8mb3_general_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`%` SQL SECURITY DEFINER */
/*!50001 VIEW `v_feed_posts` AS select `p`.`id` AS `post_id`,`p`.`author_id` AS `author_id`,`p`.`content` AS `content`,`p`.`is_edited` AS `is_edited`,`p`.`like_count` AS `like_count`,`p`.`comment_count` AS `comment_count`,`p`.`published_at` AS `published_at`,`up`.`display_name` AS `display_name`,`up`.`avatar_url` AS `avatar_url` from ((`posts` `p` join `users` `u` on((`u`.`id` = `p`.`author_id`))) join `user_profiles` `up` on((`up`.`user_id` = `p`.`author_id`))) where ((`p`.`status` = 'PUBLISHED') and (`u`.`status` = 'ACTIVE') and (`up`.`profile_completed_at` is not null)) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-07-22 13:51:08

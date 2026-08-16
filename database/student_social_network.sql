-- Student Social Network - MySQL 8 full rebuild and demo seed
-- Current model: email OTP, Google and Facebook authentication only.
-- WARNING: drops and recreates the complete test database.

DROP DATABASE IF EXISTS `student_social_network`;
CREATE DATABASE `student_social_network` CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE `student_social_network`;

-- MySQL dump 10.13  Distrib 8.0.45, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: student_social_network
-- ------------------------------------------------------
-- Server version	8.0.36
-- Auth extension: OTP registration, Google/Facebook providers, multi-login account linking.

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
  `old_status` varchar(16) NOT NULL,
  `new_status` varchar(16) NOT NULL,
  `changed_by` bigint unsigned NOT NULL,
  `reason` varchar(500) NOT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  KEY `fk_account_status_histories_changed_by` (`changed_by`),
  KEY `idx_account_status_histories_user_created` (`user_id`,`created_at` DESC,`id` DESC),
  CONSTRAINT `fk_account_status_histories_changed_by` FOREIGN KEY (`changed_by`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_account_status_histories_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `chk_account_status_histories_old_status` CHECK ((`old_status` in (_utf8mb4'ACTIVE',_utf8mb4'BLOCKED'))),
  CONSTRAINT `chk_account_status_histories_new_status` CHECK ((`new_status` in (_utf8mb4'ACTIVE',_utf8mb4'BLOCKED'))),
  CONSTRAINT `chk_account_status_histories_changed` CHECK ((`old_status` <> `new_status`))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `account_status_histories`
--

LOCK TABLES `account_status_histories` WRITE;
/*!40000 ALTER TABLE `account_status_histories` DISABLE KEYS */;
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
  `action_type` enum('BLOCK_USER','UNBLOCK_USER','UPDATE_USER_PROFILE','CREATE_HASHTAG','UPDATE_HASHTAG','DELETE_HASHTAG','HIDE_POST','RESTORE_POST','RESOLVE_REPORT','REJECT_REPORT','RESOLVE_MODERATION_CASE','REJECT_MODERATION_CASE','RESOLVE_PROFILE_REPORT','REJECT_PROFILE_REPORT','CREATE_ACADEMIC_DATA','UPDATE_ACADEMIC_DATA','CHANGE_ACADEMIC_STATUS') NOT NULL,
  `target_type` enum('USER','POST','HASHTAG','REPORT','MODERATION_CASE','PROFILE_REPORT','ACADEMIC_DATA') NOT NULL,
  `target_id` bigint unsigned NOT NULL,
  `note` varchar(1000) DEFAULT NULL,
  `old_data` json DEFAULT NULL,
  `new_data` json DEFAULT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  KEY `idx_admin_actions_target_created` (`target_type`,`target_id`,`created_at` DESC,`id` DESC),
  KEY `idx_admin_actions_admin_created` (`admin_id`,`created_at` DESC,`id` DESC),
  CONSTRAINT `fk_admin_actions_admin` FOREIGN KEY (`admin_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `admin_actions`
--

LOCK TABLES `admin_actions` WRITE;
/*!40000 ALTER TABLE `admin_actions` DISABLE KEYS */;
/*!40000 ALTER TABLE `admin_actions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `profile_report_cases`
--

DROP TABLE IF EXISTS `profile_report_cases`;
CREATE TABLE `profile_report_cases` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `reported_user_id` bigint unsigned NOT NULL,
  `status` enum('PENDING','RESOLVED','REJECTED') NOT NULL DEFAULT 'PENDING',
  `resolved_by` bigint unsigned DEFAULT NULL,
  `resolved_at` datetime(6) DEFAULT NULL,
  `resolution_note` varchar(1000) DEFAULT NULL,
  `reported_display_name_snapshot` varchar(100) NOT NULL,
  `reported_avatar_url_snapshot` varchar(1000) DEFAULT NULL,
  `reported_bio_snapshot` varchar(500) DEFAULT NULL,
  `reported_date_of_birth_snapshot` date DEFAULT NULL,
  `report_count` int unsigned NOT NULL DEFAULT '0',
  `latest_reported_at` datetime(6) NOT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_profile_report_cases_target` (`reported_user_id`),
  KEY `idx_profile_report_cases_status_latest` (`status`,`latest_reported_at`,`id`),
  KEY `idx_profile_report_cases_resolved_by` (`resolved_by`),
  CONSTRAINT `fk_profile_report_cases_target` FOREIGN KEY (`reported_user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_profile_report_cases_resolved_by` FOREIGN KEY (`resolved_by`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `chk_profile_report_cases_resolution_state` CHECK (((`status` = _utf8mb4'PENDING' and `resolved_by` is null and `resolved_at` is null) or (`status` in (_utf8mb4'RESOLVED',_utf8mb4'REJECTED') and `resolved_by` is not null and `resolved_at` is not null)))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Table structure for table `profile_reports`
--

DROP TABLE IF EXISTS `profile_reports`;
CREATE TABLE `profile_reports` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `case_id` bigint unsigned NOT NULL,
  `reporter_id` bigint unsigned NOT NULL,
  `reported_user_id` bigint unsigned NOT NULL,
  `reason` enum('PROHIBITED_CONTENT','IMPERSONATION','UNDER_MINIMUM_AGE','SCAM_OR_FRAUD','FALSE_INFORMATION','VIOLENCE_OR_DANGEROUS_ORGANIZATION') NOT NULL,
  `status` enum('PENDING','RESOLVED','REJECTED') NOT NULL DEFAULT 'PENDING',
  `resolved_by` bigint unsigned DEFAULT NULL,
  `resolved_at` datetime(6) DEFAULT NULL,
  `resolution_note` varchar(1000) DEFAULT NULL,
  `reporter_display_name_snapshot` varchar(100) NOT NULL,
  `reported_display_name_snapshot` varchar(100) NOT NULL,
  `reported_avatar_url_snapshot` varchar(1000) DEFAULT NULL,
  `reported_bio_snapshot` varchar(500) DEFAULT NULL,
  `reported_date_of_birth_snapshot` date DEFAULT NULL,
  `pending_report_key` varchar(100) GENERATED ALWAYS AS ((case when (`status` = _utf8mb4'PENDING') then concat(`reporter_id`,_utf8mb3':',`reported_user_id`) else NULL end)) STORED,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_profile_reports_pending_key` (`pending_report_key`),
  KEY `idx_profile_reports_status_created` (`status`,`created_at`,`id`),
  KEY `idx_profile_reports_target_created` (`reported_user_id`,`created_at` DESC,`id` DESC),
  KEY `idx_profile_reports_reporter_created` (`reporter_id`,`created_at` DESC,`id` DESC),
  KEY `idx_profile_reports_resolved_by` (`resolved_by`),
  KEY `idx_profile_reports_case_created` (`case_id`,`created_at`,`id`),
  CONSTRAINT `fk_profile_reports_case` FOREIGN KEY (`case_id`) REFERENCES `profile_report_cases` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_profile_reports_reporter` FOREIGN KEY (`reporter_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_profile_reports_target` FOREIGN KEY (`reported_user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_profile_reports_resolved_by` FOREIGN KEY (`resolved_by`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `chk_profile_reports_not_self` CHECK ((`reporter_id` <> `reported_user_id`)),
  CONSTRAINT `chk_profile_reports_resolution_state` CHECK (((`status` = _utf8mb4'PENDING' and `resolved_by` is null and `resolved_at` is null) or (`status` in (_utf8mb4'RESOLVED',_utf8mb4'REJECTED') and `resolved_by` is not null and `resolved_at` is not null)))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `comments`
--

LOCK TABLES `comments` WRITE;
/*!40000 ALTER TABLE `comments` DISABLE KEYS */;
/*!40000 ALTER TABLE `comments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_blocks`
--

DROP TABLE IF EXISTS `user_blocks`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_blocks` (
  `blocker_id` bigint unsigned NOT NULL,
  `blocked_id` bigint unsigned NOT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`blocker_id`,`blocked_id`),
  KEY `idx_user_blocks_blocked_blocker` (`blocked_id`,`blocker_id`),
  CONSTRAINT `fk_user_blocks_blocker` FOREIGN KEY (`blocker_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_user_blocks_blocked` FOREIGN KEY (`blocked_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `chk_user_blocks_not_self` CHECK ((`blocker_id` <> `blocked_id`))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_restrictions`
--

DROP TABLE IF EXISTS `user_restrictions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_restrictions` (
  `restrictor_id` bigint unsigned NOT NULL,
  `restricted_id` bigint unsigned NOT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`restrictor_id`,`restricted_id`),
  KEY `idx_user_restrictions_restricted_restrictor` (`restricted_id`,`restrictor_id`),
  CONSTRAINT `fk_user_restrictions_restrictor` FOREIGN KEY (`restrictor_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_user_restrictions_restricted` FOREIGN KEY (`restricted_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `chk_user_restrictions_not_self` CHECK ((`restrictor_id` <> `restricted_id`))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `hashtags`
--

LOCK TABLES `hashtags` WRITE;
/*!40000 ALTER TABLE `hashtags` DISABLE KEYS */;
/*!40000 ALTER TABLE `hashtags` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pending_registrations`
--

DROP TABLE IF EXISTS `pending_registrations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pending_registrations` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `registration_type` varchar(16) NOT NULL,
  `identifier_normalized` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  `active_identifier_key` varchar(272) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  `password_hash` varchar(255) DEFAULT NULL,
  `flow_token_hash` char(64) CHARACTER SET ascii COLLATE ascii_bin DEFAULT NULL,
  `otp_hash` char(64) CHARACTER SET ascii COLLATE ascii_bin DEFAULT NULL,
  `otp_version` int unsigned NOT NULL DEFAULT '1',
  `otp_expires_at` datetime(6) NOT NULL,
  `failed_attempts` tinyint unsigned NOT NULL DEFAULT '0',
  `resend_available_at` datetime(6) NOT NULL,
  `resend_count` smallint unsigned NOT NULL DEFAULT '0',
  `delivery_status` varchar(16) NOT NULL DEFAULT 'PENDING',
  `delivery_attempt_count` smallint unsigned NOT NULL DEFAULT '0',
  `last_delivery_attempt_at` datetime(6) DEFAULT NULL,
  `last_delivery_succeeded_at` datetime(6) DEFAULT NULL,
  `delivery_failure_code` varchar(64) DEFAULT NULL,
  `status` varchar(16) NOT NULL DEFAULT 'PENDING',
  `expires_at` datetime(6) NOT NULL,
  `completed_user_id` bigint unsigned DEFAULT NULL,
  `terminal_at` datetime(6) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_pending_flow_token_hash` (`flow_token_hash`),
  UNIQUE KEY `uq_pending_active_identifier` (`active_identifier_key`),
  UNIQUE KEY `uq_pending_completed_user` (`completed_user_id`),
  KEY `idx_pending_identifier_state` (`identifier_normalized`,`status`,`expires_at`,`id`),
  KEY `idx_pending_expiry` (`status`,`expires_at`,`id`),
  KEY `idx_pending_cleanup` (`status`,`terminal_at`,`id`),
  CONSTRAINT `fk_pending_completed_user` FOREIGN KEY (`completed_user_id`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `chk_pending_registration_type` CHECK ((`registration_type` = _utf8mb4'EMAIL')),
  CONSTRAINT `chk_pending_status` CHECK ((`status` in (_utf8mb4'PENDING',_utf8mb4'COMPLETED',_utf8mb4'CANCELLED',_utf8mb4'EXPIRED'))),
  CONSTRAINT `chk_pending_delivery_status` CHECK ((`delivery_status` in (_utf8mb4'PENDING',_utf8mb4'SENT',_utf8mb4'FAILED',_utf8mb4'UNKNOWN'))),
  CONSTRAINT `chk_pending_attempts` CHECK ((`failed_attempts` <= 5)),
  CONSTRAINT `chk_pending_times` CHECK (((`expires_at` > `created_at`) and (`otp_expires_at` > `created_at`) and (`otp_expires_at` <= `expires_at`) and (`resend_available_at` <= `expires_at`))),
  CONSTRAINT `chk_pending_delivery_failure` CHECK (((`status` <> _utf8mb4'PENDING') or (`delivery_status` <> _utf8mb4'FAILED') or (`delivery_failure_code` is not null))),
  CONSTRAINT `chk_pending_lifecycle` CHECK (((`status` = _utf8mb4'PENDING' and `identifier_normalized` is not null and `active_identifier_key` = concat(`registration_type`,_utf8mb4':',`identifier_normalized`) and `password_hash` is not null and `flow_token_hash` is not null and `otp_hash` is not null and `terminal_at` is null) or (`status` = _utf8mb4'COMPLETED' and `identifier_normalized` is not null and `active_identifier_key` is null and `password_hash` is null and `otp_hash` is null and `delivery_failure_code` is null and `terminal_at` is not null) or (`status` in (_utf8mb4'CANCELLED',_utf8mb4'EXPIRED') and `identifier_normalized` is null and `active_identifier_key` is null and `password_hash` is null and `otp_hash` is null and `delivery_failure_code` is null and `terminal_at` is not null)))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pending_registrations`
--

LOCK TABLES `pending_registrations` WRITE;
/*!40000 ALTER TABLE `pending_registrations` DISABLE KEYS */;
/*!40000 ALTER TABLE `pending_registrations` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `auth_method_link_challenges`
--

DROP TABLE IF EXISTS `auth_method_link_challenges`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `auth_method_link_challenges` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint unsigned NOT NULL,
  `purpose` varchar(16) NOT NULL,
  `identifier_normalized` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  `active_identifier_key` varchar(272) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  `active_user_purpose_key` varchar(64) CHARACTER SET ascii COLLATE ascii_bin DEFAULT NULL,
  `flow_token_hash` char(64) CHARACTER SET ascii COLLATE ascii_bin DEFAULT NULL,
  `otp_hash` char(64) CHARACTER SET ascii COLLATE ascii_bin DEFAULT NULL,
  `otp_verified_at` datetime(6) DEFAULT NULL,
  `otp_version` int unsigned NOT NULL DEFAULT '1',
  `otp_expires_at` datetime(6) NOT NULL,
  `failed_attempts` tinyint unsigned NOT NULL DEFAULT '0',
  `resend_available_at` datetime(6) NOT NULL,
  `resend_count` smallint unsigned NOT NULL DEFAULT '0',
  `delivery_status` varchar(16) NOT NULL DEFAULT 'PENDING',
  `delivery_attempt_count` smallint unsigned NOT NULL DEFAULT '0',
  `last_delivery_attempt_at` datetime(6) DEFAULT NULL,
  `last_delivery_succeeded_at` datetime(6) DEFAULT NULL,
  `delivery_failure_code` varchar(64) DEFAULT NULL,
  `status` varchar(16) NOT NULL DEFAULT 'PENDING',
  `expires_at` datetime(6) NOT NULL,
  `terminal_at` datetime(6) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_link_flow_token_hash` (`flow_token_hash`),
  UNIQUE KEY `uq_link_active_identifier` (`active_identifier_key`),
  UNIQUE KEY `uq_link_active_user_purpose` (`active_user_purpose_key`),
  KEY `idx_link_user_state` (`user_id`,`status`,`expires_at`,`id`),
  KEY `idx_link_expiry` (`status`,`expires_at`,`id`),
  KEY `idx_link_cleanup` (`status`,`terminal_at`,`id`),
  CONSTRAINT `fk_link_challenge_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `chk_link_purpose` CHECK ((`purpose` = _utf8mb4'LINK_EMAIL')),
  CONSTRAINT `chk_link_status` CHECK ((`status` in (_utf8mb4'PENDING',_utf8mb4'COMPLETED',_utf8mb4'CANCELLED',_utf8mb4'EXPIRED'))),
  CONSTRAINT `chk_link_delivery_status` CHECK ((`delivery_status` in (_utf8mb4'PENDING',_utf8mb4'SENT',_utf8mb4'FAILED',_utf8mb4'UNKNOWN'))),
  CONSTRAINT `chk_link_attempts` CHECK ((`failed_attempts` <= 5)),
  CONSTRAINT `chk_link_times` CHECK (((`expires_at` > `created_at`) and (`otp_expires_at` > `created_at`) and (`otp_expires_at` <= `expires_at`) and (`resend_available_at` <= `expires_at`))),
  CONSTRAINT `chk_link_delivery_failure` CHECK (((`status` <> _utf8mb4'PENDING') or (`delivery_status` <> _utf8mb4'FAILED') or (`delivery_failure_code` is not null))),
  CONSTRAINT `chk_link_lifecycle` CHECK (((`status` = _utf8mb4'PENDING' and `identifier_normalized` is not null and `active_identifier_key` = concat(`purpose`,_utf8mb4':',`identifier_normalized`) and `active_user_purpose_key` = concat(cast(`user_id` as char),_utf8mb4':',`purpose`) and `flow_token_hash` is not null and `otp_hash` is not null and `terminal_at` is null) or (`status` in (_utf8mb4'COMPLETED',_utf8mb4'CANCELLED',_utf8mb4'EXPIRED') and `identifier_normalized` is null and `active_identifier_key` is null and `active_user_purpose_key` is null and `flow_token_hash` is null and `otp_hash` is null and `delivery_failure_code` is null and `terminal_at` is not null)))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `auth_method_link_challenges` WRITE;
/*!40000 ALTER TABLE `auth_method_link_challenges` DISABLE KEYS */;
/*!40000 ALTER TABLE `auth_method_link_challenges` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `social_auth_challenges`
--

DROP TABLE IF EXISTS `social_auth_challenges`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `social_auth_challenges` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `conflict_token_hash` char(64) CHARACTER SET ascii COLLATE ascii_bin DEFAULT NULL,
  `provider` varchar(16) NOT NULL,
  `provider_user_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  `provider_identity_fingerprint` char(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
  `provider_email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  `provider_email_verified` tinyint(1) DEFAULT NULL,
  `active_provider_key` varchar(96) CHARACTER SET ascii COLLATE ascii_bin DEFAULT NULL,
  `conflict_type` varchar(64) NOT NULL,
  `pending_registration_id` bigint unsigned DEFAULT NULL,
  `conflicting_user_id` bigint unsigned DEFAULT NULL,
  `resolution_action` varchar(64) DEFAULT NULL,
  `resolved_user_id` bigint unsigned DEFAULT NULL,
  `status` varchar(16) NOT NULL DEFAULT 'PENDING',
  `expires_at` datetime(6) NOT NULL,
  `terminal_at` datetime(6) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_social_conflict_token_hash` (`conflict_token_hash`),
  UNIQUE KEY `uq_social_active_provider` (`active_provider_key`),
  KEY `idx_social_pending_registration` (`pending_registration_id`,`status`,`expires_at`,`id`),
  KEY `idx_social_conflicting_user` (`conflicting_user_id`,`status`,`id`),
  KEY `idx_social_expiry` (`status`,`expires_at`,`id`),
  KEY `idx_social_cleanup` (`status`,`terminal_at`,`id`),
  CONSTRAINT `fk_social_pending_registration` FOREIGN KEY (`pending_registration_id`) REFERENCES `pending_registrations` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_social_conflicting_user` FOREIGN KEY (`conflicting_user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_social_resolved_user` FOREIGN KEY (`resolved_user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `chk_social_provider` CHECK ((`provider` in (_utf8mb4'GOOGLE',_utf8mb4'FACEBOOK'))),
  CONSTRAINT `chk_social_conflict_type` CHECK ((`conflict_type` in (_utf8mb4'PENDING_EMAIL_MISMATCH',_utf8mb4'ACTIVE_EMAIL_MATCH_UNLINKED_PROVIDER'))),
  CONSTRAINT `chk_social_resolution_action` CHECK ((`resolution_action` is null or `resolution_action` in (_utf8mb4'CONTINUE_OTP',_utf8mb4'CANCEL_PENDING_AND_CONTINUE_SOCIAL',_utf8mb4'LOGIN_EXISTING_ACCOUNT',_utf8mb4'START_ACCOUNT_RECOVERY'))),
  CONSTRAINT `chk_social_status` CHECK ((`status` in (_utf8mb4'PENDING',_utf8mb4'RESOLVED',_utf8mb4'CANCELLED',_utf8mb4'EXPIRED'))),
  CONSTRAINT `chk_social_times` CHECK ((`expires_at` > `created_at`)),
  CONSTRAINT `chk_social_context` CHECK (((`conflict_type` = _utf8mb4'PENDING_EMAIL_MISMATCH' and `pending_registration_id` is not null) or (`conflict_type` = _utf8mb4'ACTIVE_EMAIL_MATCH_UNLINKED_PROVIDER' and `conflicting_user_id` is not null))),
  CONSTRAINT `chk_social_action_by_conflict` CHECK (((`resolution_action` is null and `resolved_user_id` is null) or (`conflict_type` = _utf8mb4'PENDING_EMAIL_MISMATCH' and `resolution_action` = _utf8mb4'CONTINUE_OTP' and `resolved_user_id` is null) or (`conflict_type` = _utf8mb4'PENDING_EMAIL_MISMATCH' and `resolution_action` = _utf8mb4'CANCEL_PENDING_AND_CONTINUE_SOCIAL' and `resolved_user_id` is not null) or (`conflict_type` = _utf8mb4'ACTIVE_EMAIL_MATCH_UNLINKED_PROVIDER' and `resolution_action` in (_utf8mb4'LOGIN_EXISTING_ACCOUNT',_utf8mb4'START_ACCOUNT_RECOVERY') and `resolved_user_id` is null))),
  CONSTRAINT `chk_social_lifecycle` CHECK (((`status` = _utf8mb4'PENDING' and `conflict_token_hash` is not null and `provider_user_id` is not null and `active_provider_key` = concat(`provider`,_utf8mb4':',`provider_identity_fingerprint`) and `terminal_at` is null and `resolution_action` is null and `resolved_user_id` is null and ((`provider_email` is null and `provider_email_verified` is null) or (`provider_email` is not null and `provider_email_verified` is not null))) or (`status` = _utf8mb4'RESOLVED' and `conflict_token_hash` is null and `provider_user_id` is null and `provider_email` is null and `provider_email_verified` is null and `active_provider_key` is null and `resolution_action` is not null and `terminal_at` is not null) or (`status` in (_utf8mb4'CANCELLED',_utf8mb4'EXPIRED') and `conflict_token_hash` is null and `provider_user_id` is null and `provider_email` is null and `provider_email_verified` is null and `active_provider_key` is null and `resolution_action` is null and `resolved_user_id` is null and `terminal_at` is not null)))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `social_auth_challenges` WRITE;
/*!40000 ALTER TABLE `social_auth_challenges` DISABLE KEYS */;
/*!40000 ALTER TABLE `social_auth_challenges` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `reauthentication_challenges`
--

DROP TABLE IF EXISTS `reauthentication_challenges`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reauthentication_challenges` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint unsigned NOT NULL,
  `token_hash` char(64) CHARACTER SET ascii COLLATE ascii_bin DEFAULT NULL,
  `proof_method` varchar(32) NOT NULL,
  `scope` varchar(32) NOT NULL,
  `target_auth_method` varchar(16) NOT NULL,
  `active_user_scope_key` varchar(96) CHARACTER SET ascii COLLATE ascii_bin DEFAULT NULL,
  `status` varchar(16) NOT NULL DEFAULT 'ACTIVE',
  `expires_at` datetime(6) NOT NULL,
  `terminal_at` datetime(6) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_reauth_token_hash` (`token_hash`),
  UNIQUE KEY `uq_reauth_active_user_scope` (`active_user_scope_key`),
  KEY `idx_reauth_user_state` (`user_id`,`status`,`expires_at`,`id`),
  KEY `idx_reauth_expiry` (`status`,`expires_at`,`id`),
  KEY `idx_reauth_cleanup` (`status`,`terminal_at`,`id`),
  CONSTRAINT `fk_reauth_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `chk_reauth_proof_method` CHECK ((`proof_method` in (_utf8mb4'LOCAL_PASSWORD',_utf8mb4'GOOGLE',_utf8mb4'FACEBOOK'))),
  CONSTRAINT `chk_reauth_scope` CHECK ((`scope` in (_utf8mb4'UNLINK_AUTH_METHOD',_utf8mb4'SET_PASSWORD'))),
  CONSTRAINT `chk_reauth_target_method` CHECK ((`target_auth_method` in (_utf8mb4'EMAIL',_utf8mb4'GOOGLE',_utf8mb4'FACEBOOK'))),
  CONSTRAINT `chk_reauth_status` CHECK ((`status` in (_utf8mb4'ACTIVE',_utf8mb4'CONSUMED',_utf8mb4'CANCELLED',_utf8mb4'EXPIRED'))),
  CONSTRAINT `chk_reauth_times` CHECK ((`expires_at` > `created_at`)),
  CONSTRAINT `chk_reauth_lifecycle` CHECK (((`status` = _utf8mb4'ACTIVE' and `token_hash` is not null and `active_user_scope_key` = concat(cast(`user_id` as char),_utf8mb4':',`scope`) and `terminal_at` is null) or (`status` in (_utf8mb4'CONSUMED',_utf8mb4'CANCELLED',_utf8mb4'EXPIRED') and `token_hash` is null and `active_user_scope_key` is null and `terminal_at` is not null)))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `reauthentication_challenges` WRITE;
/*!40000 ALTER TABLE `reauthentication_challenges` DISABLE KEYS */;
/*!40000 ALTER TABLE `reauthentication_challenges` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `password_recovery_challenges`
--

DROP TABLE IF EXISTS `password_recovery_challenges`;
CREATE TABLE `password_recovery_challenges` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint unsigned DEFAULT NULL,
  `is_decoy` boolean NOT NULL,
  `subject_key_hash` char(64) NOT NULL,
  `active_subject_key_hash` char(64) DEFAULT NULL,
  `delivery_channel` varchar(16) NOT NULL,
  `recovery_flow_token_hash` char(64) DEFAULT NULL,
  `otp_hash` char(64) DEFAULT NULL,
  `reset_token_hash` char(64) DEFAULT NULL,
  `otp_expires_at` datetime(6) NOT NULL,
  `challenge_expires_at` datetime(6) NOT NULL,
  `reset_token_expires_at` datetime(6) DEFAULT NULL,
  `resend_available_at` datetime(6) NOT NULL,
  `failed_attempts` int unsigned NOT NULL DEFAULT 0,
  `otp_version` int unsigned NOT NULL DEFAULT 1,
  `status` varchar(16) NOT NULL DEFAULT 'PENDING',
  `delivery_status` varchar(16) NOT NULL,
  `delivery_attempts` int unsigned NOT NULL DEFAULT 0,
  `delivery_failure_code` varchar(64) DEFAULT NULL,
  `verified_at` datetime(6) DEFAULT NULL,
  `completed_at` datetime(6) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_recovery_flow_hash` (`recovery_flow_token_hash`), UNIQUE KEY `uq_recovery_reset_hash` (`reset_token_hash`),
  UNIQUE KEY `uq_recovery_active_subject` (`active_subject_key_hash`),
  KEY `idx_recovery_expiry` (`status`,`challenge_expires_at`,`id`), KEY `idx_recovery_cleanup` (`status`,`updated_at`,`id`),
  KEY `idx_recovery_user_status` (`user_id`,`status`,`id`),
  CONSTRAINT `fk_recovery_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `chk_recovery_channel` CHECK (`delivery_channel` = 'EMAIL'),
  CONSTRAINT `chk_recovery_status` CHECK (`status` IN ('PENDING','VERIFIED','COMPLETED','EXPIRED','LOCKED')),
  CONSTRAINT `chk_recovery_delivery` CHECK (`delivery_status` IN ('NOT_APPLICABLE','PENDING','SENDING','SENT','FAILED','UNKNOWN')),
  CONSTRAINT `chk_recovery_expiry` CHECK (`otp_expires_at` <= `challenge_expires_at`),
  CONSTRAINT `chk_recovery_decoy` CHECK ((`is_decoy` = true AND `user_id` IS NULL AND `delivery_status` = 'NOT_APPLICABLE') OR (`is_decoy` = false AND `user_id` IS NOT NULL)),
  CONSTRAINT `chk_recovery_pending` CHECK ((`status` = 'PENDING' AND `active_subject_key_hash` IS NOT NULL AND `recovery_flow_token_hash` IS NOT NULL AND `otp_hash` IS NOT NULL AND `reset_token_hash` IS NULL) OR `status` <> 'PENDING'),
  CONSTRAINT `chk_recovery_verified` CHECK ((`status` = 'VERIFIED' AND `is_decoy` = false AND `recovery_flow_token_hash` IS NULL AND `otp_hash` IS NULL AND `reset_token_hash` IS NOT NULL AND `verified_at` IS NOT NULL) OR `status` <> 'VERIFIED'),
  CONSTRAINT `chk_recovery_completed` CHECK ((`status` = 'COMPLETED' AND `reset_token_hash` IS NOT NULL AND `completed_at` IS NOT NULL) OR `status` <> 'COMPLETED')
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Bảng legacy được giữ nguyên để audit; PasswordRecoveryService không ghi vào bảng này.
-- Table structure for table `password_reset_tokens`
--

DROP TABLE IF EXISTS `password_reset_tokens`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `password_reset_tokens` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint unsigned NOT NULL,
  `delivery_channel` varchar(16) NOT NULL,
  `token_hash` char(64) NOT NULL,
  `expires_at` datetime(6) NOT NULL,
  `used_at` datetime(6) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_password_reset_tokens_hash` (`token_hash`),
  KEY `idx_password_reset_tokens_user_state` (`user_id`,`used_at`,`expires_at`,`id`),
  CONSTRAINT `fk_password_reset_tokens_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `chk_password_reset_tokens_delivery_channel` CHECK ((`delivery_channel` = _utf8mb4'EMAIL')),
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
  KEY `idx_post_likes_user_created` (`user_id`,`created_at` DESC,`post_id` DESC),
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
/*!40000 ALTER TABLE `post_likes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `post_reposts`
--

DROP TABLE IF EXISTS `post_reposts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `post_reposts` (
  `user_id` bigint unsigned NOT NULL,
  `post_id` bigint unsigned NOT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`user_id`,`post_id`),
  KEY `idx_post_reposts_user_created` (`user_id`,`created_at` DESC,`post_id` DESC),
  KEY `idx_post_reposts_post_user` (`post_id`,`user_id`),
  CONSTRAINT `fk_post_reposts_post` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_post_reposts_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `post_reposts`
--

LOCK TABLES `post_reposts` WRITE;
/*!40000 ALTER TABLE `post_reposts` DISABLE KEYS */;
/*!40000 ALTER TABLE `post_reposts` ENABLE KEYS */;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `post_media`
--

LOCK TABLES `post_media` WRITE;
/*!40000 ALTER TABLE `post_media` DISABLE KEYS */;
/*!40000 ALTER TABLE `post_media` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `locations`
--

DROP TABLE IF EXISTS `locations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `locations` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `google_place_id` varchar(255) NOT NULL,
  `display_name` varchar(255) NOT NULL,
  `formatted_address` varchar(500) DEFAULT NULL,
  `latitude` decimal(10,7) NOT NULL,
  `longitude` decimal(10,7) NOT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_locations_google_place_id` (`google_place_id`),
  CONSTRAINT `chk_locations_latitude` CHECK ((`latitude` between -(90) and 90)),
  CONSTRAINT `chk_locations_longitude` CHECK ((`longitude` between -(180) and 180))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `locations`
--

LOCK TABLES `locations` WRITE;
/*!40000 ALTER TABLE `locations` DISABLE KEYS */;
/*!40000 ALTER TABLE `locations` ENABLE KEYS */;
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
  `location_id` bigint unsigned DEFAULT NULL,
  `content` varchar(500) DEFAULT NULL,
  `status` enum('PUBLISHED','HIDDEN','DELETED') NOT NULL DEFAULT 'PUBLISHED',
  `is_edited` tinyint(1) NOT NULL DEFAULT '0',
  `like_count` int unsigned NOT NULL DEFAULT '0',
  `comment_count` int unsigned NOT NULL DEFAULT '0',
  `repost_count` int unsigned NOT NULL DEFAULT '0',
  `published_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `hidden_by` bigint unsigned DEFAULT NULL,
  `hidden_at` datetime(6) DEFAULT NULL,
  `hidden_reason` varchar(500) DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  KEY `fk_posts_hidden_by` (`hidden_by`),
  KEY `idx_posts_location_id` (`location_id`),
  KEY `idx_posts_author_status_published` (`author_id`,`status`,`published_at` DESC,`id` DESC),
  KEY `idx_posts_status_published` (`status`,`published_at` DESC,`id` DESC),
  KEY `idx_posts_status_engagement` (`status`,`like_count` DESC,`comment_count` DESC,`published_at` DESC,`id` DESC),
  FULLTEXT KEY `ftx_posts_content` (`content`),
  CONSTRAINT `fk_posts_author` FOREIGN KEY (`author_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_posts_hidden_by` FOREIGN KEY (`hidden_by`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_posts_location` FOREIGN KEY (`location_id`) REFERENCES `locations` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `chk_posts_deleted_state` CHECK ((((`status` = _utf8mb4'DELETED') and (`deleted_at` is not null)) or (`status` <> _utf8mb4'DELETED'))),
  CONSTRAINT `chk_posts_hidden_state` CHECK ((((`status` = _utf8mb4'HIDDEN') and (`hidden_at` is not null) and (`hidden_by` is not null)) or (`status` <> _utf8mb4'HIDDEN')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `posts`
--

LOCK TABLES `posts` WRITE;
/*!40000 ALTER TABLE `posts` DISABLE KEYS */;
/*!40000 ALTER TABLE `posts` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `moderation_cases`
--

DROP TABLE IF EXISTS `moderation_cases`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `moderation_cases` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `post_id` bigint unsigned NOT NULL,
  `status` enum('OPEN','RESOLVED_NO_VIOLATION','RESOLVED_ACTION_TAKEN') NOT NULL DEFAULT 'OPEN',
  `report_count` int unsigned NOT NULL DEFAULT '0',
  `resolved_by` bigint unsigned DEFAULT NULL,
  `resolution_note` varchar(1000) DEFAULT NULL,
  `first_reported_at` datetime(6) NOT NULL,
  `latest_reported_at` datetime(6) NOT NULL,
  `resolved_at` datetime(6) DEFAULT NULL,
  `open_post_key` bigint unsigned GENERATED ALWAYS AS ((case when (`status` = _utf8mb4'OPEN') then `post_id` else NULL end)) STORED,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_moderation_cases_open_post` (`open_post_key`),
  KEY `idx_moderation_cases_post` (`post_id`),
  KEY `idx_moderation_cases_status_latest` (`status`,`latest_reported_at` DESC,`id` DESC),
  KEY `idx_moderation_cases_resolved_by` (`resolved_by`),
  CONSTRAINT `fk_moderation_cases_post` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_moderation_cases_resolved_by` FOREIGN KEY (`resolved_by`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `chk_moderation_cases_report_count` CHECK ((`report_count` >= 0)),
  CONSTRAINT `chk_moderation_cases_reported_time` CHECK ((`latest_reported_at` >= `first_reported_at`)),
  CONSTRAINT `chk_moderation_cases_resolution_state` CHECK (((`status` = _utf8mb4'OPEN' and `resolved_by` is null and `resolved_at` is null) or (`status` in (_utf8mb4'RESOLVED_NO_VIOLATION',_utf8mb4'RESOLVED_ACTION_TAKEN') and `resolved_by` is not null and `resolved_at` is not null)))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `moderation_cases` WRITE;
/*!40000 ALTER TABLE `moderation_cases` DISABLE KEYS */;
/*!40000 ALTER TABLE `moderation_cases` ENABLE KEYS */;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `refresh_tokens`
--

LOCK TABLES `refresh_tokens` WRITE;
/*!40000 ALTER TABLE `refresh_tokens` DISABLE KEYS */;
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
  `moderation_case_id` bigint unsigned DEFAULT NULL,
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
  KEY `idx_reports_moderation_case_created` (`moderation_case_id`,`created_at` DESC,`id` DESC),
  KEY `idx_reports_reporter_post` (`reporter_id`,`post_id`),
  CONSTRAINT `fk_reports_post` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_reports_reporter` FOREIGN KEY (`reporter_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_reports_resolved_by` FOREIGN KEY (`resolved_by`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_reports_moderation_case` FOREIGN KEY (`moderation_case_id`) REFERENCES `moderation_cases` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `chk_reports_resolution_state` CHECK ((((`status` = _utf8mb4'PENDING') and (`resolved_by` is null) and (`resolved_at` is null)) or ((`status` in (_utf8mb4'RESOLVED',_utf8mb4'REJECTED')) and (`resolved_by` is not null) and (`resolved_at` is not null))))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reports`
--

LOCK TABLES `reports` WRITE;
/*!40000 ALTER TABLE `reports` DISABLE KEYS */;
/*!40000 ALTER TABLE `reports` ENABLE KEYS */;
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
  `type` enum('FOLLOW','POST_LIKE','POST_COMMENT','COMMENT_REPLY','POST_REPOST','REPORT_RESOLVED','REPORT_REJECTED','POST_HIDDEN_BY_ADMIN','POST_RESTORED_BY_ADMIN','PROFILE_UPDATED_BY_ADMIN','ACCOUNT_BLOCKED','ACCOUNT_UNBLOCKED') NOT NULL,
  `post_id` bigint unsigned DEFAULT NULL,
  `comment_id` bigint unsigned DEFAULT NULL,
  `report_id` bigint unsigned DEFAULT NULL,
  `read_at` datetime(6) DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  KEY `idx_notifications_recipient_created` (`recipient_id`,`deleted_at`,`created_at` DESC,`id` DESC),
  KEY `idx_notifications_recipient_unread` (`recipient_id`,`deleted_at`,`read_at`,`id`),
  KEY `fk_notifications_actor` (`actor_id`),
  KEY `fk_notifications_post` (`post_id`),
  KEY `fk_notifications_comment` (`comment_id`),
  KEY `fk_notifications_report` (`report_id`),
  CONSTRAINT `fk_notifications_actor` FOREIGN KEY (`actor_id`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `fk_notifications_comment` FOREIGN KEY (`comment_id`) REFERENCES `comments` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `fk_notifications_post` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `fk_notifications_recipient` FOREIGN KEY (`recipient_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_notifications_report` FOREIGN KEY (`report_id`) REFERENCES `reports` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

-- Các quy tắc actor theo loại và chặn tự thông báo được kiểm tra ở service. MySQL không cho
-- actor_id tham gia CHECK đồng thời dùng foreign key ON DELETE SET NULL (ERROR 3823).

LOCK TABLES `notifications` WRITE;
/*!40000 ALTER TABLE `notifications` DISABLE KEYS */;
/*!40000 ALTER TABLE `notifications` ENABLE KEYS */;
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
/*!40000 ALTER TABLE `saved_posts` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_auth_providers`
--

DROP TABLE IF EXISTS `user_auth_providers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_auth_providers` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint unsigned NOT NULL,
  `provider` varchar(16) NOT NULL,
  `provider_user_id` varchar(255) NOT NULL,
  `provider_email` varchar(255) DEFAULT NULL,
  `provider_email_verified` tinyint(1) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_user_auth_provider_identity` (`provider`,`provider_user_id`),
  UNIQUE KEY `uq_user_auth_provider_per_user` (`user_id`,`provider`),
  KEY `idx_user_auth_providers_user` (`user_id`,`provider`,`id`),
  CONSTRAINT `fk_user_auth_providers_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `chk_user_auth_providers_provider` CHECK ((`provider` in (_utf8mb4'GOOGLE',_utf8mb4'FACEBOOK'))),
  CONSTRAINT `chk_user_auth_providers_provider_user_id` CHECK ((char_length(trim(`provider_user_id`)) > 0)),
  CONSTRAINT `chk_user_auth_providers_email` CHECK (((`provider_email` is null and `provider_email_verified` is null) or (`provider_email` is not null and `provider_email_verified` is not null)))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_auth_providers`
--

LOCK TABLES `user_auth_providers` WRITE;
/*!40000 ALTER TABLE `user_auth_providers` DISABLE KEYS */;
/*!40000 ALTER TABLE `user_auth_providers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_profiles`
--

DROP TABLE IF EXISTS `schools`;
CREATE TABLE `schools` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `short_name` varchar(50) DEFAULT NULL,
  `status` varchar(16) NOT NULL DEFAULT 'ACTIVE',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_schools_name` (`name`),
  KEY `idx_schools_status_name` (`status`,`name`),
  KEY `idx_schools_status_short_name` (`status`,`short_name`),
  CONSTRAINT `chk_schools_status` CHECK (`status` IN ('ACTIVE','INACTIVE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `faculties`;
CREATE TABLE `faculties` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `school_id` bigint unsigned NOT NULL,
  `name` varchar(255) NOT NULL,
  `status` varchar(16) NOT NULL DEFAULT 'ACTIVE',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_faculties_school_name` (`school_id`,`name`),
  KEY `idx_faculties_school_status_name` (`school_id`,`status`,`name`),
  CONSTRAINT `fk_faculties_school` FOREIGN KEY (`school_id`) REFERENCES `schools` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `chk_faculties_status` CHECK (`status` IN ('ACTIVE','INACTIVE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `majors`;
CREATE TABLE `majors` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `faculty_id` bigint unsigned NOT NULL,
  `name` varchar(255) NOT NULL,
  `status` varchar(16) NOT NULL DEFAULT 'ACTIVE',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_majors_faculty_name` (`faculty_id`,`name`),
  KEY `idx_majors_faculty_status_name` (`faculty_id`,`status`,`name`),
  CONSTRAINT `fk_majors_faculty` FOREIGN KEY (`faculty_id`) REFERENCES `faculties` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `chk_majors_status` CHECK (`status` IN ('ACTIVE','INACTIVE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `interest_categories`;
CREATE TABLE `interest_categories` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `status` varchar(16) NOT NULL DEFAULT 'ACTIVE',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_interest_categories_name` (`name`),
  KEY `idx_interest_categories_status_name` (`status`,`name`),
  CONSTRAINT `chk_interest_categories_status` CHECK (`status` IN ('ACTIVE','INACTIVE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `user_profiles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_profiles` (
  `user_id` bigint unsigned NOT NULL,
  `username` varchar(30) DEFAULT NULL,
  `display_name` varchar(100) DEFAULT NULL,
  `avatar_url` varchar(1000) DEFAULT NULL,
  `avatar_public_id` varchar(255) DEFAULT NULL,
  `bio` varchar(500) DEFAULT NULL,
  `date_of_birth` date DEFAULT NULL,
  `school_id` bigint unsigned DEFAULT NULL,
  `faculty_id` bigint unsigned DEFAULT NULL,
  `major_id` bigint unsigned DEFAULT NULL,
  `entry_year` smallint unsigned DEFAULT NULL,
  `profile_completed_at` datetime(6) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uq_user_profiles_username` (`username`),
  FULLTEXT KEY `ftx_user_profiles_display_name` (`display_name`),
  KEY `idx_user_profiles_school` (`school_id`),
  KEY `idx_user_profiles_faculty` (`faculty_id`),
  KEY `idx_user_profiles_major` (`major_id`),
  CONSTRAINT `fk_user_profiles_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_user_profiles_school` FOREIGN KEY (`school_id`) REFERENCES `schools` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `fk_user_profiles_faculty` FOREIGN KEY (`faculty_id`) REFERENCES `faculties` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `fk_user_profiles_major` FOREIGN KEY (`major_id`) REFERENCES `majors` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `chk_user_profiles_completion_consistency` CHECK (((`profile_completed_at` is null) or ((`username` is not null) and (`display_name` is not null) and (`date_of_birth` is not null)))),
  CONSTRAINT `chk_user_profiles_completion_requires_birth_date` CHECK (((`profile_completed_at` is null) or (`date_of_birth` is not null))),
  CONSTRAINT `chk_user_profiles_display_name_not_blank` CHECK (((`display_name` is null) or (char_length(trim(`display_name`)) > 0))),
  CONSTRAINT `chk_user_profiles_entry_year` CHECK (`entry_year` IS NULL OR `entry_year` BETWEEN 1900 AND 9999)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_profiles`
--

LOCK TABLES `user_profiles` WRITE;
/*!40000 ALTER TABLE `user_profiles` DISABLE KEYS */;
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
  `email_verified_at` datetime(6) DEFAULT NULL,
  `password_hash` varchar(255) DEFAULT NULL,
  `role` varchar(16) NOT NULL DEFAULT 'USER',
  `status` varchar(16) NOT NULL DEFAULT 'ACTIVE',
  `blocked_at` datetime(6) DEFAULT NULL,
  `blocked_reason` varchar(500) DEFAULT NULL,
  `first_active_at` datetime(6) DEFAULT NULL,
  `last_active_at` datetime(6) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_users_email` (`email`),
  KEY `idx_users_status_created_at` (`status`,`created_at` DESC,`id` DESC),
  CONSTRAINT `chk_users_role` CHECK ((`role` in (_utf8mb4'USER',_utf8mb4'ADMIN'))),
  CONSTRAINT `chk_users_status` CHECK ((`status` in (_utf8mb4'ACTIVE',_utf8mb4'BLOCKED'))),
  CONSTRAINT `chk_users_blocked_data` CHECK ((((`status` = _utf8mb4'ACTIVE') and (`blocked_at` is null)) or ((`status` = _utf8mb4'BLOCKED') and (`blocked_at` is not null)))),
  CONSTRAINT `chk_users_email_not_blank` CHECK (((`email` is null) or (char_length(trim(`email`)) > 0))),
  CONSTRAINT `chk_users_email_verification_consistency` CHECK (((`email` is not null) or (`email_verified_at` is null))),
  CONSTRAINT `chk_users_password_verified_email` CHECK (((`password_hash` is null) or (`email` is not null and `email_verified_at` is not null)))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Academic interests dùng khóa chính kép để chống trùng tại database.
DROP TABLE IF EXISTS `user_interests`;
CREATE TABLE `user_interests` (
  `user_id` bigint unsigned NOT NULL,
  `interest_id` bigint unsigned NOT NULL,
  PRIMARY KEY (`user_id`,`interest_id`),
  KEY `idx_user_interests_interest_user` (`interest_id`,`user_id`),
  CONSTRAINT `fk_user_interests_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_user_interests_interest` FOREIGN KEY (`interest_id`) REFERENCES `interest_categories` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Direct Messaging REST Core (không có dữ liệu seed cũ để backfill)
--

DROP TABLE IF EXISTS `messages`;
DROP TABLE IF EXISTS `media_cleanup_tasks`;
DROP TABLE IF EXISTS `message_attachments`;
DROP TABLE IF EXISTS `conversation_members`;
DROP TABLE IF EXISTS `conversations`;

CREATE TABLE `conversations` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `participant_low_id` bigint unsigned NOT NULL,
  `participant_high_id` bigint unsigned NOT NULL,
  `last_message_id` bigint unsigned DEFAULT NULL,
  `last_message_at` datetime(6) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_conversations_participant_pair` (`participant_low_id`,`participant_high_id`),
  KEY `idx_conversations_last_message` (`last_message_at` DESC,`id` DESC),
  CONSTRAINT `fk_conversations_participant_low` FOREIGN KEY (`participant_low_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_conversations_participant_high` FOREIGN KEY (`participant_high_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `chk_conversations_participant_order` CHECK ((`participant_low_id` < `participant_high_id`))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `conversation_members` (
  `conversation_id` bigint unsigned NOT NULL,
  `user_id` bigint unsigned NOT NULL,
  `last_read_message_id` bigint unsigned DEFAULT NULL,
  `last_read_at` datetime(6) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`conversation_id`,`user_id`),
  KEY `idx_conversation_members_user_cursor` (`user_id`,`conversation_id`,`last_read_message_id`),
  CONSTRAINT `fk_conversation_members_conversation` FOREIGN KEY (`conversation_id`) REFERENCES `conversations` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_conversation_members_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `messages` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `conversation_id` bigint unsigned NOT NULL,
  `sender_id` bigint unsigned NOT NULL,
  `client_message_id` char(36) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
  `type` enum('TEXT','IMAGE','POST_SHARE') NOT NULL,
  `content` varchar(2000) DEFAULT NULL,
  `shared_post_id` bigint unsigned DEFAULT NULL,
  `payload_fingerprint` char(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_messages_sender_client_message` (`sender_id`,`client_message_id`),
  KEY `idx_messages_conversation_cursor` (`conversation_id`,`id` DESC),
  KEY `idx_messages_shared_post` (`shared_post_id`,`id`),
  CONSTRAINT `fk_messages_conversation` FOREIGN KEY (`conversation_id`) REFERENCES `conversations` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_messages_sender_member` FOREIGN KEY (`conversation_id`,`sender_id`) REFERENCES `conversation_members` (`conversation_id`,`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_messages_shared_post` FOREIGN KEY (`shared_post_id`) REFERENCES `posts` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `chk_messages_payload_shape` CHECK (((`type` = 'TEXT' AND `content` IS NOT NULL AND char_length(trim(`content`)) > 0) OR `type` IN ('IMAGE','POST_SHARE'))),
  CONSTRAINT `chk_messages_content_length` CHECK ((char_length(`content`) <= 2000))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `message_attachments` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `message_id` bigint unsigned NOT NULL,
  `media_type` enum('IMAGE') NOT NULL,
  `storage_provider` enum('CLOUDINARY') NOT NULL,
  `storage_public_id` varchar(255) NOT NULL,
  `mime_type` varchar(64) NOT NULL,
  `file_size_bytes` bigint unsigned NOT NULL,
  `width` int unsigned NOT NULL,
  `height` int unsigned NOT NULL,
  `display_order` tinyint unsigned NOT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_message_attachments_message_order` (`message_id`,`display_order`),
  UNIQUE KEY `uq_message_attachments_storage_asset` (`storage_provider`,`storage_public_id`),
  KEY `idx_message_attachments_message` (`message_id`,`id`),
  CONSTRAINT `fk_message_attachments_message` FOREIGN KEY (`message_id`) REFERENCES `messages` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `chk_message_attachments_size` CHECK (`file_size_bytes` > 0),
  CONSTRAINT `chk_message_attachments_dimensions` CHECK (`width` > 0 AND `height` > 0),
  CONSTRAINT `chk_message_attachments_order` CHECK (`display_order` BETWEEN 0 AND 4)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `media_cleanup_tasks` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `storage_provider` varchar(32) NOT NULL,
  `storage_public_id` varchar(255) NOT NULL,
  `resource_type` varchar(32) NOT NULL,
  `reason` varchar(64) NOT NULL,
  `status` enum('PENDING','PROCESSING','COMPLETED','FAILED') NOT NULL,
  `attempt_count` int unsigned NOT NULL DEFAULT 0,
  `next_retry_at` datetime(6) NOT NULL,
  `last_error` varchar(500) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  KEY `idx_media_cleanup_due` (`status`,`next_retry_at`,`id`),
  KEY `idx_media_cleanup_asset` (`storage_provider`,`storage_public_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

ALTER TABLE `conversations`
  ADD CONSTRAINT `fk_conversations_last_message` FOREIGN KEY (`last_message_id`) REFERENCES `messages` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;
ALTER TABLE `conversation_members`
  ADD CONSTRAINT `fk_conversation_members_last_read_message` FOREIGN KEY (`last_read_message_id`) REFERENCES `messages` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

-- Table structure for table `user_daily_activities`
--

DROP TABLE IF EXISTS `user_daily_activities`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_daily_activities` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint unsigned NOT NULL,
  `activity_date` date NOT NULL,
  `first_active_at` datetime(6) NOT NULL,
  `last_active_at` datetime(6) NOT NULL,
  `activity_count` int unsigned NOT NULL DEFAULT '1',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_user_daily_activities_user_date` (`user_id`,`activity_date`),
  KEY `idx_user_daily_activities_date_user` (`activity_date`,`user_id`),
  CONSTRAINT `fk_user_daily_activities_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `chk_user_daily_activities_count` CHECK ((`activity_count` > 0)),
  CONSTRAINT `chk_user_daily_activities_time` CHECK ((`first_active_at` <= `last_active_at`))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_daily_activities`
--

LOCK TABLES `user_daily_activities` WRITE;
/*!40000 ALTER TABLE `user_daily_activities` DISABLE KEYS */;
/*!40000 ALTER TABLE `user_daily_activities` ENABLE KEYS */;
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

-- Dump completed on 2026-07-19 10:03:55

--
-- Counter triggers required by PostLikeService, CommentService and HashtagService
--

DELIMITER ;;
CREATE TRIGGER `trg_post_likes_after_insert`
AFTER INSERT ON `post_likes`
FOR EACH ROW
BEGIN
  UPDATE `posts` SET `like_count` = `like_count` + 1 WHERE `id` = NEW.`post_id`;
END;;

CREATE TRIGGER `trg_post_likes_after_delete`
AFTER DELETE ON `post_likes`
FOR EACH ROW
BEGIN
  UPDATE `posts` SET `like_count` = GREATEST(`like_count` - 1, 0) WHERE `id` = OLD.`post_id`;
END;;

CREATE TRIGGER `trg_post_reposts_after_insert`
AFTER INSERT ON `post_reposts`
FOR EACH ROW
BEGIN
  UPDATE `posts` SET `repost_count` = `repost_count` + 1 WHERE `id` = NEW.`post_id`;
END;;

CREATE TRIGGER `trg_post_reposts_after_delete`
AFTER DELETE ON `post_reposts`
FOR EACH ROW
BEGIN
  UPDATE `posts` SET `repost_count` = GREATEST(`repost_count` - 1, 0) WHERE `id` = OLD.`post_id`;
END;;

CREATE TRIGGER `trg_comments_after_insert`
AFTER INSERT ON `comments`
FOR EACH ROW
BEGIN
  IF NEW.`status` = 'PUBLISHED' THEN
    UPDATE `posts` SET `comment_count` = `comment_count` + 1 WHERE `id` = NEW.`post_id`;
  END IF;
END;;

CREATE TRIGGER `trg_comments_after_update`
AFTER UPDATE ON `comments`
FOR EACH ROW
BEGIN
  IF OLD.`status` = 'PUBLISHED' AND NEW.`status` = 'DELETED' THEN
    UPDATE `posts` SET `comment_count` = GREATEST(`comment_count` - 1, 0) WHERE `id` = NEW.`post_id`;
  ELSEIF OLD.`status` = 'DELETED' AND NEW.`status` = 'PUBLISHED' THEN
    UPDATE `posts` SET `comment_count` = `comment_count` + 1 WHERE `id` = NEW.`post_id`;
  END IF;
END;;

CREATE TRIGGER `trg_post_hashtags_after_insert`
AFTER INSERT ON `post_hashtags`
FOR EACH ROW
BEGIN
  UPDATE `hashtags` SET `post_count` = `post_count` + 1 WHERE `id` = NEW.`hashtag_id`;
END;;

CREATE TRIGGER `trg_post_hashtags_after_delete`
AFTER DELETE ON `post_hashtags`
FOR EACH ROW
BEGIN
  UPDATE `hashtags` SET `post_count` = GREATEST(`post_count` - 1, 0) WHERE `id` = OLD.`hashtag_id`;
END;;
DELIMITER ;

-- Master data Academic DEV/DEMO; không phải danh mục chính thức hoặc đầy đủ của Việt Nam.
INSERT INTO schools (id, name, short_name, status) VALUES
    (1, 'Trường Đại học Công Nghệ Sài Gòn', 'STU', 'ACTIVE'),
    (2, 'Trường Đại học Bách khoa - Đại học Quốc gia TP.HCM', 'HCMUT', 'ACTIVE'),
    (3, 'Trường Đại học Khoa học Tự nhiên - Đại học Quốc gia TP.HCM', 'HCMUS', 'ACTIVE'),
    (4, 'Trường Đại học Kinh tế TP.HCM', 'UEH', 'ACTIVE'),
    (5, 'Trường Đại học Sư phạm Kỹ thuật TP.HCM', 'HCMUTE', 'ACTIVE');

INSERT INTO faculties (id, school_id, name, status) VALUES
    (1, 1, 'Khoa Công nghệ Thông tin', 'ACTIVE'),
    (2, 1, 'Khoa Điện - Điện tử', 'ACTIVE'),
    (3, 1, 'Khoa Kỹ thuật Công trình', 'ACTIVE'),
    (4, 1, 'Khoa Cơ khí', 'ACTIVE'),
    (5, 1, 'Khoa Công nghệ Thực phẩm', 'ACTIVE'),
    (6, 1, 'Khoa Quản trị Kinh doanh', 'ACTIVE'),
    (7, 2, 'Khoa Khoa học và Kỹ thuật Máy tính', 'ACTIVE'),
    (8, 3, 'Khoa Công nghệ Thông tin', 'ACTIVE'),
    (9, 4, 'Khoa Kinh doanh', 'ACTIVE'),
    (10, 5, 'Khoa Công nghệ Thông tin', 'ACTIVE');

INSERT INTO majors (id, faculty_id, name, status) VALUES
    (1, 1, 'Công nghệ Thông tin', 'ACTIVE'), (2, 1, 'Khoa học Dữ liệu', 'ACTIVE'),
    (3, 2, 'Kỹ thuật Điện', 'ACTIVE'), (4, 2, 'Kỹ thuật Điện tử - Viễn thông', 'ACTIVE'),
    (5, 3, 'Kỹ thuật Xây dựng', 'ACTIVE'), (6, 3, 'Quản lý Xây dựng', 'ACTIVE'),
    (7, 4, 'Công nghệ Kỹ thuật Cơ điện tử', 'ACTIVE'), (8, 4, 'Công nghệ Kỹ thuật Ô tô', 'ACTIVE'),
    (9, 5, 'Công nghệ Thực phẩm', 'ACTIVE'), (10, 5, 'Đảm bảo Chất lượng và An toàn Thực phẩm', 'ACTIVE'),
    (11, 6, 'Quản trị Kinh doanh', 'ACTIVE'), (12, 6, 'Kinh doanh Quốc tế', 'ACTIVE'),
    (13, 7, 'Khoa học Máy tính', 'ACTIVE'), (14, 8, 'Công nghệ Thông tin', 'ACTIVE'),
    (15, 9, 'Quản trị Kinh doanh', 'ACTIVE'), (16, 10, 'Công nghệ Thông tin', 'ACTIVE');

INSERT INTO interest_categories (id, name, status) VALUES
    (1, 'Lập trình', 'ACTIVE'), (2, 'Trí tuệ nhân tạo', 'ACTIVE'),
    (3, 'Khoa học dữ liệu', 'ACTIVE'), (4, 'An toàn thông tin', 'ACTIVE'),
    (5, 'Thiết kế UI/UX', 'ACTIVE'), (6, 'Khởi nghiệp', 'ACTIVE'),
    (7, 'Ngoại ngữ', 'ACTIVE'), (8, 'Nhiếp ảnh', 'ACTIVE'),
    (9, 'Âm nhạc', 'ACTIVE'), (10, 'Thể thao', 'ACTIVE'),
    (11, 'Đọc sách', 'ACTIVE'), (12, 'Du lịch', 'ACTIVE'),
    (13, 'Tình nguyện', 'ACTIVE'), (14, 'Nghiên cứu khoa học', 'ACTIVE'),
    (15, 'Kỹ năng mềm', 'ACTIVE'), (16, 'Cơ hội thực tập', 'ACTIVE');


-- =============================================================================
-- CANONICAL DEMO DATASET: EXACTLY 1,000 USERS AND 1,000 POSTS
-- This section is embedded so another machine only imports this one SQL file.
-- =============================================================================
-- =============================================================================
-- SEED 1.000 USER VA 1.000 POST CHO MOI TRUONG LOCAL/TEST
-- =============================================================================
-- Chi chay tren database student_social_network dung cho local/test.
-- Script xoa toan bo du lieu nghiep vu hien co, giu nguyen schema va trigger.
-- Tat ca tai khoan local dung chung mat khau test: TestUser01@2026
-- Anh demo dung URL seed on dinh cua Lorem Picsum, khong luu file dang BLOB.

USE `student_social_network`;
SET NAMES utf8mb4;
SET time_zone = '+07:00';

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE `media_cleanup_tasks`;
TRUNCATE TABLE `message_attachments`;
TRUNCATE TABLE `conversation_members`;
TRUNCATE TABLE `messages`;
TRUNCATE TABLE `conversations`;
TRUNCATE TABLE `notifications`;
TRUNCATE TABLE `admin_actions`;
TRUNCATE TABLE `account_status_histories`;
TRUNCATE TABLE `reports`;
TRUNCATE TABLE `moderation_cases`;
TRUNCATE TABLE `saved_posts`;
TRUNCATE TABLE `post_reposts`;
TRUNCATE TABLE `comments`;
TRUNCATE TABLE `post_likes`;
TRUNCATE TABLE `post_hashtags`;
TRUNCATE TABLE `hashtags`;
TRUNCATE TABLE `post_media`;
TRUNCATE TABLE `posts`;
TRUNCATE TABLE `locations`;
TRUNCATE TABLE `user_restrictions`;
TRUNCATE TABLE `user_blocks`;
TRUNCATE TABLE `follows`;
TRUNCATE TABLE `user_daily_activities`;
TRUNCATE TABLE `password_reset_tokens`;
TRUNCATE TABLE `password_recovery_challenges`;
TRUNCATE TABLE `reauthentication_challenges`;
TRUNCATE TABLE `social_auth_challenges`;
TRUNCATE TABLE `auth_method_link_challenges`;
TRUNCATE TABLE `pending_registrations`;
TRUNCATE TABLE `refresh_tokens`;
TRUNCATE TABLE `user_auth_providers`;
TRUNCATE TABLE `user_interests`;
TRUNCATE TABLE `user_profiles`;
TRUNCATE TABLE `users`;
SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO `locations`
    (`google_place_id`, `display_name`, `formatted_address`, `latitude`, `longitude`)
VALUES
    ('demo-hcmute', 'Trường Đại học Sư phạm Kỹ thuật TP.HCM', '1 Võ Văn Ngân, Thủ Đức, TP.HCM', 10.8506000, 106.7719000),
    ('demo-uit', 'Trường Đại học Công nghệ Thông tin', 'Khu phố 6, Thủ Đức, TP.HCM', 10.8700000, 106.8030000),
    ('demo-vnuhcm', 'Đại học Quốc gia TP.HCM', 'Linh Trung, Thủ Đức, TP.HCM', 10.8799000, 106.8033000),
    ('demo-book-street', 'Đường sách Nguyễn Văn Bình', 'Quận 1, TP.HCM', 10.7802000, 106.7005000),
    ('demo-youth-house', 'Nhà Văn hóa Thanh niên', '4 Phạm Ngọc Thạch, Quận 1, TP.HCM', 10.7831000, 106.6959000),
    ('demo-library', 'Thư viện Khoa học Tổng hợp', '69 Lý Tự Trọng, Quận 1, TP.HCM', 10.7757000, 106.6990000),
    ('demo-independence', 'Dinh Độc Lập', '135 Nam Kỳ Khởi Nghĩa, Quận 1, TP.HCM', 10.7770000, 106.6953000),
    ('demo-central-park', 'Công viên Trung tâm', 'Bình Thạnh, TP.HCM', 10.7944000, 106.7218000),
    ('demo-dormitory-a', 'Ký túc xá Khu A', 'Đông Hòa, Dĩ An, Bình Dương', 10.8778000, 106.8002000),
    ('demo-dormitory-b', 'Ký túc xá Khu B', 'Đông Hòa, Dĩ An, Bình Dương', 10.8831000, 106.7827000),
    ('demo-cafe-study', 'Không gian học tập cộng đồng', 'Thủ Đức, TP.HCM', 10.8498000, 106.7711000),
    ('demo-stadium', 'Sân vận động sinh viên', 'Thủ Đức, TP.HCM', 10.8752000, 106.8011000),
    -- Địa điểm công cộng quanh đường Cao Lỗ; ID và tọa độ là dữ liệu demo gần đúng, không phải Google Place ID chính thức.
    ('demo-caolo-stu', 'Trường Đại học Công nghệ Sài Gòn', '180 Cao Lỗ, Phường 4, Quận 8, TP.HCM', 10.7387550, 106.6777880),
    ('demo-caolo-cultural-sports-center', 'Trung tâm Văn hóa - Thể thao Quận 8', '195 Cao Lỗ, Phường 4, Quận 8, TP.HCM', 10.7397000, 106.6768000),
    ('demo-caolo-dong-dieu', 'Khu Đồng Diều', 'Đường Cao Lỗ, Phường 4, Quận 8, TP.HCM', 10.7349000, 106.6760000),
    ('demo-caolo-chanh-hung-bridge', 'Cầu Chánh Hưng', 'Đường Phạm Hùng, Quận 8, TP.HCM', 10.7474000, 106.6707000),
    ('demo-caolo-pham-the-hien-market', 'Chợ Phạm Thế Hiển', 'Đường Phạm Thế Hiển, Quận 8, TP.HCM', 10.7486000, 106.6689000);

INSERT INTO `hashtags` (`normalized_name`, `display_name`) VALUES
    ('hoc tap', 'Học tập'),
    ('cong nghe', 'Công nghệ'),
    ('do an', 'Đồ án'),
    ('thuc tap', 'Thực tập'),
    ('viec lam', 'Việc làm'),
    ('an uong', 'Ăn uống'),
    ('du lich', 'Du lịch'),
    ('the thao', 'Thể thao'),
    ('am nhac', 'Âm nhạc'),
    ('nhiep anh', 'Nhiếp ảnh'),
    ('tinh nguyen', 'Tình nguyện'),
    ('doi song sinh vien', 'Đời sống sinh viên');

DROP PROCEDURE IF EXISTS `seed_website_cases`;
DELIMITER $$
CREATE PROCEDURE `seed_website_cases`()
BEGIN
    DECLARE user_no INT DEFAULT 1;
    DECLARE post_no INT DEFAULT 1;
    DECLARE media_no INT DEFAULT 0;
    DECLARE relation_no INT DEFAULT 1;
    DECLARE conversation_no INT DEFAULT 1;
    DECLARE current_post_id BIGINT UNSIGNED;
    DECLARE current_case_id BIGINT UNSIGNED;
    DECLARE current_conversation_id BIGINT UNSIGNED;
    DECLARE first_message_id BIGINT UNSIGNED;
    DECLARE second_message_id BIGINT UNSIGNED;
    DECLARE current_author_id BIGINT UNSIGNED;
    DECLARE current_user_id BIGINT UNSIGNED;
    DECLARE current_target_id BIGINT UNSIGNED;
    DECLARE current_status VARCHAR(16);
    DECLARE seed_time_value DATETIME(6);
    DECLARE seed_post_status VARCHAR(16);
    DECLARE seed_post_author_id BIGINT UNSIGNED;
    DECLARE seed_location_id BIGINT UNSIGNED;
    DECLARE seed_published_at DATETIME(6);

    START TRANSACTION;

    -- Tao dung 1.000 tai khoan, trong do 10 tai khoan cuoi dang cho onboarding.
    WHILE user_no <= 1000 DO
        SET seed_time_value = TIMESTAMP('2026-01-01 08:00:00') + INTERVAL MOD(user_no * 13, 210) DAY;
        SET current_status = IF(user_no = 6 OR MOD(user_no, 100) = 0, 'BLOCKED', 'ACTIVE');

        INSERT INTO `users` (
            `email`, `email_verified_at`, `password_hash`, `role`, `status`,
            `blocked_at`, `blocked_reason`, `first_active_at`, `last_active_at`,
            `created_at`, `updated_at`
        ) VALUES (
            CONCAT('demo.user', LPAD(user_no, 4, '0'), '@example.test'),
            seed_time_value,
            IF(user_no IN (3, 4), NULL, '$2y$10$aGQDqcH5qK7jLRKAt2hCZexSlVNmKdDODA57Njo/EWGgrlaEIJXI.'),
            IF(user_no = 1, 'ADMIN', 'USER'),
            current_status,
            IF(current_status = 'BLOCKED', seed_time_value + INTERVAL 30 DAY, NULL),
            IF(current_status = 'BLOCKED', 'Tài khoản test bị khóa để kiểm tra giao diện quản trị', NULL),
            IF(user_no <= 990, seed_time_value + INTERVAL 1 HOUR, NULL),
            IF(user_no <= 990, seed_time_value + INTERVAL MOD(user_no, 72) HOUR, NULL),
            seed_time_value,
            seed_time_value
        );

        SET current_user_id = LAST_INSERT_ID();

        INSERT INTO `user_profiles` (
            `user_id`, `username`, `display_name`, `avatar_url`, `avatar_public_id`,
            `bio`, `date_of_birth`, `school_id`, `faculty_id`, `major_id`, `entry_year`,
            `profile_completed_at`, `created_at`, `updated_at`
        ) VALUES (
            current_user_id,
            IF(user_no <= 990, CONCAT('student_', LPAD(user_no, 4, '0')), NULL),
            IF(
                user_no > 990,
                NULL,
                CASE
                    WHEN user_no = 1 THEN 'Quản trị viên Demo'
                    WHEN user_no = 2 THEN 'Nguyễn Minh Anh'
                    WHEN MOD(user_no, 5) = 0 THEN CONCAT('Trần Gia Bảo ', LPAD(user_no, 4, '0'))
                    WHEN MOD(user_no, 5) = 1 THEN CONCAT('Lê Hoài An ', LPAD(user_no, 4, '0'))
                    WHEN MOD(user_no, 5) = 2 THEN CONCAT('Phạm Khánh Linh ', LPAD(user_no, 4, '0'))
                    WHEN MOD(user_no, 5) = 3 THEN CONCAT('Võ Minh Khang ', LPAD(user_no, 4, '0'))
                    ELSE CONCAT('Đặng Thảo Vy ', LPAD(user_no, 4, '0'))
                END
            ),
            IF(user_no <= 990 AND MOD(user_no, 5) <> 0,
                CONCAT('https://i.pravatar.cc/300?img=', 1 + MOD(user_no, 70)), NULL),
            IF(user_no <= 990 AND MOD(user_no, 5) <> 0,
                CONCAT('demo/avatars/student-', LPAD(user_no, 4, '0')), NULL),
            IF(
                user_no > 990,
                NULL,
                CASE MOD(user_no, 6)
                    WHEN 0 THEN 'Sinh viên yêu thích công nghệ, lập trình và các dự án mã nguồn mở.'
                    WHEN 1 THEN 'Chia sẻ kinh nghiệm học tập, ôn thi và cuộc sống sinh viên.'
                    WHEN 2 THEN 'Quan tâm đến thiết kế, nhiếp ảnh và sáng tạo nội dung.'
                    WHEN 3 THEN 'Đang tìm cơ hội thực tập và kết nối với các bạn cùng ngành.'
                    WHEN 4 THEN 'Yêu thích thể thao, hoạt động ngoại khóa và tình nguyện.'
                    ELSE 'Khám phá địa điểm ăn uống, học nhóm và vui chơi quanh thành phố.'
                END
            ),
            IF(user_no <= 990, DATE('1998-01-01') + INTERVAL MOD(user_no * 29, 2555) DAY, NULL),
            IF(user_no <= 990, 1 + MOD(user_no - 1, 5), NULL),
            IF(
                user_no > 990,
                NULL,
                CASE MOD(user_no - 1, 5)
                    WHEN 0 THEN 1 + MOD(user_no, 6)
                    WHEN 1 THEN 7
                    WHEN 2 THEN 8
                    WHEN 3 THEN 9
                    ELSE 10
                END
            ),
            IF(
                user_no > 990,
                NULL,
                CASE MOD(user_no - 1, 5)
                    WHEN 0 THEN (2 * (1 + MOD(user_no, 6)) - 1) + MOD(user_no, 2)
                    WHEN 1 THEN 13
                    WHEN 2 THEN 14
                    WHEN 3 THEN 15
                    ELSE 16
                END
            ),
            IF(user_no <= 990, 2018 + MOD(user_no, 9), NULL),
            IF(user_no <= 990, seed_time_value + INTERVAL 30 MINUTE, NULL),
            seed_time_value,
            seed_time_value
        );

        -- Gan hai so thich khac nhau cho profile da hoan tat de demo loc/goi y sau nay.
        IF user_no <= 990 THEN
            INSERT INTO `user_interests` (`user_id`, `interest_id`) VALUES
                (current_user_id, 1 + MOD(user_no - 1, 16)),
                (current_user_id, 1 + MOD(user_no + 4, 16));
        END IF;

        SET user_no = user_no + 1;
    END WHILE;

    -- Social-only va linked-provider de test cac nhanh xac thuc.
    INSERT INTO `user_auth_providers`
        (`user_id`, `provider`, `provider_user_id`, `provider_email`, `provider_email_verified`)
    VALUES
        (3, 'GOOGLE', 'demo-google-0003', 'demo.user0003@example.test', 1),
        (4, 'FACEBOOK', 'demo-facebook-0004', 'demo.user0004@example.test', 1),
        (5, 'GOOGLE', 'demo-google-0005', 'demo.user0005@example.test', 1),
        (5, 'FACEBOOK', 'demo-facebook-0005', 'demo.user0005@example.test', 1);

    SET user_no = 10;
    WHILE user_no <= 200 DO
        INSERT IGNORE INTO `user_auth_providers`
            (`user_id`, `provider`, `provider_user_id`, `provider_email`, `provider_email_verified`)
        VALUES (
            user_no,
            IF(MOD(user_no, 2) = 0, 'GOOGLE', 'FACEBOOK'),
            CONCAT(IF(MOD(user_no, 2) = 0, 'demo-google-', 'demo-facebook-'), LPAD(user_no, 4, '0')),
            CONCAT('demo.user', LPAD(user_no, 4, '0'), '@example.test'),
            1
        );
        SET user_no = user_no + 1;
    END WHILE;

    -- Tao dung 1.000 bai viet; moi bai co tu 1 den 4 anh.
    WHILE post_no <= 1000 DO
        SET current_author_id = 2 + MOD(post_no * 37, 989);
        SET seed_time_value = TIMESTAMP('2026-08-08 10:00:00') - INTERVAL MOD(post_no * 17, 180) DAY
            - INTERVAL MOD(post_no * 43, 86400) SECOND;
        SET current_status = CASE
            WHEN MOD(post_no, 20) = 0 THEN 'DELETED'
            WHEN MOD(post_no, 20) = 1 THEN 'HIDDEN'
            ELSE 'PUBLISHED'
        END;
        -- Mười Post cố định, chia đều cho năm địa điểm để demo Nearby quanh đường Cao Lỗ, Quận 8.
        SET seed_location_id = CASE post_no
            WHEN 902 THEN (SELECT id FROM `locations` WHERE `google_place_id` = 'demo-caolo-stu')
            WHEN 903 THEN (SELECT id FROM `locations` WHERE `google_place_id` = 'demo-caolo-stu')
            WHEN 904 THEN (SELECT id FROM `locations` WHERE `google_place_id` = 'demo-caolo-cultural-sports-center')
            WHEN 905 THEN (SELECT id FROM `locations` WHERE `google_place_id` = 'demo-caolo-cultural-sports-center')
            WHEN 906 THEN (SELECT id FROM `locations` WHERE `google_place_id` = 'demo-caolo-dong-dieu')
            WHEN 907 THEN (SELECT id FROM `locations` WHERE `google_place_id` = 'demo-caolo-dong-dieu')
            WHEN 908 THEN (SELECT id FROM `locations` WHERE `google_place_id` = 'demo-caolo-chanh-hung-bridge')
            WHEN 909 THEN (SELECT id FROM `locations` WHERE `google_place_id` = 'demo-caolo-chanh-hung-bridge')
            WHEN 910 THEN (SELECT id FROM `locations` WHERE `google_place_id` = 'demo-caolo-pham-the-hien-market')
            WHEN 911 THEN (SELECT id FROM `locations` WHERE `google_place_id` = 'demo-caolo-pham-the-hien-market')
            ELSE IF(MOD(post_no, 3) = 0, 1 + MOD(post_no, 12), NULL)
        END;

        INSERT INTO `posts` (
            `author_id`, `content`, `status`, `is_edited`, `published_at`,
            `hidden_by`, `hidden_at`, `hidden_reason`, `deleted_at`,
            `created_at`, `updated_at`, `location_id`
        ) VALUES (
            current_author_id,
            CASE post_no
                WHEN 902 THEN 'Sáng nay học ở STU từ tiết một, căn tin đông nhưng vẫn kịp mua ổ bánh mì trước giờ vào lớp.'
                WHEN 903 THEN 'Có bạn nào ở gần Cao Lỗ muốn ghép nhóm làm đồ án React không? Chiều mai tụi mình gặp nhau ở STU để chia task nhé.'
                WHEN 904 THEN 'Tối nay sân ở Trung tâm Văn hóa - Thể thao Quận 8 khá thoáng. Nhóm mình còn thiếu một bạn đánh cầu lông cùng.'
                WHEN 905 THEN 'Vừa xem xong trận bóng ở khu thể thao Cao Lỗ, không khí vui hơn mình nghĩ. Cuối tuần chắc quay lại chơi tiếp.'
                WHEN 906 THEN 'Chiều đi bộ ngang khu Đồng Diều thấy trời mát, ngồi nghỉ một lúc rồi mới về làm bài. Ở gần trường có chỗ thư giãn vậy cũng tiện.'
                WHEN 907 THEN 'Mình vừa chụp được mấy tấm hoàng hôn ở khu Đồng Diều. Ánh sáng tầm năm rưỡi chiều đẹp lắm, bạn nào thích chụp ảnh thử ghé nhé.'
                WHEN 908 THEN 'Tan học chạy qua Cầu Chánh Hưng đúng giờ cao điểm nên hơi đông. Mọi người đi hướng Phạm Hùng nhớ xuất phát sớm một chút.'
                WHEN 909 THEN 'Qua Cầu Chánh Hưng buổi tối nhìn đèn hai bên kênh khá đẹp. Mình dừng gần đó mua ly nước rồi về, gió mát dễ chịu.'
                WHEN 910 THEN 'Sáng ghé Chợ Phạm Thế Hiển mua trái cây, cô bán hàng chỉ mình lựa được mấy trái ngon mà giá vẫn vừa túi tiền sinh viên.'
                WHEN 911 THEN 'Ai ở khu Cao Lỗ chưa biết ăn gì chiều nay thì thử đi một vòng gần Chợ Phạm Thế Hiển nhé, nhiều món nhỏ dễ chọn mà không quá đắt.'
                ELSE IF(
                    MOD(post_no, 25) = 0,
                    NULL,
                    CASE MOD(post_no, 10)
                        WHEN 0 THEN CONCAT('Chia sẻ tài liệu ôn tập hữu ích cho kỳ thi sắp tới. Bài số ', post_no, '.')
                        WHEN 1 THEN CONCAT('Một góc học tập yên tĩnh dành cho sinh viên hôm nay. Bài số ', post_no, '.')
                        WHEN 2 THEN CONCAT('Nhật ký làm đồ án: thêm một ngày sửa lỗi và học được nhiều điều mới. #', post_no)
                        WHEN 3 THEN CONCAT('Có bạn nào đang tìm nhóm học Spring Boot và React không? Bài số ', post_no, '.')
                        WHEN 4 THEN CONCAT('Gợi ý địa điểm ăn uống giá sinh viên, không gian thoải mái. Bài số ', post_no, '.')
                        WHEN 5 THEN CONCAT('Khoảnh khắc đáng nhớ trong hoạt động tình nguyện cuối tuần. Bài số ', post_no, '.')
                        WHEN 6 THEN CONCAT('Kinh nghiệm chuẩn bị CV và phỏng vấn thực tập cho sinh viên. Bài số ', post_no, '.')
                        WHEN 7 THEN CONCAT('Một buổi chiều thể thao cùng câu lạc bộ của trường. Bài số ', post_no, '.')
                        WHEN 8 THEN CONCAT('Ảnh chụp quanh thành phố sau giờ học. Bài số ', post_no, '.')
                        ELSE CONCAT('Cùng trao đổi cách quản lý thời gian học tập hiệu quả. Bài số ', post_no, '.')
                    END
                )
            END,
            current_status,
            IF(MOD(post_no, 9) = 0, 1, 0),
            seed_time_value,
            IF(current_status = 'HIDDEN', 1, NULL),
            IF(current_status = 'HIDDEN', seed_time_value + INTERVAL 2 HOUR, NULL),
            IF(current_status = 'HIDDEN', 'Nội dung demo được ẩn để kiểm tra màn hình quản trị', NULL),
            IF(current_status = 'DELETED', seed_time_value + INTERVAL 3 HOUR, NULL),
            seed_time_value,
            IF(MOD(post_no, 9) = 0, seed_time_value + INTERVAL 10 MINUTE, seed_time_value),
            seed_location_id
        );

        SET current_post_id = LAST_INSERT_ID();
        SET media_no = 0;
        WHILE media_no < 1 + MOD(post_no, 4) DO
            INSERT INTO `post_media` (
                `post_id`, `media_url`, `storage_public_id`, `media_type`, `mime_type`,
                `file_size_bytes`, `width_px`, `height_px`, `duration_seconds`,
                `thumbnail_url`, `display_order`, `created_at`
            ) VALUES (
                current_post_id,
                CONCAT('https://picsum.photos/seed/unishare-post-', LPAD(post_no, 4, '0'), '-', media_no, '/1200/800'),
                CONCAT('demo/posts/post-', LPAD(post_no, 4, '0'), '-image-', media_no),
                'IMAGE',
                'image/jpeg',
                180000 + MOD(post_no * 7919 + media_no * 3571, 2200000),
                1200,
                800,
                NULL,
                NULL,
                media_no,
                seed_time_value
            );
            SET media_no = media_no + 1;
        END WHILE;

        IF MOD(post_no, 5) <> 0 THEN
            INSERT INTO `post_hashtags` (`post_id`, `hashtag_id`, `created_at`)
            VALUES (current_post_id, 1 + MOD(post_no, 12), seed_time_value);
        END IF;

        SET post_no = post_no + 1;
    END WHILE;

    -- Follow phuc vu Following Feed va thong ke profile.
    SET relation_no = 1;
    WHILE relation_no <= 6000 DO
        SET current_user_id = 2 + MOD(FLOOR((relation_no - 1) / 6), 989);
        SET current_target_id = 2 + MOD(
            current_user_id - 2 + (1 + MOD(relation_no - 1, 6)) * 37,
            989
        );
        IF current_user_id <> current_target_id THEN
            INSERT IGNORE INTO `follows` (`follower_id`, `following_id`, `created_at`)
            VALUES (current_user_id, current_target_id, TIMESTAMP('2026-03-01 09:00:00') + INTERVAL MOD(relation_no, 150) DAY);
        END IF;
        SET relation_no = relation_no + 1;
    END WHILE;

    -- Like chi gan vao bai PUBLISHED va khong tu like bai cua minh.
    SET relation_no = 1;
    WHILE relation_no <= 12000 DO
        SET current_user_id = 2 + MOD(relation_no * 23, 989);
        SET current_post_id = 1 + MOD(relation_no * 47, 1000);
        SELECT `status`, `author_id`, `published_at`
        INTO seed_post_status, seed_post_author_id, seed_published_at
        FROM `posts`
        WHERE `id` = current_post_id;
        IF seed_post_status = 'PUBLISHED' AND seed_post_author_id <> current_user_id THEN
            INSERT IGNORE INTO `post_likes` (`user_id`, `post_id`, `created_at`)
            VALUES (current_user_id, current_post_id, seed_published_at + INTERVAL 1 DAY);
        END IF;
        SET relation_no = relation_no + 1;
    END WHILE;

    -- Comment gom ca comment thuong va comment da xoa de test state UI.
    SET relation_no = 1;
    WHILE relation_no <= 3000 DO
        SET current_user_id = 2 + MOD(relation_no * 29, 989);
        SET current_post_id = 1 + MOD(relation_no * 41, 1000);
        SELECT `status`, `published_at`
        INTO seed_post_status, seed_published_at
        FROM `posts`
        WHERE `id` = current_post_id;
        IF seed_post_status = 'PUBLISHED' THEN
            INSERT INTO `comments` (
                `post_id`, `user_id`, `parent_comment_id`, `content`, `status`,
                `deleted_at`, `created_at`, `updated_at`
            ) VALUES (
                current_post_id,
                current_user_id,
                NULL,
                CASE MOD(relation_no, 5)
                    WHEN 0 THEN 'Cảm ơn bạn đã chia sẻ thông tin hữu ích!'
                    WHEN 1 THEN 'Mình cũng đang quan tâm chủ đề này.'
                    WHEN 2 THEN 'Hình ảnh đẹp và nội dung rất gần gũi.'
                    WHEN 3 THEN 'Bạn có thể chia sẻ thêm tài liệu không?'
                    ELSE 'Chúc bạn có một ngày học tập hiệu quả.'
                END,
                IF(MOD(relation_no, 20) = 0, 'DELETED', 'PUBLISHED'),
                IF(MOD(relation_no, 20) = 0, seed_published_at + INTERVAL 2 DAY, NULL),
                seed_published_at + INTERVAL 1 DAY,
                seed_published_at + INTERVAL 1 DAY
            );
        END IF;
        SET relation_no = relation_no + 1;
    END WHILE;

    SET relation_no = 1;
    WHILE relation_no <= 4000 DO
        SET current_user_id = 2 + MOD(relation_no * 19, 989);
        SET current_post_id = 1 + MOD(relation_no * 53, 1000);
        INSERT IGNORE INTO `saved_posts` (`user_id`, `post_id`, `created_at`)
        SELECT current_user_id, p.id, p.published_at + INTERVAL 3 DAY
        FROM `posts` p
        WHERE p.id = current_post_id AND p.status = 'PUBLISHED';
        SET relation_no = relation_no + 1;
    END WHILE;

    SET relation_no = 1;
    WHILE relation_no <= 3000 DO
        SET current_user_id = 2 + MOD(relation_no * 11, 989);
        SET current_post_id = 1 + MOD(relation_no * 59, 1000);
        SELECT `status`, `author_id`, `published_at`
        INTO seed_post_status, seed_post_author_id, seed_published_at
        FROM `posts`
        WHERE `id` = current_post_id;
        IF seed_post_status = 'PUBLISHED' AND seed_post_author_id <> current_user_id THEN
            INSERT IGNORE INTO `post_reposts` (`user_id`, `post_id`, `created_at`)
            VALUES (current_user_id, current_post_id, seed_published_at + INTERVAL 4 DAY);
        END IF;
        SET relation_no = relation_no + 1;
    END WHILE;

    -- Quan he block/restrict co huong de test an profile, post va interaction.
    SET relation_no = 1;
    WHILE relation_no <= 30 DO
        INSERT IGNORE INTO `user_blocks` (`blocker_id`, `blocked_id`, `created_at`)
        VALUES (1 + relation_no, 101 + relation_no, TIMESTAMP('2026-07-01 08:00:00') + INTERVAL relation_no DAY);
        INSERT IGNORE INTO `user_restrictions` (`restrictor_id`, `restricted_id`, `created_at`)
        VALUES (201 + relation_no, 301 + relation_no, TIMESTAMP('2026-07-01 09:00:00') + INTERVAL relation_no DAY);
        SET relation_no = relation_no + 1;
    END WHILE;

    -- Moderation Case phu ca OPEN, khong vi pham va da xu ly co hanh dong.
    SET relation_no = 1;
    WHILE relation_no <= 30 DO
        SET current_post_id = relation_no * 20 + 2;
        SET seed_time_value = TIMESTAMP('2026-07-01 10:00:00') + INTERVAL relation_no DAY;
        INSERT INTO `moderation_cases` (
            `post_id`, `status`, `report_count`, `resolved_by`, `resolution_note`,
            `first_reported_at`, `latest_reported_at`, `resolved_at`, `created_at`, `updated_at`
        ) VALUES (
            current_post_id,
            CASE MOD(relation_no, 3)
                WHEN 0 THEN 'OPEN'
                WHEN 1 THEN 'RESOLVED_NO_VIOLATION'
                ELSE 'RESOLVED_ACTION_TAKEN'
            END,
            1,
            IF(MOD(relation_no, 3) = 0, NULL, 1),
            IF(MOD(relation_no, 3) = 0, NULL, 'Ghi chú xử lý moderation dành cho dữ liệu demo'),
            seed_time_value,
            seed_time_value,
            IF(MOD(relation_no, 3) = 0, NULL, seed_time_value + INTERVAL 1 DAY),
            seed_time_value,
            seed_time_value
        );
        SET current_case_id = LAST_INSERT_ID();
        SET current_user_id = 700 + relation_no;

        INSERT INTO `reports` (
            `reporter_id`, `post_id`, `moderation_case_id`, `reason`, `description`,
            `status`, `resolved_by`, `resolved_at`, `resolution_note`,
            `post_content_snapshot`, `post_media_snapshot`, `created_at`, `updated_at`
        )
        SELECT
            current_user_id,
            p.id,
            current_case_id,
            CASE MOD(relation_no, 7)
                WHEN 0 THEN 'SPAM'
                WHEN 1 THEN 'HARASSMENT'
                WHEN 2 THEN 'HARMFUL_CONTENT'
                WHEN 3 THEN 'VIOLENCE'
                WHEN 4 THEN 'MISINFORMATION'
                WHEN 5 THEN 'INAPPROPRIATE'
                ELSE 'OTHER'
            END,
            'Báo cáo demo để kiểm tra luồng quản trị nội dung.',
            CASE MOD(relation_no, 3)
                WHEN 0 THEN 'PENDING'
                WHEN 1 THEN 'REJECTED'
                ELSE 'RESOLVED'
            END,
            IF(MOD(relation_no, 3) = 0, NULL, 1),
            IF(MOD(relation_no, 3) = 0, NULL, seed_time_value + INTERVAL 1 DAY),
            IF(MOD(relation_no, 3) = 0, NULL, 'Kết quả xử lý report demo'),
            p.content,
            JSON_ARRAY(JSON_OBJECT(
                'mediaUrl', CONCAT('https://picsum.photos/seed/unishare-post-', LPAD(p.id, 4, '0'), '-0/1200/800'),
                'mediaType', 'IMAGE'
            )),
            seed_time_value,
            seed_time_value
        FROM `posts` p
        WHERE p.id = current_post_id;

        SET relation_no = relation_no + 1;
    END WHILE;

    -- Lich su va notification tao du lieu cho dashboard/notification center.
    INSERT INTO `account_status_histories`
        (`user_id`, `old_status`, `new_status`, `changed_by`, `reason`, `created_at`)
    SELECT
        u.id, 'ACTIVE', 'BLOCKED', 1,
        'Khóa tài khoản demo để kiểm tra lịch sử quản trị',
        u.blocked_at
    FROM `users` u
    WHERE u.status = 'BLOCKED';

    INSERT INTO `admin_actions`
        (`admin_id`, `action_type`, `target_type`, `target_id`, `note`, `old_data`, `new_data`, `created_at`)
    SELECT
        1, 'BLOCK_USER', 'USER', u.id,
        'Thao tác quản trị demo',
        JSON_OBJECT('status', 'ACTIVE'),
        JSON_OBJECT('status', 'BLOCKED'),
        u.blocked_at
    FROM `users` u
    WHERE u.status = 'BLOCKED';

    SET relation_no = 1;
    WHILE relation_no <= 300 DO
        INSERT INTO `notifications` (
            `recipient_id`, `actor_id`, `type`, `post_id`, `comment_id`,
            `report_id`, `read_at`, `deleted_at`, `created_at`, `updated_at`
        ) VALUES (
            2 + MOD(relation_no, 100),
            102 + MOD(relation_no * 7, 500),
            CASE MOD(relation_no, 5)
                WHEN 0 THEN 'FOLLOW'
                WHEN 1 THEN 'POST_LIKE'
                WHEN 2 THEN 'POST_COMMENT'
                WHEN 3 THEN 'POST_REPOST'
                ELSE 'COMMENT_REPLY'
            END,
            IF(MOD(relation_no, 5) = 0, NULL, 2 + MOD(relation_no * 13, 998)),
            NULL,
            NULL,
            IF(MOD(relation_no, 3) = 0, TIMESTAMP('2026-08-01 08:00:00') + INTERVAL relation_no MINUTE, NULL),
            NULL,
            TIMESTAMP('2026-08-01 07:00:00') + INTERVAL relation_no MINUTE,
            TIMESTAMP('2026-08-01 07:00:00') + INTERVAL relation_no MINUTE
        );
        SET relation_no = relation_no + 1;
    END WHILE;

    -- 50 hoi thoai, moi hoi thoai co hai tin nhan de man hinh Inbox co du lieu.
    SET conversation_no = 1;
    WHILE conversation_no <= 50 DO
        SET current_target_id = 2 + conversation_no;
        SET seed_time_value = TIMESTAMP('2026-08-07 08:00:00') + INTERVAL conversation_no MINUTE;

        INSERT INTO `conversations`
            (`participant_low_id`, `participant_high_id`, `created_at`, `updated_at`)
        VALUES (2, current_target_id, seed_time_value, seed_time_value);
        SET current_conversation_id = LAST_INSERT_ID();

        INSERT INTO `conversation_members`
            (`conversation_id`, `user_id`, `created_at`, `updated_at`)
        VALUES
            (current_conversation_id, 2, seed_time_value, seed_time_value),
            (current_conversation_id, current_target_id, seed_time_value, seed_time_value);

        INSERT INTO `messages` (
            `conversation_id`, `sender_id`, `client_message_id`, `type`,
            `content`, `payload_fingerprint`, `created_at`, `updated_at`
        ) VALUES (
            current_conversation_id,
            current_target_id,
            UUID(),
            'TEXT',
            CONCAT('Xin chào, mình muốn trao đổi về bài viết số ', conversation_no, '.'),
            SHA2(CONCAT('demo-message-a-', conversation_no), 256),
            seed_time_value,
            seed_time_value
        );
        SET first_message_id = LAST_INSERT_ID();

        INSERT INTO `messages` (
            `conversation_id`, `sender_id`, `client_message_id`, `type`,
            `content`, `payload_fingerprint`, `created_at`, `updated_at`
        ) VALUES (
            current_conversation_id,
            2,
            UUID(),
            'TEXT',
            'Chào bạn, mình sẵn sàng trao đổi thêm nhé!',
            SHA2(CONCAT('demo-message-b-', conversation_no), 256),
            seed_time_value + INTERVAL 1 MINUTE,
            seed_time_value + INTERVAL 1 MINUTE
        );
        SET second_message_id = LAST_INSERT_ID();

        UPDATE `conversations`
        SET `last_message_id` = second_message_id,
            `last_message_at` = seed_time_value + INTERVAL 1 MINUTE,
            `updated_at` = seed_time_value + INTERVAL 1 MINUTE
        WHERE `id` = current_conversation_id;

        UPDATE `conversation_members`
        SET `last_read_message_id` = IF(`user_id` = 2, first_message_id, second_message_id),
            `last_read_at` = seed_time_value + INTERVAL 1 MINUTE
        WHERE `conversation_id` = current_conversation_id;

        SET conversation_no = conversation_no + 1;
    END WHILE;

    -- Du lieu activity phuc vu dashboard analytics.
    SET user_no = 1;
    WHILE user_no <= 500 DO
        INSERT INTO `user_daily_activities`
            (`user_id`, `activity_date`, `first_active_at`, `last_active_at`, `activity_count`)
        VALUES (
            user_no,
            DATE('2026-08-01') + INTERVAL MOD(user_no, 7) DAY,
            TIMESTAMP('2026-08-01 07:00:00') + INTERVAL MOD(user_no, 7) DAY,
            TIMESTAMP('2026-08-01 09:00:00') + INTERVAL MOD(user_no, 7) DAY,
            1 + MOD(user_no, 20)
        );
        SET user_no = user_no + 1;
    END WHILE;

    COMMIT;
END$$
DELIMITER ;

CALL `seed_website_cases`();
DROP PROCEDURE `seed_website_cases`;

-- Ket qua mong doi: 1.000 users, 1.000 profiles, 1.000 posts va 2.500 post media.
SELECT
    (SELECT COUNT(*) FROM `users`) AS users_count,
    (SELECT COUNT(*) FROM `user_profiles`) AS profiles_count,
    (SELECT COUNT(*) FROM `user_profiles` WHERE `username` IS NOT NULL) AS completed_usernames,
    (SELECT COUNT(*) FROM `user_profiles` WHERE `profile_completed_at` IS NULL) AS onboarding_profiles,
    (SELECT COUNT(*) FROM `user_profiles` WHERE `school_id` IS NOT NULL) AS academic_profiles,
    (SELECT COUNT(*) FROM `user_interests`) AS user_interests_count,
    (SELECT COUNT(*) FROM `posts`) AS posts_count,
    (SELECT COUNT(*) FROM `post_media`) AS post_media_count;

-- Moi dong vi pham phai tra ve 0; giu verify cung seed de thu muc database gon hon.
SELECT 'invalid_demo_counts' AS check_name, COUNT(*) AS violations
FROM (SELECT 1) AS expected
WHERE (SELECT COUNT(*) FROM `users`) <> 1000
   OR (SELECT COUNT(*) FROM `user_profiles`) <> 1000
   OR (SELECT COUNT(*) FROM `posts`) <> 1000
   OR (SELECT COUNT(*) FROM `user_profiles` WHERE `profile_completed_at` IS NULL) <> 10
UNION ALL
SELECT 'invalid_academic_hierarchy', COUNT(*)
FROM `user_profiles` profile
LEFT JOIN `faculties` faculty ON faculty.id = profile.faculty_id
LEFT JOIN `majors` major ON major.id = profile.major_id
WHERE profile.school_id IS NOT NULL
  AND (faculty.school_id <> profile.school_id OR major.faculty_id <> profile.faculty_id)
UNION ALL
SELECT 'counter_mismatch', COUNT(*)
FROM `posts` post
WHERE post.like_count <> (SELECT COUNT(*) FROM `post_likes` item WHERE item.post_id = post.id)
   OR post.comment_count <> (SELECT COUNT(*) FROM `comments` item WHERE item.post_id = post.id AND item.status <> 'DELETED')
   OR post.repost_count <> (SELECT COUNT(*) FROM `post_reposts` item WHERE item.post_id = post.id)
UNION ALL
SELECT 'invalid_cao_lo_nearby_seed', COUNT(*)
FROM (SELECT 1) AS expected
WHERE (SELECT COUNT(*) FROM `locations` WHERE `google_place_id` LIKE 'demo-caolo-%') <> 5
   OR (SELECT COUNT(*)
       FROM `posts` post
       JOIN `locations` location ON location.id = post.location_id
       WHERE location.google_place_id LIKE 'demo-caolo-%'
         AND post.id BETWEEN 902 AND 911
         AND post.status = 'PUBLISHED') <> 10;

SELECT `status`, COUNT(*) AS total
FROM `posts`
GROUP BY `status`
ORDER BY `status`;

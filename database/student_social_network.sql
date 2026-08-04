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
  `action_type` enum('BLOCK_USER','UNBLOCK_USER','UPDATE_USER_PROFILE','HIDE_POST','RESTORE_POST','RESOLVE_REPORT','REJECT_REPORT','RESOLVE_MODERATION_CASE','REJECT_MODERATION_CASE') NOT NULL,
  `target_type` enum('USER','POST','REPORT','MODERATION_CASE') NOT NULL,
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
  CONSTRAINT `chk_reauth_scope` CHECK ((`scope` in (_utf8mb4'UNLINK_AUTH_METHOD'))),
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
  `type` enum('FOLLOW','POST_LIKE','POST_COMMENT','COMMENT_REPLY','POST_REPOST','REPORT_RESOLVED','REPORT_REJECTED','POST_HIDDEN_BY_ADMIN','POST_RESTORED_BY_ADMIN','ACCOUNT_BLOCKED','ACCOUNT_UNBLOCKED') NOT NULL,
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
  CONSTRAINT `chk_user_profiles_completion_consistency` CHECK (((`profile_completed_at` is null) or ((`display_name` is not null) and (`date_of_birth` is not null)))),
  CONSTRAINT `chk_user_profiles_completion_requires_birth_date` CHECK (((`profile_completed_at` is null) or (`date_of_birth` is not null))),
  CONSTRAINT `chk_user_profiles_display_name_not_blank` CHECK (((`display_name` is null) or (char_length(trim(`display_name`)) > 0)))
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
  `type` enum('TEXT','IMAGE') NOT NULL,
  `content` varchar(2000) DEFAULT NULL,
  `payload_fingerprint` char(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_messages_sender_client_message` (`sender_id`,`client_message_id`),
  KEY `idx_messages_conversation_cursor` (`conversation_id`,`id` DESC),
  CONSTRAINT `fk_messages_conversation` FOREIGN KEY (`conversation_id`) REFERENCES `conversations` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_messages_sender_member` FOREIGN KEY (`conversation_id`,`sender_id`) REFERENCES `conversation_members` (`conversation_id`,`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `chk_messages_payload_shape` CHECK (((`type` = 'TEXT' AND `content` IS NOT NULL AND char_length(trim(`content`)) > 0) OR `type` = 'IMAGE')),
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


-- DEV/DEMO SEED DATA

-- Seed Auth DEV/DEMO sau khi rebuild database.
-- Không chạy trên môi trường có dữ liệu cần bảo tồn.
-- File cố ý không idempotent: chạy lần hai phải lỗi unique/primary key thay vì tạo Admin trùng.
-- Khi dùng file này phải đặt APP_BOOTSTRAP_ADMIN_ENABLED=false.

SET NAMES utf8mb4;
SET time_zone = '+00:00';

START TRANSACTION;

-- BCrypt cost 10 cho mật khẩu demo DEV: Demo@12345
-- Không sử dụng credential này ngoài môi trường local/demo.
SET @demo_password_hash = '$2a$10$OZDWQo86Ao3A2cbcPxTzUOhaV4At2WuPcQMXK6xSRCfdVVnzSsXAy';
SET @seed_time = '2026-07-19 03:00:00.000000';

INSERT INTO users (
    id,
    email,
    email_verified_at,
    password_hash,
    role,
    status,
    blocked_at,
    blocked_reason,
    created_at,
    updated_at
) VALUES
    (1001, 'admin.demo@example.test', @seed_time, @demo_password_hash, 'ADMIN', 'ACTIVE', NULL, NULL, @seed_time, @seed_time),
    (1002, 'local.email@example.test', @seed_time, @demo_password_hash, 'USER', 'ACTIVE', NULL, NULL, @seed_time, @seed_time),
    (1003, 'local.student@example.test', @seed_time, @demo_password_hash, 'USER', 'ACTIVE', NULL, NULL, @seed_time, @seed_time),
    (1004, NULL, NULL, NULL, 'USER', 'ACTIVE', NULL, NULL, @seed_time, @seed_time),
    (1005, NULL, NULL, NULL, 'USER', 'ACTIVE', NULL, NULL, @seed_time, @seed_time),
    (1006, 'local.google@example.test', @seed_time, @demo_password_hash, 'USER', 'ACTIVE', NULL, NULL, @seed_time, @seed_time),
    (1007, 'blocked.demo@example.test', @seed_time, @demo_password_hash, 'USER', 'BLOCKED', @seed_time, 'Tài khoản demo dùng để kiểm thử BLOCKED', @seed_time, @seed_time),
    (1008, 'onboarding.pending@example.test', @seed_time, @demo_password_hash, 'USER', 'ACTIVE', NULL, NULL, @seed_time, @seed_time);

INSERT INTO user_profiles (
    user_id,
    display_name,
    avatar_url,
    avatar_public_id,
    bio,
    date_of_birth,
    profile_completed_at,
    created_at,
    updated_at
) VALUES
    (1001, 'Admin Demo', NULL, NULL, 'Tài khoản quản trị demo', '1995-01-01', @seed_time, @seed_time, @seed_time),
    (1002, 'Local Email Demo', NULL, NULL, NULL, '2000-02-02', @seed_time, @seed_time, @seed_time),
    (1003, 'Local Student Demo', NULL, NULL, NULL, '2000-03-03', @seed_time, @seed_time, @seed_time),
    (1004, 'Google Only Demo', NULL, NULL, NULL, '2000-04-04', @seed_time, @seed_time, @seed_time),
    (1005, 'Facebook Only Demo', NULL, NULL, NULL, '2000-05-05', @seed_time, @seed_time, @seed_time),
    (1006, 'Local Google Demo', NULL, NULL, NULL, '2000-06-06', @seed_time, @seed_time, @seed_time),
    (1007, 'Blocked Demo', NULL, NULL, NULL, '2000-07-07', @seed_time, @seed_time, @seed_time),
    (1008, NULL, NULL, NULL, NULL, NULL, NULL, @seed_time, @seed_time);

INSERT INTO user_auth_providers (
    id,
    user_id,
    provider,
    provider_user_id,
    provider_email,
    provider_email_verified,
    created_at,
    updated_at
) VALUES
    (2001, 1004, 'GOOGLE', 'demo-google-only-1004', 'google.only@example.test', 1, @seed_time, @seed_time),
    (2002, 1005, 'FACEBOOK', 'demo-facebook-only-1005', NULL, NULL, @seed_time, @seed_time),
    (2003, 1006, 'GOOGLE', 'demo-local-google-1006', 'local.google@example.test', 1, @seed_time, @seed_time);

-- Hồ sơ demo có avatar công khai để các màn hình Feed, Search, Follow và Comment hiển thị sát dữ liệu thật.
UPDATE user_profiles SET
    avatar_url = CASE user_id
        WHEN 1001 THEN 'https://i.pravatar.cc/300?img=12'
        WHEN 1002 THEN 'https://i.pravatar.cc/300?img=32'
        WHEN 1003 THEN 'https://i.pravatar.cc/300?img=47'
        WHEN 1004 THEN 'https://i.pravatar.cc/300?img=15'
        WHEN 1005 THEN 'https://i.pravatar.cc/300?img=25'
        WHEN 1006 THEN 'https://i.pravatar.cc/300?img=53'
        ELSE avatar_url
    END,
    avatar_public_id = CASE WHEN user_id BETWEEN 1001 AND 1006 THEN CONCAT('demo/avatar-', user_id) ELSE avatar_public_id END,
    bio = CASE user_id
        WHEN 1002 THEN 'Sinh viên yêu thích nhiếp ảnh và các hoạt động trong trường.'
        WHEN 1003 THEN 'Chia sẻ tài liệu, kinh nghiệm học tập và cuộc sống sinh viên.'
        WHEN 1004 THEN 'Thích công nghệ, cà phê và khám phá những góc đẹp trong khuôn viên.'
        WHEN 1005 THEN 'Quan tâm cơ hội thực tập, kỹ năng nghề nghiệp và thiết kế.'
        WHEN 1006 THEN 'Lập trình viên tập sự, đang học Spring Boot và React.'
        ELSE bio
    END
WHERE user_id BETWEEN 1001 AND 1006;

INSERT INTO follows (follower_id, following_id, created_at) VALUES
    (1002, 1003, '2026-07-20 01:00:00.000000'),
    (1002, 1004, '2026-07-20 01:05:00.000000'),
    (1003, 1002, '2026-07-20 01:10:00.000000'),
    (1003, 1006, '2026-07-20 01:15:00.000000'),
    (1004, 1002, '2026-07-20 01:20:00.000000'),
    (1005, 1002, '2026-07-20 01:25:00.000000'),
    (1006, 1003, '2026-07-20 01:30:00.000000');

INSERT INTO posts (
    id, author_id, content, status, is_edited, like_count, comment_count,
    published_at, created_at, updated_at
) VALUES
    (3001, 1002, 'Ngày đầu quay lại trường, khuôn viên vẫn luôn là nơi mang lại nhiều năng lượng nhất.', 'PUBLISHED', 0, 0, 0, '2026-07-20 02:00:00.000000', '2026-07-20 02:00:00.000000', '2026-07-20 02:00:00.000000'),
    (3002, 1003, 'Mình đang tìm thêm hai bạn lập nhóm ôn môn Cơ sở dữ liệu. Nhóm học vào tối thứ ba và thứ năm.', 'PUBLISHED', 0, 0, 0, '2026-07-20 04:30:00.000000', '2026-07-20 04:30:00.000000', '2026-07-20 04:30:00.000000'),
    (3003, 1004, 'Một góc yên tĩnh trong thư viện rất phù hợp để hoàn thành đồ án cuối kỳ.', 'PUBLISHED', 0, 0, 0, '2026-07-21 01:15:00.000000', '2026-07-21 01:15:00.000000', '2026-07-21 01:15:00.000000'),
    (3004, 1005, 'Có bạn nào đang chuẩn bị CV xin thực tập không? Mình vừa tổng hợp một số lưu ý từ buổi workshop nghề nghiệp.', 'PUBLISHED', 1, 0, 0, '2026-07-21 03:00:00.000000', '2026-07-21 02:45:00.000000', '2026-07-21 03:00:00.000000'),
    (3005, 1006, 'Cuối cùng API tạo bài viết bằng Spring Boot và giao diện React cũng đã kết nối thành công!', 'PUBLISHED', 0, 0, 0, '2026-07-21 06:20:00.000000', '2026-07-21 06:20:00.000000', '2026-07-21 06:20:00.000000'),
    (3006, 1002, 'Gợi ý một quán cà phê học bài gần trường: nhiều ổ cắm, khá yên tĩnh và có ánh sáng đẹp.', 'PUBLISHED', 0, 0, 0, '2026-07-22 01:00:00.000000', '2026-07-22 01:00:00.000000', '2026-07-22 01:00:00.000000'),
    (3007, 1003, 'Câu lạc bộ tình nguyện đang tuyển thành viên cho chương trình Chào tân sinh viên.', 'PUBLISHED', 0, 0, 0, '2026-07-22 03:30:00.000000', '2026-07-22 03:30:00.000000', '2026-07-22 03:30:00.000000'),
    (3008, 1004, NULL, 'PUBLISHED', 0, 0, 0, '2026-07-22 05:00:00.000000', '2026-07-22 05:00:00.000000', '2026-07-22 05:00:00.000000'),
    (3009, 1002, 'Một buổi chiều hoàn thành đồ án ở thư viện. Góc học tập này vừa yên tĩnh vừa có nhiều ánh sáng tự nhiên.', 'PUBLISHED', 0, 0, 0, '2026-07-22 07:15:00.000000', '2026-07-22 07:15:00.000000', '2026-07-22 07:15:00.000000'),
    (3010, 1003, 'Một đoạn video ngắn ghi lại hoạt động ngoại khóa cuối tuần của câu lạc bộ.', 'PUBLISHED', 0, 0, 0, '2026-07-22 09:20:00.000000', '2026-07-22 09:20:00.000000', '2026-07-22 09:20:00.000000'),
    (3011, 1004, 'Campus tour phiên bản buổi sáng: sân trường, giảng đường và khu tự học.', 'PUBLISHED', 0, 0, 0, '2026-07-23 01:10:00.000000', '2026-07-23 01:10:00.000000', '2026-07-23 01:10:00.000000'),
    (3012, 1005, 'Workshop kỹ năng phỏng vấn hôm nay có rất nhiều chia sẻ thực tế từ anh chị cựu sinh viên.', 'PUBLISHED', 0, 0, 0, '2026-07-23 02:35:00.000000', '2026-07-23 02:35:00.000000', '2026-07-23 02:35:00.000000'),
    (3013, 1006, 'Góc làm việc của nhóm trong buổi chạy nước rút cuối cùng trước ngày demo sản phẩm.', 'PUBLISHED', 0, 0, 0, '2026-07-23 04:00:00.000000', '2026-07-23 04:00:00.000000', '2026-07-23 04:00:00.000000'),
    (3014, 1002, 'Video demo nhanh giao diện mới sau khi nhóm hoàn thành kết nối Feed với Backend.', 'PUBLISHED', 0, 0, 0, '2026-07-23 06:25:00.000000', '2026-07-23 06:25:00.000000', '2026-07-23 06:25:00.000000'),
    (3015, 1003, 'Một vài tài liệu mình dùng để ôn tập môn Cơ sở dữ liệu. Hy vọng hữu ích cho các bạn.', 'PUBLISHED', 0, 0, 0, '2026-07-23 08:40:00.000000', '2026-07-23 08:40:00.000000', '2026-07-23 08:40:00.000000'),
    (3016, 1004, 'Buổi sinh hoạt câu lạc bộ đầu tiên của học kỳ mới đã có rất nhiều thành viên tham gia.', 'PUBLISHED', 0, 0, 0, '2026-07-24 00:30:00.000000', '2026-07-24 00:30:00.000000', '2026-07-24 00:30:00.000000'),
    (3017, 1005, 'Hai góc nhỏ rất phù hợp để học nhóm và trao đổi bài sau giờ học.', 'PUBLISHED', 0, 0, 0, '2026-07-24 02:05:00.000000', '2026-07-24 02:05:00.000000', '2026-07-24 02:05:00.000000'),
    (3018, 1006, 'Chia sẻ nhanh checklist chuẩn bị CV và portfolio trước khi ứng tuyển thực tập.', 'PUBLISHED', 0, 0, 0, '2026-07-24 03:45:00.000000', '2026-07-24 03:45:00.000000', '2026-07-24 03:45:00.000000'),
    (3019, 1002, 'Hôm nay mọi người đang tập trung hoàn thành môn học hay chuẩn bị cho kỳ thực tập?', 'PUBLISHED', 0, 0, 0, '2026-07-24 05:10:00.000000', '2026-07-24 05:10:00.000000', '2026-07-24 05:10:00.000000'),
    (3020, 1003, 'Tổng hợp một vài khoảnh khắc trong ngày hội sinh viên tại trường.', 'PUBLISHED', 0, 0, 0, '2026-07-24 06:50:00.000000', '2026-07-24 06:50:00.000000', '2026-07-24 06:50:00.000000'),
    (3021, 1004, 'Bắt đầu ngày mới bằng một buổi đọc sách ở khuôn viên trường.', 'PUBLISHED', 0, 0, 0, '2026-07-24 07:20:00.000000', '2026-07-24 07:20:00.000000', '2026-07-24 07:20:00.000000'),
    (3022, 1005, 'Video ngắn về không khí chuẩn bị trước giờ thuyết trình đồ án.', 'PUBLISHED', 0, 0, 0, '2026-07-24 07:40:00.000000', '2026-07-24 07:40:00.000000', '2026-07-24 07:40:00.000000'),
    (3023, 1006, 'Buổi mentoring đầu tiên giúp mình xác định rõ hơn lộ trình nghề nghiệp.', 'PUBLISHED', 0, 0, 0, '2026-07-24 08:00:00.000000', '2026-07-24 08:00:00.000000', '2026-07-24 08:00:00.000000'),
    (3024, 1002, 'Một vòng tham quan phòng thực hành và không gian học tập mới.', 'PUBLISHED', 0, 0, 0, '2026-07-24 08:20:00.000000', '2026-07-24 08:20:00.000000', '2026-07-24 08:20:00.000000'),
    (3025, 1003, 'Mọi người thường dùng công cụ nào để quản lý tiến độ bài tập nhóm?', 'PUBLISHED', 0, 0, 0, '2026-07-24 08:40:00.000000', '2026-07-24 08:40:00.000000', '2026-07-24 08:40:00.000000'),
    (3026, 1004, 'Tổng kết buổi học kỹ năng làm việc nhóm với rất nhiều hoạt động thú vị.', 'PUBLISHED', 0, 0, 0, '2026-07-24 09:00:00.000000', '2026-07-24 09:00:00.000000', '2026-07-24 09:00:00.000000'),
    (3027, 1005, 'Ba khoảnh khắc đáng nhớ trong chuyến tham quan doanh nghiệp tuần này.', 'PUBLISHED', 0, 0, 0, '2026-07-24 09:20:00.000000', '2026-07-24 09:20:00.000000', '2026-07-24 09:20:00.000000'),
    (3028, 1006, 'Một phút thư giãn sau khi hoàn thành sprint cuối cùng của dự án.', 'PUBLISHED', 0, 0, 0, '2026-07-24 09:40:00.000000', '2026-07-24 09:40:00.000000', '2026-07-24 09:40:00.000000'),
    (3029, 1002, 'Ghi chú nhanh từ buổi chia sẻ kinh nghiệm tìm nơi thực tập phù hợp.', 'PUBLISHED', 0, 0, 0, '2026-07-24 10:00:00.000000', '2026-07-24 10:00:00.000000', '2026-07-24 10:00:00.000000'),
    (3030, 1003, 'Góc máy tính quen thuộc trong những ngày chạy deadline.', 'PUBLISHED', 0, 0, 0, '2026-07-24 10:20:00.000000', '2026-07-24 10:20:00.000000', '2026-07-24 10:20:00.000000'),
    (3031, 1004, 'Một buổi học nhóm hiệu quả cần không gian tốt và mục tiêu thật rõ ràng.', 'PUBLISHED', 0, 0, 0, '2026-07-24 10:40:00.000000', '2026-07-24 10:40:00.000000', '2026-07-24 10:40:00.000000'),
    (3032, 1005, 'Có ai đang tìm đồng đội tham gia cuộc thi lập trình sắp tới không?', 'PUBLISHED', 0, 0, 0, '2026-07-24 11:00:00.000000', '2026-07-24 11:00:00.000000', '2026-07-24 11:00:00.000000'),
    (3033, 1006, 'Bảng kế hoạch cho tuần mới đã sẵn sàng, cố gắng hoàn thành từng mục nhỏ.', 'PUBLISHED', 0, 0, 0, '2026-07-24 11:20:00.000000', '2026-07-24 11:20:00.000000', '2026-07-24 11:20:00.000000'),
    (3034, 1002, 'Video tổng hợp một vài khoảnh khắc vui trong buổi sinh hoạt nhóm.', 'PUBLISHED', 0, 0, 0, '2026-07-24 11:40:00.000000', '2026-07-24 11:40:00.000000', '2026-07-24 11:40:00.000000'),
    (3035, 1003, 'Một góc xanh trong khuôn viên để nghỉ ngơi giữa hai tiết học.', 'PUBLISHED', 0, 0, 0, '2026-07-24 12:00:00.000000', '2026-07-24 12:00:00.000000', '2026-07-24 12:00:00.000000'),
    (3036, 1004, 'Hai món ăn quen thuộc trong danh sách ăn trưa của sinh viên.', 'PUBLISHED', 0, 0, 0, '2026-07-24 12:20:00.000000', '2026-07-24 12:20:00.000000', '2026-07-24 12:20:00.000000'),
    (3037, 1005, 'Tài liệu và cà phê, bộ đôi đồng hành trong mùa thi.', 'PUBLISHED', 0, 0, 0, '2026-07-24 12:40:00.000000', '2026-07-24 12:40:00.000000', '2026-07-24 12:40:00.000000'),
    (3038, 1006, 'Kết quả sau một tuần cùng nhau hoàn thiện giao diện sản phẩm.', 'PUBLISHED', 0, 0, 0, '2026-07-24 13:00:00.000000', '2026-07-24 13:00:00.000000', '2026-07-24 13:00:00.000000'),
    (3039, 1002, 'Album nhỏ về hoạt động tình nguyện cùng các bạn trong câu lạc bộ.', 'PUBLISHED', 0, 0, 0, '2026-07-24 13:20:00.000000', '2026-07-24 13:20:00.000000', '2026-07-24 13:20:00.000000'),
    (3040, 1003, 'Khép lại một ngày học tập bằng khung cảnh hoàng hôn ở sân trường.', 'PUBLISHED', 0, 0, 0, '2026-07-24 13:40:00.000000', '2026-07-24 13:40:00.000000', '2026-07-24 13:40:00.000000'),
    (3041, 1004, 'Một góc đọc sách mới vừa được bố trí ở khu tự học.', 'PUBLISHED', 0, 0, 0, '2026-07-24 14:00:00.000000', '2026-07-24 14:00:00.000000', '2026-07-24 14:00:00.000000'),
    (3042, 1005, 'Chia sẻ hai khoảnh khắc trong buổi hướng dẫn tân sinh viên hôm nay.', 'PUBLISHED', 0, 0, 0, '2026-07-24 14:20:00.000000', '2026-07-24 14:20:00.000000', '2026-07-24 14:20:00.000000'),
    (3043, 1006, 'Video ngắn ghi lại phần trình bày sản phẩm của nhóm.', 'PUBLISHED', 0, 0, 0, '2026-07-24 14:40:00.000000', '2026-07-24 14:40:00.000000', '2026-07-24 14:40:00.000000'),
    (3044, 1002, 'Một ngày nhiều deadline nhưng cuối cùng mọi việc cũng hoàn thành đúng kế hoạch.', 'PUBLISHED', 0, 0, 0, '2026-07-24 15:00:00.000000', '2026-07-24 15:00:00.000000', '2026-07-24 15:00:00.000000'),
    (3045, 1003, 'Cùng nhau chuẩn bị không gian cho sự kiện câu lạc bộ cuối tuần.', 'PUBLISHED', 0, 0, 0, '2026-07-24 15:20:00.000000', '2026-07-24 15:20:00.000000', '2026-07-24 15:20:00.000000');

-- Chỉ lưu URL và metadata ảnh/video; ứng dụng không lưu file nhị phân trong MySQL.
INSERT INTO post_media (
    id, post_id, media_url, storage_public_id, media_type, mime_type,
    file_size_bytes, width_px, height_px, duration_seconds, thumbnail_url, display_order, created_at
) VALUES
    (4001, 3001, 'https://images.unsplash.com/photo-1523050854058-8df90110c9f1?auto=format&fit=crop&w=1200&q=80', 'demo/posts/campus-3001', 'IMAGE', 'image/jpeg', 384512, 1200, 800, NULL, NULL, 0, '2026-07-20 02:00:00.000000'),
    (4002, 3003, 'https://images.unsplash.com/photo-1521587760476-6c12a4b040da?auto=format&fit=crop&w=1200&q=80', 'demo/posts/library-3003', 'IMAGE', 'image/jpeg', 412208, 1200, 800, NULL, NULL, 0, '2026-07-21 01:15:00.000000'),
    (4003, 3005, 'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?auto=format&fit=crop&w=1200&q=80', 'demo/posts/coding-3005', 'IMAGE', 'image/jpeg', 356740, 1200, 800, NULL, NULL, 0, '2026-07-21 06:20:00.000000'),
    (4004, 3006, 'https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?auto=format&fit=crop&w=1200&q=80', 'demo/posts/coffee-3006', 'IMAGE', 'image/jpeg', 298340, 1200, 800, NULL, NULL, 0, '2026-07-22 01:00:00.000000'),
    (4005, 3007, 'https://images.unsplash.com/photo-1529156069898-49953e39b3ac?auto=format&fit=crop&w=1200&q=80', 'demo/posts/club-3007-1', 'IMAGE', 'image/jpeg', 445670, 1200, 800, NULL, NULL, 0, '2026-07-22 03:30:00.000000'),
    (4006, 3007, 'https://images.unsplash.com/photo-1529390079861-591de354faf5?auto=format&fit=crop&w=1200&q=80', 'demo/posts/club-3007-2', 'IMAGE', 'image/jpeg', 421850, 1200, 800, NULL, NULL, 1, '2026-07-22 03:30:00.000000'),
    (4007, 3008, 'https://images.unsplash.com/photo-1523240795612-9a054b0db644?auto=format&fit=crop&w=1200&q=80', 'demo/posts/students-3008-1', 'IMAGE', 'image/jpeg', 478920, 1200, 800, NULL, NULL, 0, '2026-07-22 05:00:00.000000'),
    (4008, 3008, 'https://images.unsplash.com/photo-1517486808906-6ca8b3f04846?auto=format&fit=crop&w=1200&q=80', 'demo/posts/students-3008-2', 'IMAGE', 'image/jpeg', 463110, 1200, 800, NULL, NULL, 1, '2026-07-22 05:00:00.000000'),
    (4009, 3009, 'https://images.unsplash.com/photo-1524995997946-a1c2e315a42f?auto=format&fit=crop&w=1200&q=80', 'demo/posts/library-3009', 'IMAGE', 'image/jpeg', 392410, 1200, 800, NULL, NULL, 0, '2026-07-22 07:15:00.000000'),
    (4010, 3010, 'https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4', 'demo/posts/club-video-3010', 'VIDEO', 'video/mp4', 2498125, 1280, 720, 15, 'https://images.unsplash.com/photo-1529156069898-49953e39b3ac?auto=format&fit=crop&w=1200&q=80', 0, '2026-07-22 09:20:00.000000'),
    (4011, 3011, 'https://images.unsplash.com/photo-1562774053-701939374585?auto=format&fit=crop&w=1200&q=80', 'demo/posts/campus-3011-1', 'IMAGE', 'image/jpeg', 421560, 1200, 800, NULL, NULL, 0, '2026-07-23 01:10:00.000000'),
    (4012, 3011, 'https://images.unsplash.com/photo-1541339907198-e08756dedf3f?auto=format&fit=crop&w=1200&q=80', 'demo/posts/campus-3011-2', 'IMAGE', 'image/jpeg', 405210, 1200, 800, NULL, NULL, 1, '2026-07-23 01:10:00.000000'),
    (4013, 3012, 'https://images.unsplash.com/photo-1556761175-b413da4baf72?auto=format&fit=crop&w=1200&q=80', 'demo/posts/workshop-3012', 'IMAGE', 'image/jpeg', 387420, 1200, 800, NULL, NULL, 0, '2026-07-23 02:35:00.000000'),
    (4014, 3013, 'https://images.unsplash.com/photo-1522071820081-009f0129c71c?auto=format&fit=crop&w=1200&q=80', 'demo/posts/teamwork-3013', 'IMAGE', 'image/jpeg', 433890, 1200, 800, NULL, NULL, 0, '2026-07-23 04:00:00.000000'),
    (4015, 3014, 'https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4', 'demo/posts/product-video-3014', 'VIDEO', 'video/mp4', 15648210, 1280, 720, 15, 'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?auto=format&fit=crop&w=1200&q=80', 0, '2026-07-23 06:25:00.000000'),
    (4016, 3015, 'https://images.unsplash.com/photo-1456513080510-7bf3a84b82f8?auto=format&fit=crop&w=1200&q=80', 'demo/posts/study-3015', 'IMAGE', 'image/jpeg', 365720, 1200, 800, NULL, NULL, 0, '2026-07-23 08:40:00.000000'),
    (4017, 3016, 'https://images.unsplash.com/photo-1511632765486-a01980e01a18?auto=format&fit=crop&w=1200&q=80', 'demo/posts/club-3016', 'IMAGE', 'image/jpeg', 447610, 1200, 800, NULL, NULL, 0, '2026-07-24 00:30:00.000000'),
    (4018, 3017, 'https://images.unsplash.com/photo-1497366754035-f200968a6e72?auto=format&fit=crop&w=1200&q=80', 'demo/posts/study-space-3017-1', 'IMAGE', 'image/jpeg', 408330, 1200, 800, NULL, NULL, 0, '2026-07-24 02:05:00.000000'),
    (4019, 3017, 'https://images.unsplash.com/photo-1497366811353-6870744d04b2?auto=format&fit=crop&w=1200&q=80', 'demo/posts/study-space-3017-2', 'IMAGE', 'image/jpeg', 419820, 1200, 800, NULL, NULL, 1, '2026-07-24 02:05:00.000000'),
    (4020, 3018, 'https://images.unsplash.com/photo-1586281380349-632531db7ed4?auto=format&fit=crop&w=1200&q=80', 'demo/posts/cv-3018', 'IMAGE', 'image/jpeg', 376440, 1200, 800, NULL, NULL, 0, '2026-07-24 03:45:00.000000'),
    (4021, 3020, 'https://images.unsplash.com/photo-1523580494863-6f3031224c94?auto=format&fit=crop&w=1200&q=80', 'demo/posts/student-day-3020-1', 'IMAGE', 'image/jpeg', 451230, 1200, 800, NULL, NULL, 0, '2026-07-24 06:50:00.000000'),
    (4022, 3020, 'https://images.unsplash.com/photo-1531482615713-2afd69097998?auto=format&fit=crop&w=1200&q=80', 'demo/posts/student-day-3020-2', 'IMAGE', 'image/jpeg', 438760, 1200, 800, NULL, NULL, 1, '2026-07-24 06:50:00.000000'),
    (4023, 3020, 'https://images.unsplash.com/photo-1528605248644-14dd04022da1?auto=format&fit=crop&w=1200&q=80', 'demo/posts/student-day-3020-3', 'IMAGE', 'image/jpeg', 462190, 1200, 800, NULL, NULL, 2, '2026-07-24 06:50:00.000000'),
    (4024, 3021, 'https://images.unsplash.com/photo-1507842217343-583bb7270b66?auto=format&fit=crop&w=1200&q=80', 'demo/posts/reading-3021', 'IMAGE', 'image/jpeg', 398220, 1200, 800, NULL, NULL, 0, '2026-07-24 07:20:00.000000'),
    (4025, 3022, 'https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4', 'demo/posts/presentation-video-3022', 'VIDEO', 'video/mp4', 1018253, 1280, 720, 60, 'https://images.unsplash.com/photo-1524178232363-1fb2b075b655?auto=format&fit=crop&w=1200&q=80', 0, '2026-07-24 07:40:00.000000'),
    (4026, 3023, 'https://images.unsplash.com/photo-1552664730-d307ca884978?auto=format&fit=crop&w=1200&q=80', 'demo/posts/mentoring-3023', 'IMAGE', 'image/jpeg', 423510, 1200, 800, NULL, NULL, 0, '2026-07-24 08:00:00.000000'),
    (4027, 3024, 'https://images.unsplash.com/photo-1498243691581-b145c3f54a5a?auto=format&fit=crop&w=1200&q=80', 'demo/posts/lab-3024-1', 'IMAGE', 'image/jpeg', 406820, 1200, 800, NULL, NULL, 0, '2026-07-24 08:20:00.000000'),
    (4028, 3024, 'https://images.unsplash.com/photo-1497366216548-37526070297c?auto=format&fit=crop&w=1200&q=80', 'demo/posts/lab-3024-2', 'IMAGE', 'image/jpeg', 418760, 1200, 800, NULL, NULL, 1, '2026-07-24 08:20:00.000000'),
    (4029, 3026, 'https://images.unsplash.com/photo-1521737711867-e3b97375f902?auto=format&fit=crop&w=1200&q=80', 'demo/posts/team-skill-3026', 'IMAGE', 'image/jpeg', 391440, 1200, 800, NULL, NULL, 0, '2026-07-24 09:00:00.000000'),
    (4030, 3027, 'https://images.unsplash.com/photo-1524758631624-e2822e304c36?auto=format&fit=crop&w=1200&q=80', 'demo/posts/company-3027-1', 'IMAGE', 'image/jpeg', 445120, 1200, 800, NULL, NULL, 0, '2026-07-24 09:20:00.000000'),
    (4031, 3027, 'https://images.unsplash.com/photo-1497366412874-3415097a27e7?auto=format&fit=crop&w=1200&q=80', 'demo/posts/company-3027-2', 'IMAGE', 'image/jpeg', 432810, 1200, 800, NULL, NULL, 1, '2026-07-24 09:20:00.000000'),
    (4032, 3027, 'https://images.unsplash.com/photo-1497366754035-f200968a6e72?auto=format&fit=crop&w=1200&q=80', 'demo/posts/company-3027-3', 'IMAGE', 'image/jpeg', 429630, 1200, 800, NULL, NULL, 2, '2026-07-24 09:20:00.000000'),
    (4033, 3028, 'https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyrides.mp4', 'demo/posts/sprint-video-3028', 'VIDEO', 'video/mp4', 2372821, 1280, 720, 15, 'https://images.unsplash.com/photo-1517245386807-bb43f82c33c4?auto=format&fit=crop&w=1200&q=80', 0, '2026-07-24 09:40:00.000000'),
    (4034, 3029, 'https://images.unsplash.com/photo-1551836022-d5d88e9218df?auto=format&fit=crop&w=1200&q=80', 'demo/posts/internship-3029', 'IMAGE', 'image/jpeg', 386520, 1200, 800, NULL, NULL, 0, '2026-07-24 10:00:00.000000'),
    (4035, 3030, 'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?auto=format&fit=crop&w=1200&q=80', 'demo/posts/computer-3030', 'IMAGE', 'image/jpeg', 414230, 1200, 800, NULL, NULL, 0, '2026-07-24 10:20:00.000000'),
    (4036, 3031, 'https://images.unsplash.com/photo-1522202176988-66273c2fd55f?auto=format&fit=crop&w=1200&q=80', 'demo/posts/study-group-3031-1', 'IMAGE', 'image/jpeg', 437210, 1200, 800, NULL, NULL, 0, '2026-07-24 10:40:00.000000'),
    (4037, 3031, 'https://images.unsplash.com/photo-1523240795612-9a054b0db644?auto=format&fit=crop&w=1200&q=80', 'demo/posts/study-group-3031-2', 'IMAGE', 'image/jpeg', 448720, 1200, 800, NULL, NULL, 1, '2026-07-24 10:40:00.000000'),
    (4038, 3033, 'https://images.unsplash.com/photo-1484480974693-6ca0a78fb36b?auto=format&fit=crop&w=1200&q=80', 'demo/posts/planning-3033', 'IMAGE', 'image/jpeg', 362410, 1200, 800, NULL, NULL, 0, '2026-07-24 11:20:00.000000'),
    (4039, 3034, 'https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerMeltdowns.mp4', 'demo/posts/group-video-3034', 'VIDEO', 'video/mp4', 3173828, 1280, 720, 15, 'https://images.unsplash.com/photo-1511632765486-a01980e01a18?auto=format&fit=crop&w=1200&q=80', 0, '2026-07-24 11:40:00.000000'),
    (4040, 3035, 'https://images.unsplash.com/photo-1441974231531-c6227db76b6e?auto=format&fit=crop&w=1200&q=80', 'demo/posts/green-campus-3035', 'IMAGE', 'image/jpeg', 439510, 1200, 800, NULL, NULL, 0, '2026-07-24 12:00:00.000000'),
    (4041, 3036, 'https://images.unsplash.com/photo-1547592180-85f173990554?auto=format&fit=crop&w=1200&q=80', 'demo/posts/lunch-3036-1', 'IMAGE', 'image/jpeg', 395640, 1200, 800, NULL, NULL, 0, '2026-07-24 12:20:00.000000'),
    (4042, 3036, 'https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?auto=format&fit=crop&w=1200&q=80', 'demo/posts/lunch-3036-2', 'IMAGE', 'image/jpeg', 401230, 1200, 800, NULL, NULL, 1, '2026-07-24 12:20:00.000000'),
    (4043, 3037, 'https://images.unsplash.com/photo-1511081692775-05d0f180a065?auto=format&fit=crop&w=1200&q=80', 'demo/posts/exam-3037', 'IMAGE', 'image/jpeg', 374890, 1200, 800, NULL, NULL, 0, '2026-07-24 12:40:00.000000'),
    (4044, 3038, 'https://images.unsplash.com/photo-1558655146-d09347e92766?auto=format&fit=crop&w=1200&q=80', 'demo/posts/interface-3038', 'IMAGE', 'image/jpeg', 426710, 1200, 800, NULL, NULL, 0, '2026-07-24 13:00:00.000000'),
    (4045, 3039, 'https://images.unsplash.com/photo-1559027615-cd4628902d4a?auto=format&fit=crop&w=1200&q=80', 'demo/posts/volunteer-3039-1', 'IMAGE', 'image/jpeg', 447850, 1200, 800, NULL, NULL, 0, '2026-07-24 13:20:00.000000'),
    (4046, 3039, 'https://images.unsplash.com/photo-1593113598332-cd288d649433?auto=format&fit=crop&w=1200&q=80', 'demo/posts/volunteer-3039-2', 'IMAGE', 'image/jpeg', 452340, 1200, 800, NULL, NULL, 1, '2026-07-24 13:20:00.000000'),
    (4047, 3039, 'https://images.unsplash.com/photo-1488521787991-ed7bbaae773c?auto=format&fit=crop&w=1200&q=80', 'demo/posts/volunteer-3039-3', 'IMAGE', 'image/jpeg', 461920, 1200, 800, NULL, NULL, 2, '2026-07-24 13:20:00.000000'),
    (4048, 3040, 'https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=1200&q=80', 'demo/posts/sunset-3040', 'IMAGE', 'image/jpeg', 443610, 1200, 800, NULL, NULL, 0, '2026-07-24 13:40:00.000000'),
    (4049, 3041, 'https://images.unsplash.com/photo-1481627834876-b7833e8f5570?auto=format&fit=crop&w=1200&q=80', 'demo/posts/reading-corner-3041', 'IMAGE', 'image/jpeg', 397810, 1200, 800, NULL, NULL, 0, '2026-07-24 14:00:00.000000'),
    (4050, 3042, 'https://images.unsplash.com/photo-1529390079861-591de354faf5?auto=format&fit=crop&w=1200&q=80', 'demo/posts/orientation-3042-1', 'IMAGE', 'image/jpeg', 438210, 1200, 800, NULL, NULL, 0, '2026-07-24 14:20:00.000000'),
    (4051, 3042, 'https://images.unsplash.com/photo-1517486808906-6ca8b3f04846?auto=format&fit=crop&w=1200&q=80', 'demo/posts/orientation-3042-2', 'IMAGE', 'image/jpeg', 445620, 1200, 800, NULL, NULL, 1, '2026-07-24 14:20:00.000000'),
    (4052, 3043, 'https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerMobsters.mp4', 'demo/posts/presentation-video-3043', 'VIDEO', 'video/mp4', 1283941, 1280, 720, 15, 'https://images.unsplash.com/photo-1542744173-8e7e53415bb0?auto=format&fit=crop&w=1200&q=80', 0, '2026-07-24 14:40:00.000000'),
    (4053, 3044, 'https://images.unsplash.com/photo-1504384308090-c894fdcc538d?auto=format&fit=crop&w=1200&q=80', 'demo/posts/deadline-3044', 'IMAGE', 'image/jpeg', 416730, 1200, 800, NULL, NULL, 0, '2026-07-24 15:00:00.000000'),
    (4054, 3045, 'https://images.unsplash.com/photo-1501281668745-f7f57925c3b4?auto=format&fit=crop&w=1200&q=80', 'demo/posts/event-3045', 'IMAGE', 'image/jpeg', 452180, 1200, 800, NULL, NULL, 0, '2026-07-24 15:20:00.000000');

INSERT INTO hashtags (id, normalized_name, display_name, post_count, created_at, updated_at) VALUES
    (5001, 'đời sống sinh viên', 'đời sống sinh viên', 0, @seed_time, @seed_time),
    (5002, 'học tập', 'học tập', 0, @seed_time, @seed_time),
    (5003, 'thực tập', 'thực tập', 0, @seed_time, @seed_time),
    (5004, 'lập trình', 'lập trình', 0, @seed_time, @seed_time),
    (5005, 'câu lạc bộ', 'câu lạc bộ', 0, @seed_time, @seed_time);

INSERT INTO post_hashtags (post_id, hashtag_id, created_at) VALUES
    (3001, 5001, '2026-07-20 02:00:00.000000'),
    (3002, 5002, '2026-07-20 04:30:00.000000'),
    (3003, 5002, '2026-07-21 01:15:00.000000'),
    (3004, 5003, '2026-07-21 03:00:00.000000'),
    (3005, 5004, '2026-07-21 06:20:00.000000'),
    (3006, 5001, '2026-07-22 01:00:00.000000'),
    (3007, 5005, '2026-07-22 03:30:00.000000'),
    (3008, 5001, '2026-07-22 05:00:00.000000'),
    (3009, 5002, '2026-07-22 07:15:00.000000'),
    (3010, 5005, '2026-07-22 09:20:00.000000'),
    (3011, 5001, '2026-07-23 01:10:00.000000'),
    (3012, 5003, '2026-07-23 02:35:00.000000'),
    (3013, 5004, '2026-07-23 04:00:00.000000'),
    (3014, 5004, '2026-07-23 06:25:00.000000'),
    (3015, 5002, '2026-07-23 08:40:00.000000'),
    (3016, 5005, '2026-07-24 00:30:00.000000'),
    (3017, 5001, '2026-07-24 02:05:00.000000'),
    (3018, 5003, '2026-07-24 03:45:00.000000'),
    (3020, 5001, '2026-07-24 06:50:00.000000'),
    (3021, 5002, '2026-07-24 07:20:00.000000'),
    (3022, 5004, '2026-07-24 07:40:00.000000'),
    (3023, 5003, '2026-07-24 08:00:00.000000'),
    (3024, 5001, '2026-07-24 08:20:00.000000'),
    (3025, 5002, '2026-07-24 08:40:00.000000'),
    (3026, 5005, '2026-07-24 09:00:00.000000'),
    (3027, 5003, '2026-07-24 09:20:00.000000'),
    (3028, 5004, '2026-07-24 09:40:00.000000'),
    (3029, 5003, '2026-07-24 10:00:00.000000'),
    (3030, 5004, '2026-07-24 10:20:00.000000'),
    (3031, 5002, '2026-07-24 10:40:00.000000'),
    (3032, 5004, '2026-07-24 11:00:00.000000'),
    (3033, 5002, '2026-07-24 11:20:00.000000'),
    (3034, 5005, '2026-07-24 11:40:00.000000'),
    (3035, 5001, '2026-07-24 12:00:00.000000'),
    (3036, 5001, '2026-07-24 12:20:00.000000'),
    (3037, 5002, '2026-07-24 12:40:00.000000'),
    (3038, 5004, '2026-07-24 13:00:00.000000'),
    (3039, 5005, '2026-07-24 13:20:00.000000'),
    (3040, 5001, '2026-07-24 13:40:00.000000'),
    (3041, 5002, '2026-07-24 14:00:00.000000'),
    (3042, 5005, '2026-07-24 14:20:00.000000'),
    (3043, 5004, '2026-07-24 14:40:00.000000'),
    (3044, 5002, '2026-07-24 15:00:00.000000'),
    (3045, 5005, '2026-07-24 15:20:00.000000');

INSERT INTO post_likes (user_id, post_id, created_at) VALUES
    (1003, 3001, '2026-07-20 03:00:00.000000'), (1004, 3001, '2026-07-20 03:05:00.000000'),
    (1005, 3001, '2026-07-20 03:10:00.000000'), (1002, 3002, '2026-07-20 05:00:00.000000'),
    (1004, 3002, '2026-07-20 05:10:00.000000'), (1002, 3003, '2026-07-21 02:00:00.000000'),
    (1003, 3003, '2026-07-21 02:05:00.000000'), (1006, 3003, '2026-07-21 02:10:00.000000'),
    (1002, 3004, '2026-07-21 04:00:00.000000'), (1003, 3004, '2026-07-21 04:05:00.000000'),
    (1004, 3005, '2026-07-21 07:00:00.000000'), (1005, 3005, '2026-07-21 07:05:00.000000'),
    (1006, 3006, '2026-07-22 02:00:00.000000'), (1002, 3007, '2026-07-22 04:00:00.000000'),
    (1005, 3007, '2026-07-22 04:05:00.000000'), (1006, 3008, '2026-07-22 05:30:00.000000');

INSERT INTO comments (id, post_id, user_id, parent_comment_id, content, status, deleted_at, created_at, updated_at) VALUES
    (6001, 3001, 1003, NULL, 'Ảnh đẹp quá, nhìn là muốn quay lại trường ngay!', 'PUBLISHED', NULL, '2026-07-20 03:20:00.000000', '2026-07-20 03:20:00.000000'),
    (6002, 3001, 1002, 6001, 'Cảm ơn bạn, hôm đó thời tiết cũng rất đẹp.', 'PUBLISHED', NULL, '2026-07-20 03:30:00.000000', '2026-07-20 03:30:00.000000'),
    (6003, 3002, 1006, NULL, 'Nhóm còn chỗ không? Mình muốn tham gia cùng.', 'PUBLISHED', NULL, '2026-07-20 05:20:00.000000', '2026-07-20 05:20:00.000000'),
    (6004, 3003, 1002, NULL, 'Góc này ở tầng mấy vậy bạn?', 'PUBLISHED', NULL, '2026-07-21 02:20:00.000000', '2026-07-21 02:20:00.000000'),
    (6005, 3003, 1004, 6004, 'Ở tầng ba, gần khu tài liệu tham khảo nhé.', 'PUBLISHED', NULL, '2026-07-21 02:30:00.000000', '2026-07-21 02:30:00.000000'),
    (6006, 3004, 1003, NULL, 'Bạn chia sẻ tài liệu workshop giúp mình với nhé.', 'PUBLISHED', NULL, '2026-07-21 04:20:00.000000', '2026-07-21 04:20:00.000000'),
    (6007, 3005, 1003, NULL, 'Chúc mừng! Nhớ chia sẻ kinh nghiệm tích hợp API nha.', 'PUBLISHED', NULL, '2026-07-21 07:20:00.000000', '2026-07-21 07:20:00.000000'),
    (6008, 3006, 1005, NULL, 'Quán này có mở cuối tuần không?', 'PUBLISHED', NULL, '2026-07-22 02:15:00.000000', '2026-07-22 02:15:00.000000');

INSERT INTO saved_posts (user_id, post_id, created_at) VALUES
    (1002, 3004, '2026-07-21 04:10:00.000000'),
    (1003, 3005, '2026-07-21 07:10:00.000000'),
    (1004, 3006, '2026-07-22 02:10:00.000000'),
    (1005, 3002, '2026-07-20 05:15:00.000000'),
    (1006, 3003, '2026-07-21 02:15:00.000000');

INSERT INTO reports (
    id, reporter_id, post_id, reason, description, status, resolved_by, resolved_at,
    resolution_note, post_content_snapshot, post_media_snapshot, created_at, updated_at
) VALUES
    (7001, 1005, 3002, 'OTHER', 'Dữ liệu demo cho màn hình quản trị báo cáo.', 'PENDING', NULL, NULL, NULL,
     'Mình đang tìm thêm hai bạn lập nhóm ôn môn Cơ sở dữ liệu.', JSON_ARRAY(), '2026-07-22 05:40:00.000000', '2026-07-22 05:40:00.000000'),
    (7002, 1004, 3004, 'SPAM', 'Báo cáo demo đã được quản trị viên xem xét.', 'REJECTED', 1001, '2026-07-22 06:00:00.000000',
     'Nội dung không vi phạm tiêu chuẩn cộng đồng.', 'Có bạn nào đang chuẩn bị CV xin thực tập không?', JSON_ARRAY(), '2026-07-22 05:45:00.000000', '2026-07-22 06:00:00.000000');

INSERT INTO notifications (
    id, recipient_id, actor_id, type, post_id, comment_id, report_id, read_at, deleted_at, created_at, updated_at
) VALUES
    (8001, 1002, 1003, 'FOLLOW', NULL, NULL, NULL, NULL, NULL, '2026-07-20 01:10:00.000000', '2026-07-20 01:10:00.000000'),
    (8002, 1002, 1003, 'POST_LIKE', 3001, NULL, NULL, NULL, NULL, '2026-07-20 03:00:00.000000', '2026-07-20 03:00:00.000000'),
    (8003, 1002, 1003, 'POST_COMMENT', 3001, 6001, NULL, NULL, NULL, '2026-07-20 03:20:00.000000', '2026-07-20 03:20:00.000000'),
    (8004, 1003, 1002, 'COMMENT_REPLY', 3001, 6002, NULL, NULL, NULL, '2026-07-20 03:30:00.000000', '2026-07-20 03:30:00.000000'),
    (8005, 1004, 1002, 'POST_COMMENT', 3003, 6004, NULL, '2026-07-21 03:00:00.000000', NULL, '2026-07-21 02:20:00.000000', '2026-07-21 03:00:00.000000'),
    (8006, 1005, 1001, 'REPORT_REJECTED', 3004, NULL, 7002, NULL, NULL, '2026-07-22 06:00:00.000000', '2026-07-22 06:00:00.000000');

COMMIT;

-- Hậu kiểm nhanh cho cả Auth và dữ liệu mạng xã hội demo.
SELECT COUNT(*) AS seeded_users FROM users WHERE id BETWEEN 1001 AND 1008;
SELECT COUNT(*) AS seeded_profiles FROM user_profiles WHERE user_id BETWEEN 1001 AND 1008;
SELECT COUNT(*) AS seeded_provider_links FROM user_auth_providers WHERE id BETWEEN 2001 AND 2003;
SELECT COUNT(*) AS seeded_admins FROM users WHERE role = 'ADMIN' AND id BETWEEN 1001 AND 1008;
SELECT COUNT(*) AS seeded_posts FROM posts WHERE id BETWEEN 3001 AND 3045;
SELECT COUNT(*) AS seeded_post_media FROM post_media WHERE id BETWEEN 4001 AND 4054;
SELECT COUNT(*) AS seeded_comments FROM comments WHERE id BETWEEN 6001 AND 6008;
SELECT COUNT(*) AS seeded_notifications FROM notifications WHERE id BETWEEN 8001 AND 8006;
SELECT id, like_count, comment_count FROM posts WHERE id BETWEEN 3001 AND 3045 ORDER BY id;

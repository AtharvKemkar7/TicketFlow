-- HelixDesk HeidiSQL / MariaDB dump
-- Import in HeidiSQL: File -> Load SQL file -> helixdesk-heidisql.sql -> Execute
-- Session: 127.0.0.1:3306, user helixdesk, password helixdesk, database helixdesk
-- If CREATE USER fails, run this file as root once, then reconnect as helixdesk.

CREATE DATABASE IF NOT EXISTS helixdesk
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;


ALTER USER 'helixdesk'@'localhost' IDENTIFIED BY 'helixdesk';
ALTER USER 'helixdesk'@'%' IDENTIFIED BY 'helixdesk';
GRANT ALL PRIVILEGES ON helixdesk.* TO 'helixdesk'@'localhost';
GRANT ALL PRIVILEGES ON helixdesk.* TO 'helixdesk'@'%';
FLUSH PRIVILEGES;

USE helixdesk;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE IF NOT EXISTS users (
  id BIGINT NOT NULL AUTO_INCREMENT,
  email VARCHAR(120) NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  first_name VARCHAR(80) NOT NULL,
  last_name VARCHAR(80) NOT NULL,
  role VARCHAR(20) NOT NULL,
  status VARCHAR(20) NOT NULL,
  department VARCHAR(80) NULL,
  version BIGINT NULL,
  created_at DATETIME(6) NULL,
  updated_at DATETIME(6) NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS categories (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(80) NOT NULL,
  description VARCHAR(400) NULL,
  active BIT(1) NOT NULL,
  required_skill_name VARCHAR(80) NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_categories_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS subcategories (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(80) NOT NULL,
  category_id BIGINT NOT NULL,
  active BIT(1) NOT NULL,
  required_skill_name VARCHAR(80) NULL,
  PRIMARY KEY (id),
  KEY idx_subcategories_category (category_id),
  CONSTRAINT fk_subcategories_category FOREIGN KEY (category_id) REFERENCES categories (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS teams (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(80) NOT NULL,
  description VARCHAR(400) NULL,
  active BIT(1) NOT NULL,
  primary_category_id BIGINT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_teams_name (name),
  KEY idx_teams_category (primary_category_id),
  CONSTRAINT fk_teams_category FOREIGN KEY (primary_category_id) REFERENCES categories (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS skills (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(80) NOT NULL,
  description VARCHAR(400) NULL,
  active BIT(1) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_skills_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS specialist_profiles (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  team_id BIGINT NULL,
  support_level VARCHAR(10) NOT NULL,
  availability VARCHAR(20) NOT NULL,
  current_workload INT NOT NULL,
  active BIT(1) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_specialist_user (user_id),
  KEY idx_specialist_team (team_id),
  CONSTRAINT fk_specialist_user FOREIGN KEY (user_id) REFERENCES users (id),
  CONSTRAINT fk_specialist_team FOREIGN KEY (team_id) REFERENCES teams (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS specialist_skills (
  id BIGINT NOT NULL AUTO_INCREMENT,
  specialist_id BIGINT NOT NULL,
  skill_id BIGINT NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_specialist_skill (specialist_id, skill_id),
  KEY idx_specialist_skills_skill (skill_id),
  CONSTRAINT fk_specialist_skills_specialist FOREIGN KEY (specialist_id) REFERENCES specialist_profiles (id),
  CONSTRAINT fk_specialist_skills_skill FOREIGN KEY (skill_id) REFERENCES skills (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS priority_rules (
  id BIGINT NOT NULL AUTO_INCREMENT,
  impact VARCHAR(20) NOT NULL,
  urgency VARCHAR(20) NOT NULL,
  priority VARCHAR(20) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_priority_impact_urgency (impact, urgency)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sla_policies (
  id BIGINT NOT NULL AUTO_INCREMENT,
  priority VARCHAR(20) NOT NULL,
  response_target_minutes INT NOT NULL,
  resolution_target_minutes INT NOT NULL,
  at_risk_threshold_minutes INT NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_sla_priority (priority)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS knowledge_articles (
  id BIGINT NOT NULL AUTO_INCREMENT,
  title VARCHAR(180) NOT NULL,
  content TEXT NOT NULL,
  category_id BIGINT NULL,
  subcategory_id BIGINT NULL,
  tags VARCHAR(255) NULL,
  status VARCHAR(20) NOT NULL,
  created_by_id BIGINT NULL,
  updated_by_id BIGINT NULL,
  created_at DATETIME(6) NULL,
  updated_at DATETIME(6) NULL,
  PRIMARY KEY (id),
  KEY idx_knowledge_category (category_id),
  KEY idx_knowledge_subcategory (subcategory_id),
  CONSTRAINT fk_knowledge_category FOREIGN KEY (category_id) REFERENCES categories (id),
  CONSTRAINT fk_knowledge_subcategory FOREIGN KEY (subcategory_id) REFERENCES subcategories (id),
  CONSTRAINT fk_knowledge_created_by FOREIGN KEY (created_by_id) REFERENCES users (id),
  CONSTRAINT fk_knowledge_updated_by FOREIGN KEY (updated_by_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS system_settings (
  setting_key VARCHAR(80) NOT NULL,
  setting_value VARCHAR(400) NULL,
  PRIMARY KEY (setting_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS tickets (
  id BIGINT NOT NULL AUTO_INCREMENT,
  ticket_number VARCHAR(32) NOT NULL,
  title VARCHAR(180) NOT NULL,
  description TEXT NOT NULL,
  status VARCHAR(40) NOT NULL,
  priority VARCHAR(20) NOT NULL,
  impact VARCHAR(20) NOT NULL,
  urgency VARCHAR(20) NOT NULL,
  business_effect VARCHAR(400) NULL,
  requester_id BIGINT NOT NULL,
  category_id BIGINT NULL,
  subcategory_id BIGINT NULL,
  assigned_specialist_id BIGINT NULL,
  assigned_team_id BIGINT NULL,
  current_support_level VARCHAR(10) NULL,
  sla_state VARCHAR(20) NOT NULL,
  sla_response_due_at DATETIME(6) NULL,
  sla_resolution_due_at DATETIME(6) NULL,
  first_responded_at DATETIME(6) NULL,
  resolved_at DATETIME(6) NULL,
  closed_at DATETIME(6) NULL,
  resolution_summary TEXT NULL,
  ai_attempt_count INT NOT NULL,
  human_requested BIT(1) NOT NULL,
  version BIGINT NULL,
  created_at DATETIME(6) NULL,
  updated_at DATETIME(6) NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_tickets_number (ticket_number),
  KEY idx_tickets_requester (requester_id),
  KEY idx_tickets_status (status),
  KEY idx_tickets_specialist (assigned_specialist_id),
  CONSTRAINT fk_tickets_requester FOREIGN KEY (requester_id) REFERENCES users (id),
  CONSTRAINT fk_tickets_category FOREIGN KEY (category_id) REFERENCES categories (id),
  CONSTRAINT fk_tickets_subcategory FOREIGN KEY (subcategory_id) REFERENCES subcategories (id),
  CONSTRAINT fk_tickets_specialist FOREIGN KEY (assigned_specialist_id) REFERENCES specialist_profiles (id),
  CONSTRAINT fk_tickets_team FOREIGN KEY (assigned_team_id) REFERENCES teams (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS ticket_comments (
  id BIGINT NOT NULL AUTO_INCREMENT,
  ticket_id BIGINT NOT NULL,
  author_id BIGINT NOT NULL,
  type VARCHAR(30) NOT NULL,
  body TEXT NOT NULL,
  created_at DATETIME(6) NULL,
  PRIMARY KEY (id),
  KEY idx_comments_ticket (ticket_id),
  CONSTRAINT fk_comments_ticket FOREIGN KEY (ticket_id) REFERENCES tickets (id),
  CONSTRAINT fk_comments_author FOREIGN KEY (author_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS ticket_attachments (
  id BIGINT NOT NULL AUTO_INCREMENT,
  ticket_id BIGINT NOT NULL,
  uploaded_by_id BIGINT NOT NULL,
  original_filename VARCHAR(180) NOT NULL,
  stored_path VARCHAR(255) NOT NULL,
  content_type VARCHAR(120) NULL,
  size_bytes BIGINT NOT NULL,
  created_at DATETIME(6) NULL,
  PRIMARY KEY (id),
  KEY idx_attachments_ticket (ticket_id),
  CONSTRAINT fk_attachments_ticket FOREIGN KEY (ticket_id) REFERENCES tickets (id),
  CONSTRAINT fk_attachments_user FOREIGN KEY (uploaded_by_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS ticket_assignment_history (
  id BIGINT NOT NULL AUTO_INCREMENT,
  ticket_id BIGINT NOT NULL,
  from_specialist_id BIGINT NULL,
  to_specialist_id BIGINT NULL,
  support_level VARCHAR(10) NULL,
  event_type VARCHAR(40) NULL,
  reason VARCHAR(400) NULL,
  created_at DATETIME(6) NULL,
  PRIMARY KEY (id),
  KEY idx_assignment_history_ticket (ticket_id),
  CONSTRAINT fk_assignment_history_ticket FOREIGN KEY (ticket_id) REFERENCES tickets (id),
  CONSTRAINT fk_assignment_history_from FOREIGN KEY (from_specialist_id) REFERENCES specialist_profiles (id),
  CONSTRAINT fk_assignment_history_to FOREIGN KEY (to_specialist_id) REFERENCES specialist_profiles (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS ticket_escalation_history (
  id BIGINT NOT NULL AUTO_INCREMENT,
  ticket_id BIGINT NOT NULL,
  from_level VARCHAR(10) NOT NULL,
  to_level VARCHAR(10) NOT NULL,
  from_specialist_id BIGINT NULL,
  to_specialist_id BIGINT NULL,
  reason VARCHAR(400) NOT NULL,
  notes TEXT NULL,
  created_at DATETIME(6) NULL,
  PRIMARY KEY (id),
  KEY idx_escalation_history_ticket (ticket_id),
  CONSTRAINT fk_escalation_history_ticket FOREIGN KEY (ticket_id) REFERENCES tickets (id),
  CONSTRAINT fk_escalation_history_from FOREIGN KEY (from_specialist_id) REFERENCES specialist_profiles (id),
  CONSTRAINT fk_escalation_history_to FOREIGN KEY (to_specialist_id) REFERENCES specialist_profiles (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS audit_logs (
  id BIGINT NOT NULL AUTO_INCREMENT,
  actor_id BIGINT NULL,
  actor_name VARCHAR(80) NULL,
  ticket_id BIGINT NULL,
  action VARCHAR(40) NOT NULL,
  previous_value VARCHAR(255) NULL,
  new_value VARCHAR(255) NULL,
  reason VARCHAR(400) NULL,
  created_at DATETIME(6) NULL,
  PRIMARY KEY (id),
  KEY idx_audit_ticket (ticket_id),
  CONSTRAINT fk_audit_actor FOREIGN KEY (actor_id) REFERENCES users (id),
  CONSTRAINT fk_audit_ticket FOREIGN KEY (ticket_id) REFERENCES tickets (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS notifications (
  id BIGINT NOT NULL AUTO_INCREMENT,
  recipient_id BIGINT NOT NULL,
  ticket_id BIGINT NULL,
  type VARCHAR(40) NOT NULL,
  message VARCHAR(255) NOT NULL,
  read_flag BIT(1) NOT NULL,
  created_at DATETIME(6) NULL,
  PRIMARY KEY (id),
  KEY idx_notifications_recipient (recipient_id),
  CONSTRAINT fk_notifications_recipient FOREIGN KEY (recipient_id) REFERENCES users (id),
  CONSTRAINT fk_notifications_ticket FOREIGN KEY (ticket_id) REFERENCES tickets (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS intake_sessions (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  conversation_json TEXT NULL,
  suggested_category VARCHAR(255) NULL,
  suggested_subcategory VARCHAR(255) NULL,
  impact VARCHAR(20) NULL,
  urgency VARCHAR(20) NULL,
  business_effect VARCHAR(400) NULL,
  title VARCHAR(180) NULL,
  problem_summary TEXT NULL,
  complete BIT(1) NOT NULL,
  created_ticket_id BIGINT NULL,
  created_at DATETIME(6) NULL,
  updated_at DATETIME(6) NULL,
  PRIMARY KEY (id),
  KEY idx_intake_user (user_id),
  CONSTRAINT fk_intake_user FOREIGN KEY (user_id) REFERENCES users (id),
  CONSTRAINT fk_intake_ticket FOREIGN KEY (created_ticket_id) REFERENCES tickets (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS ai_resolution_attempts (
  id BIGINT NOT NULL AUTO_INCREMENT,
  ticket_id BIGINT NOT NULL,
  attempt_number INT NOT NULL,
  solution_summary TEXT NULL,
  knowledge_sources TEXT NULL,
  successful BIT(1) NOT NULL,
  handed_off BIT(1) NOT NULL,
  handoff_reason VARCHAR(120) NULL,
  created_at DATETIME(6) NULL,
  PRIMARY KEY (id),
  KEY idx_ai_attempts_ticket (ticket_id),
  CONSTRAINT fk_ai_attempts_ticket FOREIGN KEY (ticket_id) REFERENCES tickets (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO users (id, email, password_hash, first_name, last_name, role, status, department, version, created_at, updated_at) VALUES
  (1, 'admin@helixdesk.local', '$2a$10$bQpUz.sn/lR/Y04DitFfEeNi35vIjOpR8VZJCmrDYU3PfZ43uiHHu', 'Admin', 'Helix', 'ADMIN', 'ACTIVE', 'IT Operations', 0, NOW(6), NOW(6)),
  (2, 'specialist@helixdesk.local', '$2a$10$bQpUz.sn/lR/Y04DitFfEeNi35vIjOpR8VZJCmrDYU3PfZ43uiHHu', 'Avery', 'Chen', 'SPECIALIST', 'ACTIVE', 'Network', 0, NOW(6), NOW(6)),
  (3, 'l2@helixdesk.local', '$2a$10$bQpUz.sn/lR/Y04DitFfEeNi35vIjOpR8VZJCmrDYU3PfZ43uiHHu', 'Morgan', 'Lee', 'SPECIALIST', 'ACTIVE', 'Network', 0, NOW(6), NOW(6)),
  (4, 'l3@helixdesk.local', '$2a$10$bQpUz.sn/lR/Y04DitFfEeNi35vIjOpR8VZJCmrDYU3PfZ43uiHHu', 'Jordan', 'Patel', 'SPECIALIST', 'ACTIVE', 'Network', 0, NOW(6), NOW(6)),
  (5, 'user@helixdesk.local', '$2a$10$bQpUz.sn/lR/Y04DitFfEeNi35vIjOpR8VZJCmrDYU3PfZ43uiHHu', 'Sam', 'Rivera', 'USER', 'ACTIVE', 'Finance', 0, NOW(6), NOW(6));

INSERT IGNORE INTO categories (id, name, description, active, required_skill_name) VALUES
  (1, 'NETWORK', 'Network connectivity and remote access', 1, 'VPN'),
  (2, 'ACCESS', 'Identity and access management', 1, 'IAM'),
  (3, 'HARDWARE', 'Endpoints and peripherals', 1, 'HARDWARE'),
  (4, 'SOFTWARE', 'Business applications', 1, 'APPLICATION'),
  (5, 'EMAIL', 'Messaging platforms', 1, 'EMAIL');

INSERT IGNORE INTO subcategories (id, name, category_id, active, required_skill_name) VALUES
  (1, 'VPN', 1, 1, 'VPN'),
  (2, 'CONNECTIVITY', 1, 1, 'NETWORK'),
  (3, 'CREDENTIALS', 2, 1, 'IAM'),
  (4, 'DEVICE', 3, 1, 'HARDWARE'),
  (5, 'APPLICATION', 4, 1, 'APPLICATION'),
  (6, 'OUTLOOK', 5, 1, 'EMAIL');

INSERT IGNORE INTO teams (id, name, description, active, primary_category_id) VALUES
  (1, 'Network Operations', 'Network Operations', 1, 1),
  (2, 'Endpoint Support', 'Endpoint Support', 1, 3),
  (3, 'Identity Services', 'Identity Services', 1, 2);

INSERT IGNORE INTO skills (id, name, description, active) VALUES
  (1, 'VPN', NULL, 1),
  (2, 'IAM', NULL, 1),
  (3, 'HARDWARE', NULL, 1),
  (4, 'APPLICATION', NULL, 1),
  (5, 'EMAIL', NULL, 1),
  (6, 'NETWORK', NULL, 1);

INSERT IGNORE INTO specialist_profiles (id, user_id, team_id, support_level, availability, current_workload, active) VALUES
  (1, 2, 1, 'L2', 'AVAILABLE', 0, 1),
  (2, 3, 1, 'L2', 'AVAILABLE', 0, 1),
  (3, 4, 1, 'L3', 'AVAILABLE', 0, 1);

INSERT IGNORE INTO specialist_skills (id, specialist_id, skill_id) VALUES
  (1, 1, 1),
  (2, 2, 1),
  (3, 3, 1);

INSERT IGNORE INTO priority_rules (id, impact, urgency, priority) VALUES
  (1, 'INDIVIDUAL', 'LOW', 'LOW'),
  (2, 'INDIVIDUAL', 'MEDIUM', 'MEDIUM'),
  (3, 'INDIVIDUAL', 'HIGH', 'HIGH'),
  (4, 'INDIVIDUAL', 'CRITICAL', 'CRITICAL'),
  (5, 'TEAM', 'LOW', 'MEDIUM'),
  (6, 'TEAM', 'MEDIUM', 'MEDIUM'),
  (7, 'TEAM', 'HIGH', 'HIGH'),
  (8, 'TEAM', 'CRITICAL', 'CRITICAL'),
  (9, 'DEPARTMENT', 'LOW', 'HIGH'),
  (10, 'DEPARTMENT', 'MEDIUM', 'HIGH'),
  (11, 'DEPARTMENT', 'HIGH', 'HIGH'),
  (12, 'DEPARTMENT', 'CRITICAL', 'CRITICAL'),
  (13, 'ORGANIZATION', 'LOW', 'CRITICAL'),
  (14, 'ORGANIZATION', 'MEDIUM', 'CRITICAL'),
  (15, 'ORGANIZATION', 'HIGH', 'CRITICAL'),
  (16, 'ORGANIZATION', 'CRITICAL', 'CRITICAL');

INSERT IGNORE INTO sla_policies (id, priority, response_target_minutes, resolution_target_minutes, at_risk_threshold_minutes) VALUES
  (1, 'LOW', 240, 1440, 120),
  (2, 'MEDIUM', 120, 480, 60),
  (3, 'HIGH', 30, 240, 30),
  (4, 'CRITICAL', 15, 120, 20);

INSERT IGNORE INTO knowledge_articles (id, title, content, category_id, tags, status, created_by_id, updated_by_id, created_at, updated_at) VALUES
  (1, 'VPN Connection Troubleshooting',
   'Attempt 1: Restart the VPN client, confirm you are on a trusted network, and retry the corporate VPN profile.\nDisconnect any personal VPN, then reconnect using the company client.\nAttempt 2: Verify VPN credentials, check DNS, confirm the VPN gateway is reachable, and review local firewall rules.\nIf the client still fails, capture the error code and request a specialist.',
   1, 'vpn,network,remote', 'PUBLISHED', 1, 1, NOW(6), NOW(6)),
  (2, 'Account Lockout Recovery',
   'Attempt 1: Use the self-service password reset portal and wait five minutes before signing in again.\nAttempt 2: Confirm MFA device time sync and retry. If still locked, a specialist must unlock the directory account.',
   2, 'password,iam,login', 'PUBLISHED', 1, 1, NOW(6), NOW(6));

INSERT IGNORE INTO system_settings (setting_key, setting_value) VALUES
  ('ticket.prefix', 'HX');

SET FOREIGN_KEY_CHECKS = 1;

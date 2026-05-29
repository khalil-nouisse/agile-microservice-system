-- ============================================================
-- Create all databases
-- ============================================================
CREATE DATABASE auth_db;
CREATE DATABASE project_db;
CREATE DATABASE workitem_db;
CREATE DATABASE sprint_db;
CREATE DATABASE notification_db;

-- ============================================================
-- Create dedicated users per service
-- ============================================================
CREATE USER auth_user WITH PASSWORD 'auth_pass';
CREATE USER project_user WITH PASSWORD 'project_pass';
CREATE USER workitem_user WITH PASSWORD 'workitem_pass';
CREATE USER sprint_user WITH PASSWORD 'sprint_pass';
CREATE USER notification_user WITH PASSWORD 'notification_pass';

-- ============================================================
-- Grant access — each user only accesses its own database
-- ============================================================
GRANT ALL PRIVILEGES ON DATABASE auth_db TO auth_user;
GRANT ALL PRIVILEGES ON DATABASE project_db TO project_user;
GRANT ALL PRIVILEGES ON DATABASE workitem_db TO workitem_user;
GRANT ALL PRIVILEGES ON DATABASE sprint_db TO sprint_user;
GRANT ALL PRIVILEGES ON DATABASE notification_db TO notification_user;
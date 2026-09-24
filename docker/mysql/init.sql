-- MySQL 首次初始化（docker-entrypoint-initdb.d）
-- 库名由 MYSQL_DATABASE=interview_guide 自动创建，此处仅加固字符集
ALTER DATABASE interview_guide CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

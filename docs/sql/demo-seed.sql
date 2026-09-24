-- AI Interview Guide 演示种子（模糊虚拟数据，非真实个人信息）
-- 适用：MySQL 8 / 库 interview_guide
-- 密码（BCrypt）：
--   admin / admin123
--   候选人统一 password: candidate123
--
-- 用法：
--   1) docker compose 首次启动会自动执行本文件
--   2) 或手动：mysql -uroot -p interview_guide < docs/sql/demo-seed.sql

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE IF NOT EXISTS `admin` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(64) NOT NULL,
  `password` VARCHAR(128) NOT NULL,
  `real_name` VARCHAR(64) NOT NULL,
  `created_at` DATETIME(6) DEFAULT NULL,
  `updated_at` DATETIME(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_admin_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `interviewer_config` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `ai_name` VARCHAR(64) NOT NULL,
  `system_prompt` TEXT,
  `created_at` DATETIME(6) DEFAULT NULL,
  `updated_at` DATETIME(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `job` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `job_name` VARCHAR(128) NOT NULL,
  `job_desc` TEXT,
  `status` INT NOT NULL,
  `interviewer_config_id` BIGINT DEFAULT NULL,
  `prompt` TEXT,
  `created_at` DATETIME(6) DEFAULT NULL,
  `updated_at` DATETIME(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `question_lib` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `job_id` BIGINT NOT NULL,
  `question` TEXT NOT NULL,
  `reference_answer` TEXT,
  `is_on` INT NOT NULL,
  `sort_order` INT DEFAULT NULL,
  `created_at` DATETIME(6) DEFAULT NULL,
  `updated_at` DATETIME(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_question_job_on` (`job_id`, `is_on`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `candidate` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `real_name` VARCHAR(64) NOT NULL,
  `mobile` VARCHAR(20) NOT NULL,
  `email` VARCHAR(128) DEFAULT NULL,
  `sex` INT DEFAULT NULL,
  `password` VARCHAR(128) NOT NULL,
  `job_id` BIGINT DEFAULT NULL,
  `remark` VARCHAR(500) DEFAULT NULL,
  `created_at` DATETIME(6) DEFAULT NULL,
  `updated_at` DATETIME(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_candidate_mobile` (`mobile`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `resumes` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `candidate_id` BIGINT NOT NULL,
  `file_name` VARCHAR(255) NOT NULL,
  `content_hash` VARCHAR(64) NOT NULL,
  `storage_key` VARCHAR(512) DEFAULT NULL,
  `raw_text` TEXT,
  `file_size` BIGINT DEFAULT NULL,
  `content_type` VARCHAR(128) DEFAULT NULL,
  `analyze_status` VARCHAR(32) NOT NULL,
  `analyze_error` VARCHAR(500) DEFAULT NULL,
  `created_at` DATETIME(6) DEFAULT NULL,
  `updated_at` DATETIME(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_resume_candidate_hash` (`candidate_id`, `content_hash`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `resume_analyses` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `resume_id` BIGINT NOT NULL,
  `overall_score` INT DEFAULT NULL,
  `summary` TEXT,
  `strengths` TEXT,
  `suggestions` TEXT,
  `analyzed_at` DATETIME(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `interview_sessions` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `candidate_id` BIGINT NOT NULL,
  `job_id` BIGINT NOT NULL,
  `resume_id` BIGINT DEFAULT NULL,
  `status` VARCHAR(32) NOT NULL,
  `total_questions` INT NOT NULL,
  `current_index` INT NOT NULL,
  `evaluate_status` VARCHAR(20) DEFAULT NULL,
  `evaluate_error` VARCHAR(500) DEFAULT NULL,
  `overall_score` INT DEFAULT NULL,
  `overall_feedback` TEXT,
  `strengths_json` TEXT,
  `improvements_json` TEXT,
  `created_at` DATETIME(6) DEFAULT NULL,
  `updated_at` DATETIME(6) DEFAULT NULL,
  `completed_at` DATETIME(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `interview_answers` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `session_id` BIGINT NOT NULL,
  `question_index` INT NOT NULL,
  `question_text` TEXT NOT NULL,
  `source` VARCHAR(16) NOT NULL,
  `reference_answer` TEXT,
  `answer_text` TEXT,
  `follow_up_question` TEXT,
  `follow_up_answer` TEXT,
  `created_at` DATETIME(6) DEFAULT NULL,
  `updated_at` DATETIME(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_answer_session` (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------- 演示数据（可重复执行：先清演示主键区间） ----------
DELETE FROM `interview_answers` WHERE `session_id` = 1;
DELETE FROM `interview_sessions` WHERE `id` = 1;
DELETE FROM `resume_analyses` WHERE `resume_id` = 1;
DELETE FROM `resumes` WHERE `id` = 1;
DELETE FROM `question_lib` WHERE `id` BETWEEN 1 AND 20;
DELETE FROM `candidate` WHERE `id` BETWEEN 1 AND 5;
DELETE FROM `job` WHERE `id` BETWEEN 1 AND 5;
DELETE FROM `interviewer_config` WHERE `id` BETWEEN 1 AND 5;
DELETE FROM `admin` WHERE `id` = 1 OR `username` = 'admin';

INSERT INTO `admin` (`id`, `username`, `password`, `real_name`, `created_at`, `updated_at`) VALUES
(1, 'admin',
 '$2a$10$dAlVe/8DqJAZoq6PIi4fdeBWdD4ZSC3.sO61V69vlxv9KNTpvDfda',
 '系统管理员', NOW(6), NOW(6));

INSERT INTO `interviewer_config` (`id`, `ai_name`, `system_prompt`, `created_at`, `updated_at`) VALUES
(1, '严苛技术官·阿衡',
 '你是一位严谨的后端技术面试官，关注基础扎实度、系统设计与工程落地，评分标准偏严格，反馈要具体可改进。',
 NOW(6), NOW(6)),
(2, '友好产品官·小林',
 '你是一位亲和的前端/协作向面试官，关注表达清晰度、用户体验意识与沟通协作，鼓励式反馈但不回避短板。',
 NOW(6), NOW(6));

INSERT INTO `job` (`id`, `job_name`, `job_desc`, `status`, `interviewer_config_id`, `prompt`, `created_at`, `updated_at`) VALUES
(1, 'Java后端开发（校招）',
 '参与业务服务开发与维护；熟悉 Spring Boot、MySQL、Redis；具备基本并发与接口设计能力。',
 1, 1,
 '评估维度：Java基础、Spring、数据库、缓存、问题解决。总分满分100，给出优缺点与改进建议。',
 NOW(6), NOW(6)),
(2, 'Web前端开发（校招）',
 '负责管理端/用户端页面开发；熟悉 Vue3、TypeScript、组件化与接口联调。',
 1, 2,
 '评估维度：Vue基础、TS、工程化、联调协作、表达。总分满分100。',
 NOW(6), NOW(6)),
(3, '测试工程师（实习）',
 '参与功能测试与用例设计；了解接口测试与基础自动化即可。',
 2, 1,
 '评估维度：测试思维、用例设计、缺陷描述清晰度。',
 NOW(6), NOW(6));

-- 岗位1：≥5题，演示 BANK 优先满额
INSERT INTO `question_lib`
(`id`, `job_id`, `question`, `reference_answer`, `is_on`, `sort_order`, `created_at`, `updated_at`) VALUES
(1, 1, '请解释 Java 中 == 与 equals 的区别，并举一个业务场景。',
 '==比较引用/基本类型值；equals可重写比较内容。场景：判断订单号字符串是否相等应用 equals。', 1, 10, NOW(6), NOW(6)),
(2, 1, 'Spring Boot 自动配置的大致原理是什么？',
 '基于 @EnableAutoConfiguration / AutoConfiguration.imports，按条件注解装配 Bean。', 1, 20, NOW(6), NOW(6)),
(3, 1, 'MySQL 索引失效的常见原因有哪些？',
 '对索引列函数/隐式转换、左右模糊、or 条件不当、选择性过低等。', 1, 30, NOW(6), NOW(6)),
(4, 1, 'Redis 适合做什么？不适合做什么？请结合面试系统举例。',
 '适合缓存会话Token、限流计数、消息流；不适合替代持久化主库存核心业务终态。', 1, 40, NOW(6), NOW(6)),
(5, 1, '如何设计一个「面试会话」表结构的核心字段？',
 '候选人、岗位、状态机、题目进度、评估结果与时间戳；答题明细可分表。', 1, 50, NOW(6), NOW(6)),
(6, 1, '（停用题）请背诵 JVM 所有垃圾回收器名称。',
 '偏死记硬背，演示 is_on=0 不会被抽到。', 0, 99, NOW(6), NOW(6)),
-- 岗位2：仅2题启用，演示 LLM 补题
(7, 2, 'Vue3 中 ref 与 reactive 的使用差异？',
 'ref 适合基本类型/需.value；reactive 适合对象，注意解构丢失响应式。', 1, 10, NOW(6), NOW(6)),
(8, 2, '前端如何安全地存储登录 Token？',
 '避免长期明文落盘；可用内存+短期存储，注意 XSS；生产可配合 HttpOnly Cookie。', 1, 20, NOW(6), NOW(6)),
-- 岗位3：关闭岗位仍可留题
(9, 3, '请描述等价类划分在登录接口测试中的用法。',
 '有效/无效手机号、空密码、超长输入等边界与异常类。', 1, 10, NOW(6), NOW(6));

INSERT INTO `candidate`
(`id`, `real_name`, `mobile`, `email`, `sex`, `password`, `job_id`, `remark`, `created_at`, `updated_at`) VALUES
(1, '张同学', '13800000001', 'zhang.demo@example.com', 1,
 '$2a$10$IYeUs2HP79nYxCfaO7qjxuM8RnUrTqWRled6F0HiXdpWYwtaI9AzO',
 1, '演示账号·后端方向', NOW(6), NOW(6)),
(2, '李同学', '13800000002', 'li.demo@example.com', 2,
 '$2a$10$IYeUs2HP79nYxCfaO7qjxuM8RnUrTqWRled6F0HiXdpWYwtaI9AzO',
 2, '演示账号·前端方向', NOW(6), NOW(6)),
(3, '王同学', '13800000003', NULL, 0,
 '$2a$10$IYeUs2HP79nYxCfaO7qjxuM8RnUrTqWRled6F0HiXdpWYwtaI9AzO',
 NULL, '演示账号·未绑定岗位', NOW(6), NOW(6));

INSERT INTO `resumes`
(`id`, `candidate_id`, `file_name`, `content_hash`, `storage_key`, `raw_text`, `file_size`,
 `content_type`, `analyze_status`, `analyze_error`, `created_at`, `updated_at`) VALUES
(1, 1, 'zhang-demo-resume.txt', 'demo-hash-zhang-001', 'demo/zhang-demo-resume.txt',
 '张同学｜本科｜计算机｜实习项目：校园二手交易后端（Spring Boot + MySQL）｜熟悉 Redis 基础缓存。',
 256, 'text/plain', 'COMPLETED', NULL, NOW(6), NOW(6));

INSERT INTO `resume_analyses`
(`id`, `resume_id`, `overall_score`, `summary`, `strengths`, `suggestions`, `analyzed_at`) VALUES
(1, 1, 78,
 '具备校招后端基础项目经历，技术栈与目标岗位匹配度中等偏上。',
 '项目描述较清晰；提到了 Spring Boot 与 MySQL。',
 '补充量化结果（QPS/延迟）；补充并发与排查案例。',
 NOW(6));

INSERT INTO `interview_sessions`
(`id`, `candidate_id`, `job_id`, `resume_id`, `status`, `total_questions`, `current_index`,
 `evaluate_status`, `evaluate_error`, `overall_score`, `overall_feedback`,
 `strengths_json`, `improvements_json`, `created_at`, `updated_at`, `completed_at`) VALUES
(1, 1, 1, 1, 'EVALUATED', 5, 4, 'COMPLETED', NULL, 81,
 '整体表达清楚，基础题回答到位；系统设计题可再结构化。',
 '["基础概念清晰","能结合项目举例"]',
 '["补充索引失效案例细节","回答可先结论后展开"]',
 DATE_SUB(NOW(6), INTERVAL 2 DAY), NOW(6), DATE_SUB(NOW(6), INTERVAL 2 DAY));

INSERT INTO `interview_answers`
(`id`, `session_id`, `question_index`, `question_text`, `source`, `reference_answer`,
 `answer_text`, `follow_up_question`, `follow_up_answer`, `created_at`, `updated_at`) VALUES
(1, 1, 0,
 '请解释 Java 中 == 与 equals 的区别，并举一个业务场景。', 'BANK',
 '==比较引用/基本类型值；equals可重写比较内容。',
 '==比的是引用地址，equals 可以比内容。比如比较两个订单号字符串我会用 equals。',
 '如果重写 equals 为什么通常也要重写 hashCode？',
 '为了让对象放进 HashMap/HashSet 时行为一致，否则可能找不到。',
 NOW(6), NOW(6)),
(2, 1, 1,
 'Spring Boot 自动配置的大致原理是什么？', 'BANK',
 '基于自动配置清单与条件装配。',
 '启动时会加载自动配置类，满足条件才创建 Bean，减少手写 XML。',
 NULL, NULL, NOW(6), NOW(6)),
(3, 1, 2,
 'MySQL 索引失效的常见原因有哪些？', 'BANK',
 '函数、隐式转换、不当模糊查询等。',
 '对字段做函数、类型不一致、like 前模糊可能导致走不了索引。',
 NULL, NULL, NOW(6), NOW(6)),
(4, 1, 3,
 'Redis 适合做什么？不适合做什么？请结合面试系统举例。', 'BANK',
 '缓存与会话；不替代主库。',
 '适合存登录 Token；面试最终评分还是应该落库。',
 NULL, NULL, NOW(6), NOW(6)),
(5, 1, 4,
 '如何设计一个「面试会话」表结构的核心字段？', 'BANK',
 '候选人岗位状态进度评估字段。',
 '需要候选人ID、岗位ID、状态、当前题号、总分和反馈；答题另表存。',
 NULL, NULL, NOW(6), NOW(6));

SET FOREIGN_KEY_CHECKS = 1;

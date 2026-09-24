# Phase 1 — 鉴权与管理域 CRUD

先读 `docs/agent-prompts/00-OVERVIEW.md`。假设 Phase 0 已完成。

## 参考
- 表概念：`D:\Java\Programfile\springAI\AI-interview\ai-interviewer\ai-interviewer.sql`
- 管理端页面：`D:\Java\Programfile\springAI\AI-interview\ai-interviewer\ai-interviewer-frontend\pages\a\` 下 candidate/job/settings
- 后端风格：`D:\Java\Programfile\springAI\interview-guide` 的 Controller/Service/Entity/DTO 规范

## 数据模型（精简，禁止 ai_src）
实现并建表（JPA Entity 驱动即可）：
1. `admin`：username、password（加密）、realName、timestamps
2. `candidate`：realName、mobile、email、sex、jobId（可选）、remark；**不要**身份证等重隐私字段
3. `job`：jobName、jobDesc、status、interviewerConfigId、prompt、timestamps
4. `question_lib`：jobId 或 interviewerConfigId 关联、question、referenceAnswer、isOn、sort；**禁止 ai_src**
5. `interviewer_config`：aiName、systemPrompt（评估/面试人设）；**不要形象图/视频**
6. 预留关联字段说明：后续 resume / interview_session 将挂 `candidate_id`、`job_id`（本 Phase 可只写注释或空关联）

## 后端任务
1. `modules.auth`：Admin 登录（Session 或 JWT/Token 存 Redis 任选一种简单方案）；候选人登录（手机号+密码或邀请码+密码，二选一写清）
2. `modules.admin` API（REST + DTO，不返回 Entity）：
   - 岗位 CRUD + 启停
   - 题库 CRUD + 启停（按岗位筛选）
   - 候选人 CRUD
   - 评估人设 CRUD
   - 面试记录列表接口可先返回空分页结构，字段预留
3. 统一 `Result<T>`、业务异常、错误码分域（admin 可用 9xxx 或单独域，与 guide 风格一致）
4. **不要**加限流注解/切面

## 前端任务（admin-web）
用 Vue3 + Element Plus **重写**（不要 httpVueLoader）：
- 登录页
- 侧栏菜单对齐参考：候选人列表/创建、面试记录、职位列表/创建、面试官设置、面试题库
- 完成上述 CRUD 页面并联调 API

## 验收标准
- [ ] 管理员可登录
- [ ] 能创建：1 个评估人设、1 个岗位、≥5 道题、1 个候选人
- [ ] 题库表/API/UI 均无视频字段
- [ ] 候选人无身份证字段
- [ ] 无限流相关代码

## 完成后
给出表清单、主要 API 列表、默认测试账号说明（写在 `.env.example` 注释，勿硬编码生产密码）。

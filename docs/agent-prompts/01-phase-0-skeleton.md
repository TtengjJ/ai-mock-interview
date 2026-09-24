# Phase 0 — 定范围与工程骨架

你是负责搭建合并版 AI 面试平台骨架的工程 Agent。先阅读 `docs/agent-prompts/00-OVERVIEW.md`，严格遵守非目标与技术栈。

## 参考路径
- 后端/AI 参考：`D:\Java\Programfile\springAI\interview-guide`
- 管理端菜单参考：`D:\Java\Programfile\springAI\AI-interview\ai-interviewer\ai-interviewer-frontend\pages\a\adminrouter.js`
- 目标仓库：`D:\Java\Programfile\springAI\tjxt\aI-interview-guide`

## 任务
1. 在目标仓库建立清晰工程结构（若已有空 Spring 脚手架，可改造而非推倒）：
   ```
   aI-interview-guide/
     docs/agent-prompts/   # 已有提示词
     backend/              # Spring Boot 后端
     admin-web/            # Vue3 + Element Plus 管理端
     user-web/             # Vue3 用户端
     docker-compose.yml    # mysql + redis + minio（先可只起依赖）
     README.md             # 一页说明：定位、非目标、如何启动
   ```
2. 后端：以 interview-guide 为蓝本初始化/迁移核心包结构：
   - 保留：`common`（异常、Result、AI Registry 简化版）、`infrastructure`（文件/Redis/存储）
   - 业务模块先建空包：`modules.auth`、`modules.admin`、`modules.resume`、`modules.interview`
   - **删除/不要引入**：`voiceinterview`、`knowledgebase`、`interviewschedule`、限流切面、Langfuse/OTLP
3. 配置：`application.yml` + `.env.example`（DB/Redis/MinIO/单一 LLM Provider）；不要提交真实密钥
4. 前端空壳：
   - `admin-web`：Vite + Vue3 + TS + Vue Router + Element Plus；布局 + 侧栏占位（候选人/职位/题库/评估设置/面试记录）
   - `user-web`：Vite + Vue3 + TS + Vue Router；布局 + 占位页（上传简历/简历列表/面试入口/面试历史）
5. 写一页 `docs/PRD.md`：角色、页面清单、非目标、本期范围

## 明确不要做
- 不要实现真实业务 API、不要接 LLM、不要做限流、不要 RAG/语音/日历/数字人

## 验收标准
- [ ] `backend` 能启动（可暂时无完整业务表）
- [ ] `admin-web` / `user-web` `npm run dev` 可打开占位页与路由
- [ ] README 写清非目标与目录
- [ ] 仓库中无 voice/kb/schedule/限流/Langfuse 相关代码路径

## 完成后
在回复中列出目录树、启动命令、以及进入 Phase 1 的前置说明。

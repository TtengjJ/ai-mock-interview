# AI Interview Guide — 合并平台总则（所有 Phase 必读）

## 目标仓库
- 本仓库：`D:\Java\Programfile\springAI\tjxt\aI-interview-guide`
- 参考（只读借鉴，勿直接大面积复制无关代码）：
  - 用户端/AI 深度：`D:\Java\Programfile\springAI\interview-guide`
  - 管理端信息架构：`D:\Java\Programfile\springAI\AI-interview\ai-interviewer`

## 产品定位
双端 Web 中复杂度 AI 面试平台：
- **管理端**：配置岗位、题库、候选人、评估人设；查看面试记录
- **用户端**：上传简历并 AI 评分；按岗位做**文字**模拟面试并查看评估
- **不做**移动端 uni-app；面试形态为文字，不是数字人视频

## 硬性非目标（禁止实现）
1. 数字人预录视频 / `ai_src` / 数字人形象媒资链路
2. 面试日程 / 日历模块
3. 知识库 RAG / pgvector 向量检索 / SSE 知识库问答
4. Langfuse / 复杂 OTLP 可观测链路
5. 语音面试（WebSocket / ASR / TTS）
6. **限流**（不要 `@RateLimit`、不要 Redis Lua 限流）
7. 腾讯云 SMS、百度 ASR 等与上述砍掉能力相关的依赖

## 技术栈（强制）
| 层 | 选型 |
|----|------|
| 后端基座 | 以 interview-guide 架构为准：Spring Boot + Java 21 + Spring AI + JPA |
| DB | MySQL 8（本期不启用向量检索） |
| 缓存/异步 | Redis + Redis Stream（仅 2 管道：`resume:analyze`、`interview:evaluate`） |
| 对象存储 | MinIO / S3 兼容 |
| 前端 | **Vue 3 + Vite + TypeScript**；管理端 Element Plus；用户端 Vue3 |
| 工程结构 | `backend/` + `admin-web/` + `user-web/`（可共享类型，但两个 app） |

## 功能取舍（本期必做）
- Admin：岗位、题库（无视频字段）、候选人（精简字段）、评估人设（名称+prompt）、面试记录
- User：简历上传/解析/异步 AI 评分、文字面试（题库优先抽题，不足 LLM 补题）、1 轮追问、异步评估、历史查看
- 鉴权：Admin 密码登录；候选人简单账号/手机号+密码或邀请码（不要云短信）

## 降级规则
- Skill：岗位题库为主 + 至多 1 个「自由练习」Skill（可选）
- 追问：固定 1 轮
- PDF 导出：本期不做，页面查看即可
- 多 LLM Provider：固定单一 Provider
- **无限流**

## 编码约束
- 禁止 `RuntimeException` 业务抛出，用统一业务异常 + 错误码
- Controller 只做路由；禁止返回 Entity 给前端
- 事务内禁止调用 LLM / S3
- 2 空格缩进；禁止通配符 import
- 前端整体 Vue 重写：不要复用 AI-interview 的 httpVueLoader 脚手架；不要保留 React

## 执行方式
按 `01`→`05` 顺序执行；每个 Phase 提示词自包含。完成一个 Phase 后对照其「验收标准」再进入下一 Phase。

## 提示词索引
| 文件 | Phase |
|------|-------|
| `00-OVERVIEW.md` | 总则（每个 Phase 必读） |
| `01-phase-0-skeleton.md` | 定范围与工程骨架 |
| `02-phase-1-admin.md` | 鉴权与管理域 CRUD |
| `03-phase-2-resume.md` | 用户端简历能力 |
| `04-phase-3-interview.md` | 文字面试主链路 |
| `05-phase-4-polish.md` | 打磨与工程化 |

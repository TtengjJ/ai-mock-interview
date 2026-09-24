# Phase 2 — 用户端简历能力

先读 `docs/agent-prompts/00-OVERVIEW.md`。假设 Phase 1 完成。

## 参考（只借鉴实现思路）
- `D:\Java\Programfile\springAI\interview-guide` 的 `modules/resume`、Analyze Stream Producer/Consumer、文件解析与存储
- 前端页面能力参考（需 Vue 重写）：Upload / History / ResumeDetail

## 后端任务
1. 迁移/实现简历模块：
   - 上传（校验类型大小）→ 解析（Tika）→ 存对象存储 → 落库
   - 内容哈希去重（可选但推荐）
   - Redis Stream 管道 **`resume:analyze`**：异步 AI 评分；失败重试最多 3 次；实体已删则 ACK 丢弃
2. 简历与 `candidate_id` 绑定；列表/详情/分析状态（待分析/分析中/完成/失败）
3. 使用单一 LLM Provider + 结构化输出（可简化，但要有评分结果落库）
4. **不要** PDF 导出、不要限流、不要知识库

## 前端任务（user-web）
Vue3 重写并联调：
- 候选人登录
- 简历上传页
- 简历列表（展示分析状态，可轮询或手动刷新）
- 简历详情（展示评分与建议文本）

## 验收标准
- [ ] 候选人登录后可上传简历
- [ ] 列表状态从分析中变为完成
- [ ] 详情可见 AI 评分结果
- [ ] 仅存在 `resume:analyze` 与（若已预留）评估管道相关 Stream 常量；无 voice/kb stream
- [ ] 无限流

## 完成后
说明异步时序图（文字即可）、关键 API、以及简历如何在 Phase 3 被面试引用（`resumeId` 可选）。

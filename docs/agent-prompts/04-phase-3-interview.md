# Phase 3 — 文字面试主链路（核心）

先读 `docs/agent-prompts/00-OVERVIEW.md`。假设 Phase 1–2 完成。这是产品主闭环。

## 参考
- 出题/会话/评估：`D:\Java\Programfile\springAI\interview-guide` 的 `modules/interview` + `common/evaluation`（可简化）
- 管理端记录页：`D:\Java\Programfile\springAI\AI-interview\ai-interviewer` 的 interviewRecord 信息架构
- **出题策略（强制）**：岗位题库优先抽题；题量不足再用 LLM + 岗位 JD + 可选简历摘要补题
- 追问：固定 1 轮
- 可选：保留 1 个「自由练习」Skill；不要上 10 个 Skill 全家桶

## 后端任务
1. 面试会话：
   - 创建：`jobId` 必填，`resumeId` 可选，`candidateId` 从登录态取
   - 状态机：进行中 → 已结束 → 评估中 → 评估完成/失败
2. 出题服务：先查 `question_lib`（isOn=1）；不足再 LLM 补题；记录来源（BANK/LLM）便于调试
3. 答题提交；支持 1 次追问生成与作答
4. 结束后投递 Redis Stream **`interview:evaluate`**：异步评估并写入结果
5. 用户端：我的面试列表/详情
6. 管理端：面试记录列表/详情对接真实数据（候选人、岗位、得分、详情文本）
7. **不要**语音、日历、RAG、PDF、限流、数字人视频

## 前端任务
**user-web**
- 面试入口（选择岗位、可选关联简历）
- 答题页（题目、作答、追问）
- 面试历史与详情（含评估结果）

**admin-web**
- 面试记录列表与详情（只读）

## 验收标准
- [ ] 候选人完成一场文字面试全流程
- [ ] 出题优先来自题库（可用日志/字段证明）
- [ ] 结束后异步评估成功，用户端与管理端都能看到结果
- [ ] 无语音/RAG/日历/ai_src/限流

## 完成后
给出主时序（创建→出题→答题→追问→结束→评估）、API 清单、以及一场演示用数据脚本或手动步骤。

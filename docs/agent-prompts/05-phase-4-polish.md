# Phase 4 — 打磨与工程化

先读 `docs/agent-prompts/00-OVERVIEW.md`。假设 Phase 0–3 主链路已通。

## 任务
1. 统一错误码与前端错误提示；鉴权失效跳转登录
2. Docker Compose：Postgres + Redis + MinIO + backend（前端可用文档说明分别 dev 启动，或一并容器化）
3. README：快速开始、环境变量、默认账号、功能范围与**非目标**列表
4. `.env.example` 齐全；确保密钥不入库
5. 补关键测试（任选可落地的）：
   - 出题：题库足够时不调用 LLM 补题（可 mock）
   - 评估状态流转
   - Admin/Candidate 鉴权失败用例
6. 代码清扫：删除未使用的 voice/kb/schedule/限流/OTLP 残留；去掉调试用 System.out
7. **仍然不要**加限流、RAG、日历、Langfuse、语音、数字人、PDF（除非时间充裕且不破坏范围——默认不做 PDF）

## 验收标准
- [ ] 按 README，陌生人能在干净环境跑通：管理员配置 → 候选人简历+面试 → 两端看结果
- [ ] docker 依赖可一键起
- [ ] 非目标未回流
- [ ] 至少若干自动化测试可运行

## 完成后
输出：演示脚本（逐步点击路径）、已知限制、建议的二期项（RAG/日历/PDF/多 Provider——仅文档提及，不实现）。

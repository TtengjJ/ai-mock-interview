# AI Interview Guide

双端 Web 中复杂度 AI 面试平台（合并版）。

- **管理端**：岗位 / 题库 / 候选人 / 评估人设 / 面试记录
- **用户端**：简历上传与 AI 评分、文字模拟面试与评估查看
- **后端**：`backend/`（Spring Boot 4.0.1 + Java 21 + Spring AI + Maven）

## 目录结构

```
aI-interview-guide/
├── docs/
│   ├── agent-prompts/      # Phase 提示词
│   ├── PRD.md
│   └── DEMO.md             # 逐步演示脚本 / 已知限制 / 二期
├── backend/                # 主后端（Maven）
├── admin-web/              # Vue3 + Element Plus 管理端 :5173
├── user-web/               # Vue3 用户端 :5174
├── docker-compose.yml      # mysql + redis + minio（可选 backend profile）
├── docker/mysql/           # MySQL 初始化
├── docs/sql/demo-seed.sql  # 模糊演示数据
└── README.md
```

## 功能范围（本期）

| 端 | 能力 |
|----|------|
| 管理端 | 登录、岗位、题库、候选人、评估人设、面试记录 |
| 用户端 | 登录、简历上传/异步评分、文字面试、追问、历史评估 |
| 后端 | Token 鉴权、Redis Stream（简历分析 / 面试评估）、MinIO、单一 LLM Provider |

## 非目标（本期不做）

- 数字人 / `ai_src` / 形象媒资
- 面试日程 / 日历
- 知识库 RAG / pgvector
- Langfuse / OTLP
- 语音面试（WebSocket / ASR / TTS）
- 限流、SMS、百度 ASR、PDF 导出
- uni-app / React

## 快速开始

### 1. 一键起依赖

```bash
docker compose up -d
# 等价：docker compose up -d mysql redis minio createbuckets
# 首次启动会自动导入 docs/sql/demo-seed.sql
```

可选：连同后端镜像一起起（需本机 Docker 能构建，并设置 `AI_API_KEY`）：

```bash
# 在仓库根目录准备 .env（可从 backend/.env.example 复制）后：
docker compose --profile full up -d --build
```

### 2. 后端（本地开发推荐）

```bash
cd backend
copy .env.example .env   # Windows；填写 AI_API_KEY
mvn spring-boot:run
```

- API：http://localhost:8080  
- Swagger：http://localhost:8080/swagger-ui.html  

运行测试：

```bash
mvn test
```

打包：

```bash
mvn -DskipTests package
```
### 3. 前端

```bash
cd admin-web && npm install && npm run dev   # http://localhost:5173
cd user-web && npm install && npm run dev    # http://localhost:5174
```

## 环境变量

见 `backend/.env.example`。关键项：

| 变量 | 说明 |
|------|------|
| `MYSQL_*` | MySQL 8（Docker 映射 **3307**，避免与本机 3306 冲突） |
| `REDIS_*` | Redis（compose 默认密码 `redis`） |
| `APP_STORAGE_*` / `MINIO_*` | MinIO |
| `AI_API_KEY` / `AI_BASE_URL` / `AI_MODEL` | DashScope OpenAI 兼容 |
| `CORS_ALLOWED_ORIGINS` | 默认 5173/5174 |
| `ADMIN_USERNAME` / `ADMIN_PASSWORD` | 种子管理员 |

**勿将真实 `.env` 提交入库**（已在 `.gitignore`）。

## 默认账号

| 角色 | 账号 | 密码 |
|------|------|------|
| 管理员（启动种子 / demo-seed） | `admin` | `admin123` |
| 候选人（demo-seed） | `13800000001` ~ `13800000003` | `candidate123` |

## 演示脚本

详见 [`docs/DEMO.md`](docs/DEMO.md)（逐步点击路径、已知限制、二期建议）。

简版：

1. 管理端登录 → 评估人设 → 岗位 → ≥5 道题 → 候选人  
2. 用户端登录 → 上传简历（可选）→ 开始面试 → 答题/追问 → 结束评估  
3. 两端查看面试记录与评分  

出题：`BANK` 优先，不足 `LLM` 补题。评估：`interview:evaluate:stream` 异步。

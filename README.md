# 团队 TodoList 甘特图

这是一个面向 5 人内部团队的任务协作应用：Vue 前端通过甘特图汇总任务，Spring Boot API 负责登录、分类、跟进人、权限和乐观锁。生产环境部署到华为 CCE，同一命名空间内仅通过内网入口访问。

## 当前工程

```text
frontend/       Vue 3 + Vite + Pinia + Element Plus
backend/        Java 17 + Spring Boot + Maven + Flyway
deploy/cce/     CCE 内网部署清单
旧 React/Express 原型已移出当前仓库，不参与构建
```

## 核心能力

- HttpOnly Cookie 登录，默认创建 1 位管理员和 4 位成员
- 管理员维护用户与任务分类
- 任务包含分类、开始日期、截止日期、跟进人、状态和进度
- 普通成员只能修改自己负责的任务；管理员可管理全部任务
- 任务更新使用版本号，冲突时返回 `409 Conflict`
- 甘特图按时间轴展示全部任务，支持分类、跟进人和状态筛选
- 无实时推送、离线编辑或拖动改期；可手动刷新

## 本地开发

需要 Node.js 20+、Java 17+、Maven 3.9+ 和 Docker Compose。

启动前端开发服务器：

```powershell
cd frontend
npm install
npm run dev
```

启动 Java API：

```powershell
cd backend
mvn spring-boot:run
```

前端 Vite 会将 `/api` 代理到本机 `http://localhost:8080`。首次启动后默认管理员为 `admin`，本地默认密码为 `ChangeMe123!`；任何非本地环境必须通过环境变量或 Secret 替换初始化密码和 JWT 密钥。

使用 Docker Compose 启动完整本地环境：

```powershell
docker compose up --build
```

本地入口为 `http://localhost:8081`。前端 Node 静态服务会把 `/api` 代理到后端容器；生产 CCE 环境由内部 Ingress 直接按路径路由。

## 数据库迁移

Flyway Java migration 会创建 `users`、`task_categories` 和扩展后的 `tasks` 表。对于旧项目的任务表：

- `startTime` 迁移为 `start_date`
- `endTime` 迁移为 `deadline`
- 原有负责人保存到 `legacy_assignee` 并尝试匹配初始化用户
- 原任务归入“未分类”
- 原有任务 ID、状态和进度尽量保留

CCE 中先执行 `deploy/cce/migration-job.yaml`，确认 Job 完成后再部署 API 和前端。

## CCE 内网部署

部署清单位于 [deploy/cce](deploy/cce/README.md)。所有 Service 都是 `ClusterIP`，仅允许由绑定内网 ELB 的 Ingress Class 访问。

需要在部署前替换：

- TDSQL 地址、库名和账号
- SWR 镜像地址与版本
- 内网域名 `todo.intra.example.com`
- `internal-nginx` 为实际的 CCE 内网 Ingress Class
- `secret.example.yaml` 中的数据库密码、JWT 密钥和初始化密码

如果内部入口启用 HTTPS，设置 `APP_AUTH_SECURE_COOKIE=true`。

## 交付文档

- [项目交付总览](docs/00-项目交付总览.md)
- [普通用户使用手册](docs/01-普通用户使用手册.md)
- [管理员使用与权限说明](docs/管理员使用与权限说明.md)
- [CCE 部署及迭代与 TDSQL 连接说明书](docs/CCE_TDSQL_部署及迭代说明书.md)
- [测试与验收报告](docs/04-测试与验收报告.md)
- [运维手册](docs/05-运维手册.md)
- [数据迁移与备份恢复说明](docs/06-数据迁移与备份恢复说明.md)
- [版本说明与已知限制](docs/07-版本说明与已知限制.md)
- [技术交接文档](docs/08-技术交接文档.md)

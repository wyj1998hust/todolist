# TodoList 前后端 CCE 部署与 TDSQL 连接说明书

> 适用版本：当前仓库中的 Vue 3 前端、Spring Boot 后端。  
> 目标环境：华为 CCE，同一命名空间，前后端通过内网 Ingress 访问，数据库使用外部 TDSQL。

## 1. 部署后的架构

~~~text
企业内网用户
      |
      v
内网 DNS：todo.intra.example.com
      |
      v
CCE 内网 ELB + Ingress
      |  /                         |  /api
      v                            v
todo-frontend:8080             todo-backend:8080
      |                            |
      +------ 同一 CCE 命名空间 ------+
                                   |
                                   v
                         外部 TDSQL MySQL 兼容地址
~~~

当前清单会创建：

- todo-frontend：Vue 构建产物，由 Node 静态服务器提供，ClusterIP Service。
- todo-backend：Spring Boot API，默认 2 个副本，ClusterIP Service。
- todo-migration：一次性 Flyway 数据库迁移 Job。
- todo-internal：示例命名空间。
- todo-internal Ingress：仅绑定 CCE 内网 ELB，不应分配公网 IP。

## 2. 部署前准备

需要准备以下信息和权限：

| 项目 | 示例 | 说明 |
|---|---|---|
| CCE 集群上下文 | my-cce-context | kubectl config current-context 能切换到目标集群 |
| 命名空间 | todo-internal | 可沿用清单中的名称，也可以统一替换 |
| TDSQL 地址 | tdsql-prod.example.internal | 必须是 CCE Pod 可访问的地址，不是本机地址 |
| TDSQL 端口 | 3306 | 以 TDSQL 实际端口为准 |
| 数据库名 | todolist_db | 需要提前创建 |
| 数据库账号 | todo_app | 需要具备首次 Flyway 迁移所需的 DDL 权限 |
| SWR 镜像地址 | swr.cn-xxx.myhuaweicloud.com/team-todo | CCE 节点必须能拉取 |
| 内网域名 | todo.intra.example.com | 解析到内网 Ingress ELB |
| Ingress Class | internal-nginx | 必须对应 CCE 内网 ELB，实际名称以集群为准 |

还需要安装并登录：

- Docker Desktop（Linux 容器模式）。
- kubectl，并配置 CCE 集群的 kubeconfig。
- 能访问目标 SWR 的 Docker 登录凭据。
- TDSQL 管理权限或 DBA 协助。
- 企业内网 DNS、网络安全组和 TDSQL 白名单配置权限。

## 3. 创建并放通 TDSQL

### 3.1 创建数据库和应用账号

请由 DBA 在 TDSQL 控制台或 SQL 客户端执行。以下 SQL 仅为 MySQL 兼容示例，账号名、密码、字符集和权限应按企业规范调整：

~~~sql
CREATE DATABASE IF NOT EXISTS todolist_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

CREATE USER 'todo_app'@'%' IDENTIFIED BY '替换为高强度数据库密码';

GRANT ALL PRIVILEGES ON todolist_db.* TO 'todo_app'@'%';
FLUSH PRIVILEGES;
~~~

当前项目使用同一个数据库账号执行 Flyway 迁移和运行时读写，因此首次迁移阶段需要 CREATE、ALTER、INDEX 等权限。若生产安全规范要求运行账号不能执行 DDL，可以另建一个仅供迁移 Job 使用的账号，并分别改造 migration-job.yaml 和后端 Deployment 的 Secret；不要直接把运行账号权限降低后再执行首次迁移。

### 3.2 配置 TDSQL 网络访问

在 CCE 和 TDSQL 之间确认：

1. CCE 节点或 Pod 出方向可以到达 TDSQL 私网地址和端口。
2. TDSQL 白名单加入 CCE 实际出网网段、NAT 网关地址或 Pod 网段，具体以华为云网络架构为准。
3. 安全组允许到 TDSQL 端口的 TCP 流量。
4. CCE 内的 DNS 能解析 TDSQL 主机名。
5. 若 TDSQL 强制 SSL，配置 DB_SSL=true；若不强制 SSL，也建议生产环境使用 SSL。

不要在配置中使用 localhost、127.0.0.1 或 Docker Compose 中的 db，这些地址只适用于本地环境。

## 4. 检查后端的 TDSQL 连接配置

后端配置文件是 backend/src/main/resources/application.yml。当前代码已经使用环境变量连接 MySQL 兼容数据库，通常不需要改 Java 代码或把生产密码写入此文件：

~~~yaml
spring:
  datasource:
    url: jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME:todolist_db}?useUnicode=true&characterEncoding=utf8&useSSL=${DB_SSL:false}&serverTimezone=UTC
    username: ${DB_USER:root}
    password: ${DB_PASSWORD:rootpassword}
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: validate
  flyway:
    enabled: ${SPRING_FLYWAY_ENABLED:true}
~~~

部署时由 CCE ConfigMap/Secret 注入：

| 环境变量 | 来源 | 生产值 |
|---|---|---|
| DB_HOST | ConfigMap | TDSQL 私网域名或 IP |
| DB_PORT | ConfigMap | TDSQL 实际端口 |
| DB_NAME | ConfigMap | todolist_db 或实际库名 |
| DB_USER | ConfigMap | TDSQL 应用账号 |
| DB_SSL | ConfigMap | true 或 false |
| DB_PASSWORD | Secret | TDSQL 密码 |
| SPRING_FLYWAY_ENABLED | ConfigMap | 后端 Deployment 为 false |

ddl-auto 必须保持为 validate，表结构由 Flyway 管理，不要改成 create、update 或 create-drop。

## 5. 修改 CCE 配置文件

以下文件都在 deploy/cce/ 下。建议先复制一份部署环境目录或使用 Git 分支保存本次版本。

### 5.1 修改 namespace.yaml

如果使用默认命名空间名，不需要修改。若企业要求使用其他命名空间，例如 team-todo-prod，将文件中的 metadata.name 替换为实际名称，并同步修改 configmap.yaml、secret.example.yaml、backend.yaml、frontend.yaml、migration-job.yaml、ingress-internal.yaml 和 kustomization.yaml 中的 namespace。

### 5.2 修改 configmap.yaml

至少修改下面这些值：

~~~yaml
data:
  DB_HOST: tdsql-prod.example.internal
  DB_PORT: "3306"
  DB_NAME: todolist_db
  DB_USER: todo_app
  DB_SSL: "true"
  SPRING_FLYWAY_ENABLED: "false"
  APP_AUTH_COOKIE_NAME: TODO_SESSION
  APP_AUTH_SESSION_HOURS: "12"
  APP_AUTH_SECURE_COOKIE: "true"
  APP_BOOTSTRAP_ENABLED: "true"
  APP_BOOTSTRAP_ADMIN_USERNAME: admin
  APP_BOOTSTRAP_ADMIN_DISPLAY_NAME: 管理员
  APP_BOOTSTRAP_MEMBER_USERNAMES: member1,member2,member3,member4
~~~

说明：

- APP_AUTH_SECURE_COOKIE=true 只适用于 HTTPS 内网入口；如果内网入口暂时是 HTTP，应设为 false，否则浏览器不会发送登录 Cookie。
- APP_BOOTSTRAP_ENABLED=true 用于首次启动自动创建 admin 和 4 个成员账号。
- 后续升级仍可保持为 true，初始化器只会创建不存在的账号，不会覆盖已有账号密码。
- 首次登录后请立即在应用内修改密码。修改密码后，旧登录会话会失效。

### 5.3 创建 secret.yaml

不要直接修改并提交 secret.example.yaml。复制一份：

~~~powershell
Copy-Item .\deploy\cce\secret.example.yaml .\deploy\cce\secret.yaml
~~~

编辑 deploy/cce/secret.yaml，填入真实值：

~~~yaml
apiVersion: v1
kind: Secret
metadata:
  name: todo-secrets
  namespace: todo-internal
type: Opaque
stringData:
  DB_PASSWORD: 替换为TDSQL密码
  APP_AUTH_JWT_SECRET: 替换为随机且至少32字节的JWT密钥
  APP_BOOTSTRAP_ADMIN_PASSWORD: 替换为首次管理员密码
  APP_BOOTSTRAP_MEMBER_PASSWORD: 替换为首次成员密码
~~~

注意：

- 密码不能写入 application.yml、ConfigMap、Dockerfile 或前端代码。
- APP_AUTH_JWT_SECRET 在同一套环境的所有后端副本中必须相同，否则会出现登录后请求随机失效。
- secret.yaml 不能提交 Git、不能发给不需要访问生产密钥的人员；可在部署后从本地安全位置删除，并保留企业密钥管理系统中的副本。
- 已存在用户时，修改 APP_BOOTSTRAP_ADMIN_PASSWORD 不会重置该用户密码；管理员应使用应用内“修改密码”或用户管理功能处理。

### 5.4 修改三个镜像清单

在以下三个文件中，将示例镜像替换为同一个发布版本对应的实际镜像：

- deploy/cce/backend.yaml
- deploy/cce/frontend.yaml
- deploy/cce/migration-job.yaml

例如把：

~~~yaml
image: swr.example.com/todo/team-todo-backend:REPLACE_TAG
~~~

改为：

~~~yaml
image: swr.cn-xxx.myhuaweicloud.com/team-todo/team-todo-backend:20260801-001
~~~

前端同理：

~~~yaml
image: swr.cn-xxx.myhuaweicloud.com/team-todo/team-todo-frontend:20260801-001
~~~

migration-job.yaml 的后端镜像必须与即将部署的后端版本一致。前端 Deployment 中的 API_PROXY_TARGET 保持为 http://todo-backend:8080。浏览器请求使用相对路径 /api，CCE Ingress 会把它转发给后端 Service。

如果 SWR 仓库是私有的，还需要在同一命名空间创建镜像拉取 Secret，并在上述三个 Pod 模板的 spec 下增加：

~~~yaml
imagePullSecrets:
  - name: swr-pull-secret
~~~

镜像拉取 Secret 的具体创建方式按企业 SWR 凭据规范执行，不要把凭据写进 Deployment 文件。

### 5.5 修改 ingress-internal.yaml

将下面三项替换为实际值：

~~~yaml
metadata:
  annotations:
    kubernetes.io/ingress.class: internal-nginx
spec:
  ingressClassName: internal-nginx
  rules:
    - host: todo.intra.example.com
~~~

- internal-nginx 必须是 CCE 中绑定内网 ELB 的 Ingress Class。
- todo.intra.example.com 必须由企业内网 DNS 解析到该内网 ELB。
- 不要把 Ingress 改成公网 ELB，也不要为此应用配置公网 IP。

如果使用 HTTPS，在 spec 下增加企业内部证书对应的 TLS 配置，并确认 configmap.yaml 中 APP_AUTH_SECURE_COOKIE 为 true：

~~~yaml
  tls:
    - hosts:
        - todo.intra.example.com
      secretName: todo-internal-tls
~~~

## 6. 构建并推送前后端镜像

在 Windows PowerShell 中，从项目根目录 todolist 执行。以下命令中的仓库地址和版本号替换为实际值：

~~~powershell
cd F:\myProject\todolist

$REGISTRY = "swr.cn-xxx.myhuaweicloud.com/team-todo"
$TAG = "20260801-001"

docker login swr.cn-xxx.myhuaweicloud.com
docker build -t "$REGISTRY/team-todo-backend:$TAG" .\backend
docker build -t "$REGISTRY/team-todo-frontend:$TAG" .\frontend
docker push "$REGISTRY/team-todo-backend:$TAG"
docker push "$REGISTRY/team-todo-frontend:$TAG"
~~~

后端 backend/Dockerfile 会在镜像构建阶段使用 Maven 编译；前端 frontend/Dockerfile 会使用 npm ci 和 npm run build 构建 Vue 产物。CCE 节点架构必须与镜像架构匹配；如果集群是 ARM 节点，应按集群架构构建镜像。

推送完成后，可在 SWR 控制台确认两个镜像的同一版本标签都存在，再修改三个 CCE YAML 的 image 字段。

## 7. 按顺序部署到 CCE

### 7.1 检查 kubectl 连接

~~~powershell
kubectl config current-context
kubectl get nodes
kubectl get ingressclass
~~~

确认当前上下文是目标 CCE 集群，并确认实际内网 Ingress Class 名称。不要在错误集群执行后续命令。

### 7.2 创建基础资源和 Secret

~~~powershell
cd F:\myProject\todolist
kubectl apply -f .\deploy\cce\namespace.yaml
kubectl apply -f .\deploy\cce\configmap.yaml
kubectl apply -f .\deploy\cce\secret.yaml
~~~

如果命名空间不是 todo-internal，YAML 内的 namespace 必须保持一致。

### 7.3 先执行一次数据库迁移

当前 kustomization.yaml 没有自动包含 migration-job.yaml，这是为了避免每次普通发布都重复创建同名 Job。因此首次部署和数据库结构升级时要显式执行：

~~~powershell
kubectl apply -f .\deploy\cce\migration-job.yaml
kubectl -n todo-internal wait --for=condition=complete job/todo-migration --timeout=10m
kubectl -n todo-internal logs job/todo-migration
~~~

看到 Job Complete 后再继续。该 Job 会执行 Flyway：

- 创建或升级 users、task_categories、tasks。
- 增加用户会话版本字段。
- 兼容旧任务表的 startTime、endTime、负责人、状态和进度迁移。
- 创建默认“未分类”分类。

首次生产迁移前必须先备份 TDSQL 数据库，并确认旧应用已经停止写入，避免迁移期间发生结构或数据竞争。

### 7.4 部署后端、前端和 Ingress

~~~powershell
kubectl apply -f .\deploy\cce\backend.yaml
kubectl apply -f .\deploy\cce\frontend.yaml
kubectl apply -f .\deploy\cce\ingress-internal.yaml

kubectl -n todo-internal rollout status deployment/todo-backend --timeout=10m
kubectl -n todo-internal rollout status deployment/todo-frontend --timeout=10m
kubectl -n todo-internal get pods,svc,ingress
~~~

后端 readiness 检查为 /actuator/health/readiness，前端 readiness 检查为 /health。只有 Pod 通过健康检查后，Service 才会接收流量。

## 8. 部署验证

### 8.1 检查应用和数据库连接

在企业内网、且能解析内部域名的机器上执行：

~~~powershell
curl.exe -i http://todo.intra.example.com/health
curl.exe -i http://todo.intra.example.com/api/health
~~~

两个接口应返回 HTTP 200 和 {"status":"UP"}。HTTPS 环境把 URL 改为 https://。

如果健康接口成功，再打开 http(s)://todo.intra.example.com/，使用 admin 和初始化密码登录，并立即修改密码。

### 8.2 检查日志和事件

~~~powershell
kubectl -n todo-internal get pods
kubectl -n todo-internal logs deployment/todo-backend --tail=200
kubectl -n todo-internal logs deployment/todo-frontend --tail=100
kubectl -n todo-internal describe pod -l app.kubernetes.io/name=todo-backend
kubectl -n todo-internal describe ingress todo-internal
~~~

日志中不应出现数据库连接失败、Flyway 失败、镜像拉取失败或健康检查失败。

### 8.3 功能验收

至少验证：

1. 管理员登录、退出、修改密码。
2. 4 个成员可以同时登录并查询任务。
3. 普通成员新增任务时负责人只能是自己。
4. 普通成员不能编辑他人任务；管理员可以管理全部任务。
5. 新增分类、启用/停用分类、颜色和甘特图显示正常。
6. 两个用户编辑同一任务时，后提交者收到 409 Conflict。
7. 关闭公网访问路径后，从公网无法访问域名或 Service。

## 9. 版本发布、数据库迁移和回滚

### 发布新版本

1. 生成新的不可变镜像标签，例如 20260801-002。
2. 构建并推送前后端两个镜像。
3. 修改 backend.yaml、frontend.yaml 和 migration-job.yaml 中的标签。
4. 若包含数据库变更，先备份 TDSQL，再删除旧的已完成迁移 Job 并重新创建新 Job：

~~~powershell
kubectl -n todo-internal delete job todo-migration
kubectl apply -f .\deploy\cce\migration-job.yaml
kubectl -n todo-internal wait --for=condition=complete job/todo-migration --timeout=10m
~~~

5. 迁移成功后重新 apply 后端和前端清单，观察滚动更新。

### 回滚

~~~powershell
kubectl -n todo-internal rollout undo deployment/todo-backend
kubectl -n todo-internal rollout undo deployment/todo-frontend
kubectl -n todo-internal rollout status deployment/todo-backend
kubectl -n todo-internal rollout status deployment/todo-frontend
~~~

应用可以回滚镜像，但 Flyway 数据库迁移默认是向前的，不能只回滚应用而假定数据库也会自动回退。数据库结构变更必须使用备份、兼容性迁移或 DBA 制定的回退方案。

## 10. 常见问题

| 现象 | 优先检查 |
|---|---|
| Migration Job Access denied | DB_HOST、DB_PORT、DB_NAME、账号密码、TDSQL 白名单和账号 DDL 权限 |
| Communications link failure | CCE 到 TDSQL 的路由、安全组、端口、DNS 和 TDSQL 私网地址 |
| SSL 握手失败 | DB_SSL 与 TDSQL SSL 策略是否一致；按 DBA 提供的证书参数调整 JDBC URL |
| Pod ImagePullBackOff | SWR 镜像地址/标签、仓库权限、imagePullSecrets 和节点网络 |
| 登录后马上未登录 | HTTPS 时 APP_AUTH_SECURE_COOKIE 必须为 true；HTTP 时必须为 false；检查前后端是否使用同一 JWT Secret |
| Ingress 404 | Host、Ingress Class、路径 /api、内网 DNS 和 ELB 监听配置 |
| 前端页面能打开但 API 失败 | 检查 todo-backend Service、后端 Pod 健康状态及 Ingress /api 路由 |
| 修改初始化密码无效 | 数据库中用户已经存在，Bootstrap 不会覆盖已有密码，应使用应用内密码修改/重置功能 |

## 11. Git 和敏感文件检查

应提交的部署文档为：

~~~text
docs/CCE_TDSQL_部署及迭代说明书.md
~~~

它不属于当前 .gitignore 的忽略规则。提交前可在 todolist 目录执行：

~~~powershell
git check-ignore -v .\docs\CCE_TDSQL_部署及迭代说明书.md
git status --short .\docs\CCE_TDSQL_部署及迭代说明书.md
~~~

第一条命令没有输出，表示文档没有被忽略；第二条命令应显示新增文件。真实的 deploy/cce/secret.yaml 不应提交，即使当前 Git 忽略规则没有自动拦截，也必须在 git add 时排除它。

部署清单中的 secret.example.yaml 只保留占位符，适合随代码交付；真实密钥应通过企业密钥管理流程或安全渠道提供。

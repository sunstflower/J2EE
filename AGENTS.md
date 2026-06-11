# AGENTS

本文件用于记录项目协作规范，以及每次会话对项目造成的修改。

## 1. 协作规则

- 每次会话结束后，必须更新本文件中的“变更记录”部分。
- 记录内容应包括日期、会话目标、修改文件、主要变更说明。
- 如果本次会话没有修改代码，只修改文档，也需要记录。
- 如果只是分析、未产生任何文件变更，应明确写明“无文件修改”。
- 不应删除历史记录；如需修正，追加说明即可。

## 2. 文档维护范围

后续会话至少检查并按需更新以下文档：

- `AGENTS.md`
- `docs/architecture.md`
- `docs/frontend-scope.md`

## 3. 变更记录格式

建议使用以下模板：

```text
### YYYY-MM-DD HH:MM
- 会话目标：
- 修改文件：
- 主要变更：
- 备注：
```

## 4. 变更记录

### 2026-06-11 15:18
- 会话目标：继续排查库存页异常，并修复药品名称中文乱码
- 修改文件：
  - `AGENTS.md`
- 主要变更：
  - 确认前端库存页真实依赖接口为 `GET /api/inventories` 与 `GET /api/warnings/low-stock`，不存在 `GET /api/inventory/overview` 路由
  - 复验库存列表和低库存预警接口，确认接口本身返回 `200`
  - 定位到库存页中文乱码来自现有 `drug` 表演示数据历史写脏，而非接口路由或前端渲染问题
  - 在运行中的 MySQL 数据内修正 12 条演示药品的中文名称等关键文本字段，恢复库存页和低库存提醒中的中文显示
- 备注：
  - 本次未修改业务代码；库存页相关问题实质为运行态数据修复
  - 已复核 `docs/architecture.md` 与 `docs/frontend-scope.md`，无需改动现有设计文档

### 2026-06-11 14:53
- 会话目标：修复容器环境登录接口返回 `500` 的问题
- 修改文件：
  - `backend/src/main/resources/application.yml`
  - `docker-compose.yml`
  - `AGENTS.md`
- 主要变更：
  - 定位到 MySQL JDBC 连接参数 `characterEncoding=utf8mb4` 与 Java 编码名不兼容，导致后端首次获取数据库连接时报 `Unsupported character encoding 'utf8mb4'`
  - 将后端默认数据源 URL 和 Compose 注入的 `SPRING_DATASOURCE_URL` 统一改为 `characterEncoding=UTF-8`
- 备注：
  - 已复核 `docs/architecture.md` 与 `docs/frontend-scope.md`，本次仅涉及运行配置修复，无需改动现有设计文档

### 2026-06-11 15:02
- 会话目标：修复登录响应中的中文用户名乱码
- 修改文件：
  - `docker/mysql/init/002_schema.sql`
  - `docker/mysql/init/003_seed.sql`
  - `AGENTS.md`
- 主要变更：
  - 确认乱码根因不是前端展示，而是历史错误 JDBC 编码配置曾将演示账号姓名以错误字节写入现有 MySQL 数据卷
  - 在运行中的 `drug_management.user_account` 表内将 `1001`、`2001` 两条演示账号的 `user_name` 修正回正确 UTF-8 字节
  - 为 `002_schema.sql` 与 `003_seed.sql` 增加 `SET NAMES utf8mb4;`，降低重新初始化数据卷时再次写脏中文数据的风险
  - 复验登录接口返回字节与数据库十六进制，确认中文链路恢复正常
- 备注：
  - 种子文件 `docker/mysql/init/003_seed.sql` 本身无乱码；若后续重新初始化数据卷，当前种子数据可直接写入正确中文
  - 已复核 `docs/architecture.md` 与 `docs/frontend-scope.md`，本次仅修复运行态数据，无需改动现有设计文档

### 2026-06-11 14:35
- 会话目标：降低 `docker compose up --build -d` 首次构建阶段的依赖下载耗时，并补充镜像源覆盖说明
- 修改文件：
  - `backend/Dockerfile`
  - `frontend/Dockerfile`
  - `docker-compose.yml`
  - `README.md`
  - `AGENTS.md`
- 主要变更：
  - 为后端 Maven 构建增加可配置的 `MAVEN_REPO_URL`，默认使用阿里云 Maven 镜像
  - 为前端 npm 构建增加可配置的 `NPM_REGISTRY`，默认使用 npmmirror 源
  - 在 `docker-compose.yml` 中补充对应构建参数，并保留环境变量覆盖能力
  - 在 README 中补充首次冷构建耗时原因，以及覆盖 Maven/npm 镜像源的执行示例
- 备注：
  - 已复核 `docs/architecture.md` 与 `docs/frontend-scope.md`，本次仅涉及构建链路与使用文档，无需改动现有设计文档

### 2026-06-11 14:32
- 会话目标：更新 README 文档，使项目使用方法与当前实际启动链路保持一致
- 修改文件：
  - `README.md`
  - `AGENTS.md`
- 主要变更：
  - 重写 README 中的快速开始、容器启动、本地开发、测试与排查章节
  - 补充当前登录方式为 `userId + password`，并明确医生与药师两个演示账号
  - 补充容器首次构建会在镜像内执行 `mvn clean package` 与 `npm ci`、可能长时间下载依赖的说明
  - 更新 Docker Compose、本地前后端联调、健康检查与常见故障排查命令
- 备注：
  - 已复核 `docs/architecture.md` 与 `docs/frontend-scope.md`，本次仅更新使用文档，无需改动现有设计文档
### 2026-06-11 14:11
- 会话目标：修复 `docker compose up --build -d` 后端仍引用旧产物、导致容器环境登录接口异常的问题
- 修改文件：
  - `backend/Dockerfile`
  - `backend/.dockerignore`
  - `frontend/Dockerfile`
  - `AGENTS.md`
- 主要变更：
  - 将后端镜像改回真正的多阶段构建，改为在容器内执行 `mvn clean package -DskipTests`
  - 移除对宿主机 `backend/target/*.jar` 的构建依赖，避免 `docker compose up --build` 误打包本地过期产物
  - 为后端新增 `.dockerignore`，收紧构建上下文，避免将本地 `target` 等无关内容送入镜像构建
  - 将前端镜像安装步骤改为 `npm ci --no-audit --no-fund`，并为 npm / Maven 启用 BuildKit 缓存挂载，降低首次构建后的重复下载开销
  - 复核 `docs/architecture.md` 与 `docs/frontend-scope.md`，确认本次问题仅涉及容器构建链路，无需改动现有文档内容
- 备注：
  - 首次重建后端镜像会额外下载 Maven 依赖，耗时显著高于前端镜像构建
  - 首次重建前端镜像同样需要重新下载 npm 依赖，`docker build`/`docker compose up --build` 在依赖安装阶段可能长时间无新增输出
  - 运行态异常表现为 `POST /api/auth/login` 返回 `500`，当前旧容器仍为 2 天前镜像，需等待新镜像完成替换后继续复验
### 2026-06-11 13:48
- 会话目标：将项目启动、测试、构建与容器运行命令集中整理到 README 文档
- 修改文件：
  - `README.md`
  - `AGENTS.md`
- 主要变更：
  - 新增根目录 `README.md`，补充项目简介、环境要求与目录说明
  - 汇总后端 Maven、前端 Vite/Vitest、本地联调、Docker Compose 启动与停止命令
  - 增加最小演示路径与常见容器日志排查命令，便于直接启动和验收
- 备注：
  - 本次仅补充文档，不修改业务代码

### 2026-06-08 15:45
- 会话目标：按演示目标收缩前端功能范围，形成新的页面与交互设计文档
- 修改文件：
  - `AGENTS.md`
  - `docs/architecture.md`
  - `docs/frontend-scope.md`
- 主要变更：
  - 将前端展示范围收敛为“库存预览、开药、药物入库”三项核心功能
  - 明确库存预览页需要同时展示库存列表和低库存预警
  - 明确开药页中处方药只能由医生账号开出，药师账号不展示该能力
  - 补充新的前端页面结构、角色边界和演示落地原则
- 备注：
  - 本次仅修改文档，不修改业务代码

### 2026-06-08 16:00
- 会话目标：进行基础修改，并将 `frontend-scope.md` 中的收口要求落实到前端入口和 AGENTS 文档
- 修改文件：
  - `frontend/src/App.jsx`
  - `frontend/src/styles.css`
  - `frontend/src/pages/DashboardPage.jsx`
  - `AGENTS.md`
- 主要变更：
  - 补回前端缺失的 `App.jsx` 和 `styles.css`，恢复最小可运行入口
  - 将主页入口收敛为 `库存预览`、`开药`、`药物入库` 三项
  - 按 `frontend-scope.md` 约束，药师账号不再显示“开药”入口
  - 将本次基于 `frontend-scope.md` 的基础落地动作同步记录到 AGENTS 文档
- 备注：
  - 当前仅完成入口层和页面骨架收口，具体页面内容仍需继续按收口设计替换现有旧模块实现

### 2026-06-08 16:07
- 会话目标：修复当前不可运行状态，恢复最小可演示闭环的前后端、数据库初始化和测试验证
- 修改文件：
  - `backend/src/main/java/com/example/drugmanagement/common/auth/AuthSessionService.java`
  - `backend/src/main/java/com/example/drugmanagement/common/config/WebMvcConfig.java`
  - `backend/src/main/java/com/example/drugmanagement/controller/AuthController.java`
  - `backend/src/main/java/com/example/drugmanagement/dto/inventory/CreateInventoryCheckRequest.java`
  - `backend/src/main/java/com/example/drugmanagement/dto/inventory/CreateInventoryInboundRequest.java`
  - `backend/src/main/java/com/example/drugmanagement/dto/inventory/CreateInventoryOutboundRequest.java`
  - `backend/src/main/java/com/example/drugmanagement/dto/prescription/CreatePrescriptionRequest.java`
  - `backend/src/main/java/com/example/drugmanagement/dto/prescription/PrescriptionAuditRequest.java`
  - `backend/src/main/java/com/example/drugmanagement/dto/prescription/PrescriptionDispenseRequest.java`
  - `backend/src/main/java/com/example/drugmanagement/dto/prescription/PrescriptionDoctorApprovalRequest.java`
  - `backend/src/main/java/com/example/drugmanagement/entity/UserAccount.java`
  - `backend/src/main/java/com/example/drugmanagement/mapper/UserAccountMapper.java`
  - `backend/src/main/java/com/example/drugmanagement/service/impl/InventoryServiceImpl.java`
  - `backend/src/main/java/com/example/drugmanagement/service/impl/PrescriptionServiceImpl.java`
  - `backend/src/main/resources/application.yml`
  - `backend/src/main/resources/mapper/PrescriptionItemMapper.xml`
  - `backend/src/main/resources/mapper/PrescriptionMapper.xml`
  - `backend/src/main/resources/mapper/UserAccountMapper.xml`
  - `backend/src/test/resources/application.yml`
  - `docker/mysql/init/002_schema.sql`
  - `docker/mysql/init/003_seed.sql`
  - `frontend/src/App.jsx`
  - `frontend/src/styles.css`
  - `frontend/src/pages/InboundPage.jsx`
  - `frontend/src/pages/InventoryOverviewPage.jsx`
  - `frontend/src/pages/PrescribePage.jsx`
  - `docs/testing.md`
  - `docs/deployment.md`
  - `AGENTS.md`
- 主要变更：
  - 恢复后端认证会话、CORS 配置、库存与处方缺失 DTO、库存服务与处方服务实现
  - 恢复 `user_account` 最小数据模型与 Mapper，避免 MyBatis 启动残缺
  - 补齐 MySQL 业务表初始化脚本和 12 条药品与库存演示数据
  - 将前端真实落地为 `库存预览`、`药物入库`、`开药` 三个页面，并加入医生角色路由限制
  - 修正测试与部署文档，使其与当前最小演示闭环一致
  - 完成 `backend:mvn clean test`、`frontend:npm run build`、`frontend:npm test` 验证
- 备注：
  - 当前项目已恢复到可构建、可测试、可继续联调的最小演示状态

### 2026-06-08 16:08
- 会话目标：补齐 Docker Compose 实跑所需配置，准备容器化验收
- 修改文件：
  - `backend/Dockerfile`
  - `docker-compose.yml`
  - `AGENTS.md`
- 主要变更：
  - 将后端镜像改为多阶段构建，容器内直接完成 Maven 打包
  - 新增根目录 `docker-compose.yml`，编排 MySQL、backend、frontend、nginx 四个服务
  - 将演示入口端口统一暴露为 `localhost:3000`
- 备注：
  - 下一步将直接执行 `docker compose up --build` 做实跑验收

### 2026-06-08 16:36
- 会话目标：修复 Docker 实跑中的登录失败问题并继续验收
- 修改文件：
  - `docker/mysql/init/002_schema.sql`
  - `docker/mysql/init/003_seed.sql`
  - `AGENTS.md`
- 主要变更：
  - 修正 `user_account` 表结构，补齐 `enabled`、审计字段和 `deleted`
  - 修正演示账号种子数据，使其与后端 `UserAccountMapper` 查询字段一致
- 备注：
  - 该修复需要重新初始化 MySQL 数据卷后再复验登录接口

### 2026-06-08 16:41
- 会话目标：修复容器化环境下库存页 `Failed to fetch` 问题，检查真实数据库连接
- 修改文件：
  - `frontend/src/api/client.js`
  - `backend/src/main/java/com/example/drugmanagement/common/auth/AuthInterceptor.java`
  - `AGENTS.md`
- 主要变更：
  - 修正前端 API 基地址逻辑，使 `VITE_API_BASE_URL` 为空字符串时走同源 `/api`
  - 放行后端 `OPTIONS` 预检请求，避免带鉴权头的跨域预检被误判为未授权
  - 复核容器内 MySQL 数据，确认 `drug` 与 `inventory` 均已有 12 条初始化数据
- 备注：
  - 本次问题根因不是数据库无数据，而是容器联调请求链路配置错误

### 2026-06-09 08:20
- 会话目标：修复当前接口中文乱码问题，统一后端与数据库字符集链路
- 修改文件：
  - `backend/pom.xml`
  - `backend/src/main/resources/application.yml`
  - `docker-compose.yml`
  - `AGENTS.md`
- 主要变更：
  - 为 Maven 明确声明 UTF-8 源码与输出编码
  - 为 Spring Boot 响应编码和 Servlet 编码统一指定 UTF-8
  - 将 JDBC 连接字符集升级为 `utf8mb4` 并补齐连接排序规则
  - 将 Docker Compose 中后端数据源连接参数同步为 `utf8mb4`
- 备注：
  - 本次修改不改变业务功能，仅修复中文展示质量

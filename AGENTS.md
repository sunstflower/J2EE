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

- `README.md`
- `AGENTS.md`
- `docs/architecture.md`

如果项目进入开发阶段，再按需扩展：

- Docker 使用说明
- 数据库初始化说明
- API 文档
- 测试说明
- 部署说明

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

### 2026-06-02 10:10
- 会话目标：初始化后端公共基础与数据库业务表，为后续模块开发做准备
- 修改文件：
  - `README.md`
  - `AGENTS.md`
  - `docs/architecture.md`
  - `backend/src/main/java/com/example/drugmanagement/common/config/MybatisConfig.java`
  - `backend/src/main/java/com/example/drugmanagement/common/dto/PageQuery.java`
  - `backend/src/main/java/com/example/drugmanagement/common/entity/BaseEntity.java`
  - `backend/src/main/java/com/example/drugmanagement/common/entity/AuditEntity.java`
  - `backend/src/main/java/com/example/drugmanagement/common/enums/DoctorApprovalStatus.java`
  - `backend/src/main/java/com/example/drugmanagement/common/enums/InventoryRecordType.java`
  - `backend/src/main/java/com/example/drugmanagement/common/enums/PrescriptionStatus.java`
  - `backend/src/main/java/com/example/drugmanagement/common/enums/RoleType.java`
  - `backend/src/main/java/com/example/drugmanagement/common/enums/WarningStatus.java`
  - `backend/src/main/java/com/example/drugmanagement/common/enums/WarningType.java`
  - `backend/src/main/java/com/example/drugmanagement/common/exception/BusinessException.java`
  - `backend/src/main/java/com/example/drugmanagement/common/exception/GlobalExceptionHandler.java`
  - `backend/src/main/java/com/example/drugmanagement/common/response/ApiResponse.java`
  - `backend/src/main/java/com/example/drugmanagement/common/response/PageResponse.java`
  - `backend/src/main/java/com/example/drugmanagement/common/response/ResponseCode.java`
  - `backend/src/main/java/com/example/drugmanagement/entity/Drug.java`
  - `backend/src/main/java/com/example/drugmanagement/entity/Inventory.java`
  - `backend/src/main/java/com/example/drugmanagement/entity/InventoryRecord.java`
  - `backend/src/main/java/com/example/drugmanagement/entity/Prescription.java`
  - `backend/src/main/java/com/example/drugmanagement/entity/PrescriptionItem.java`
  - `backend/src/main/java/com/example/drugmanagement/entity/WarningRecord.java`
  - `backend/src/main/resources/application.yml`
  - `backend/src/test/java/com/example/drugmanagement/common/ApiResponseTest.java`
  - `docker/mysql/init/002_schema.sql`
  - `docker/mysql/init/003_seed.sql`
- 主要变更：
  - 初始化后端统一错误码、分页对象、基础实体、业务枚举与 MyBatis 基础配置
  - 补充药品、库存、库存流水、处方、处方明细、预警记录等实体类骨架
  - 创建数据库业务表、索引、约束及示例种子数据脚本
  - 补充基础响应测试并验证后端测试通过
- 备注：
  - 当前已完成公共基础与表结构初始化，尚未实现具体业务 Mapper/Service/Controller

### 2026-06-02 09:31
- 会话目标：初始化项目工程骨架，为后续业务开发与测试落地做准备
- 修改文件：
  - `README.md`
  - `AGENTS.md`
  - `docs/architecture.md`
  - `.gitignore`
  - `docker-compose.yml`
  - `backend/pom.xml`
  - `backend/Dockerfile`
  - `backend/src/main/java/com/example/drugmanagement/DrugManagementApplication.java`
  - `backend/src/main/java/com/example/drugmanagement/controller/HealthController.java`
  - `backend/src/main/java/com/example/drugmanagement/common/response/ApiResponse.java`
  - `backend/src/main/java/com/example/drugmanagement/common/exception/BusinessException.java`
  - `backend/src/main/java/com/example/drugmanagement/common/exception/GlobalExceptionHandler.java`
  - `backend/src/main/resources/application.yml`
  - `backend/src/test/java/com/example/drugmanagement/DrugManagementApplicationTests.java`
  - `backend/src/test/java/com/example/drugmanagement/controller/HealthControllerTest.java`
  - `frontend/package.json`
  - `frontend/vite.config.js`
  - `frontend/index.html`
  - `frontend/Dockerfile`
  - `frontend/src/main.jsx`
  - `frontend/src/App.jsx`
  - `frontend/src/styles.css`
  - `frontend/src/pages/HomePage.jsx`
  - `frontend/src/pages/__tests__/App.test.jsx`
  - `frontend/src/test/setup/setupTests.js`
  - `docker/mysql/init/001_init.sql`
  - `docker/nginx/default.conf`
- 主要变更：
  - 初始化 Spring Boot + MyBatis 后端骨架、统一响应、异常处理与健康检查接口
  - 初始化 React + Vite 前端骨架、Vitest 测试配置与占位首页
  - 创建 Docker Compose、MySQL 初始化目录、Nginx 代理配置与前后端 Dockerfile
  - 同步更新项目文档，标记当前初始化状态与后续开发落点
- 备注：
  - 当前仅完成工程与测试骨架，尚未实现正式业务模块

### 2026-06-02 09:20
- 会话目标：细化项目文档，并补充后续单元测试与测试分层规划
- 修改文件：
  - `README.md`
  - `AGENTS.md`
  - `docs/architecture.md`
- 主要变更：
  - 细化开发阶段实施顺序，增加后端基础结构、建表脚本与前端落地顺序建议
  - 扩展后端与前端测试分层、命名规范、优先级与测试数据准备策略
  - 为库存、处方、审计链路补充更明确的业务校验与事务一致性要求
- 备注：
  - 当前仍为文档设计阶段，未编写业务代码

### 2026-06-02 09:11
- 会话目标：根据新的处方开具与授权规则更新项目文档
- 修改文件：
  - `README.md`
  - `AGENTS.md`
  - `docs/architecture.md`
- 主要变更：
  - 增加医生与药师两类角色说明及处方权限边界
  - 补充药师输入医生 ID 发起代开、医生登录确认授权的业务流程
  - 更新处方状态、接口草案、数据字段建议与测试场景
- 备注：
  - 当前仍为文档设计阶段，未编写业务代码
### 2026-06-02 08:43
- 会话目标：初始化“药物管理系统”项目文档，不编写业务代码
- 修改文件：
  - `README.md`
  - `AGENTS.md`
  - `docs/architecture.md`
- 主要变更：
  - 编写项目概述、技术栈、功能范围与分层架构要求
  - 定义 RESTful API 草案、核心数据表建议、测试范围与 Docker 规划
  - 建立 AGENTS 协作规则，并约定后续每次会话后追加修改记录
- 备注：
  - 当前仓库未初始化前后端工程，仅完成文档层设计

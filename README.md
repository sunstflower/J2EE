# 药物管理系统

一个基于 J2EE 技术栈落地的药物管理系统项目，当前已完成项目文档、前后端工程骨架，以及后端公共基础与数据库业务表初始化。

## 1. 项目目标

系统用于支撑药房日常药物管理，覆盖以下核心业务：

- 药物信息维护：新增、查询、修改、删除
- 药房库存管理：入库、扣减、盘点、库存变更记录
- 库存预警：低库存预警
- 效期管理：药品有效期跟踪、临期预警、过期标记
- 处方流程：模拟医生开具处方、处方审核、确认发药、库存扣减

## 2. 技术栈

- 后端：Spring Boot、MyBatis
- 前端：React、Vite、shadcn/ui
- 数据库：MySQL 8.x
- 容器化：Docker、Docker Compose
- API 风格：RESTful API
- 测试：
  - 后端单元测试：JUnit 5、Mockito、Spring Boot Test
  - 前端组件/页面测试：Vitest、React Testing Library

## 3. 项目规划

当前仓库建议按前后端分离方式组织：

```text
J2EE/
├── README.md
├── AGENTS.md
├── docs/
│   └── architecture.md
├── backend/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
├── frontend/
│   ├── Dockerfile
│   ├── package.json
│   └── src/
├── docker/
│   ├── mysql/
│   │   └── init/
│   └── nginx/
└── docker-compose.yml
```

说明：

- 当前已完成前后端工程骨架、测试目录与容器化基础文件初始化。
- 当前已完成数据库业务表 DDL、初始化种子数据与后端公共响应/异常/基础实体定义。
- 当前已完成药品模块 CRUD 的后端接口、服务、Mapper 与基础测试。
- 库存、预警、处方、认证授权与页面功能仍待后续开发。

## 4. 业务模块设计

### 4.0 用户与角色

为方便演示，系统当前只区分两类用户：

- 医生：可直接开具处方，并在系统中留下处方记录
- 药师：可审核处方、确认发药；也可代医生发起开方申请

处方开具规则：

- 医生登录后，可直接创建并提交处方
- 药师不能以自己名义直接开具处方
- 药师如需协助开方，必须输入医生 ID 发起代开申请
- 医生登录系统并明确同意后，该处方才可由该药师代为开具
- 无医生授权的药师代开行为必须被系统拒绝
- 所有开方行为都必须记录实际开方发起人、授权医生和操作时间

### 4.1 药物信息管理

主要字段建议：

- 药品编码 `drug_code`
- 药品名称 `drug_name`
- 通用名 `generic_name`
- 分类 `category`
- 规格 `specification`
- 单位 `unit`
- 生产厂家 `manufacturer`
- 批准文号 `approval_number`
- 采购价 `purchase_price`
- 销售价 `sale_price`
- 最低库存阈值 `low_stock_threshold`
- 是否启用 `enabled`

主要功能：

- 新增药品
- 查询药品列表
- 查看药品详情
- 更新药品信息
- 删除或逻辑停用药品

当前已实现的后端能力：

- `POST /api/drugs`：新增药品
- `GET /api/drugs`：分页查询药品，支持关键字、分类、启用状态筛选
- `GET /api/drugs/{id}`：查询药品详情
- `PUT /api/drugs/{id}`：更新药品信息
- `DELETE /api/drugs/{id}`：逻辑删除药品

当前已落地的校验规则：

- 药品编码不可重复
- 药品名称、单位、采购价、销售价、最低库存阈值为必填
- 采购价、销售价不得小于 0
- 最低库存阈值不得小于 0
- 删除操作采用逻辑删除

### 4.2 库存管理

主要功能：

- 药品入库
- 药品出库
- 库存盘点
- 按批次管理库存
- 记录库存流水

建议设计：

- 库存按“药品 + 批次号 + 有效期”维度管理
- 出库优先扣减最早到期批次

### 4.3 库存预警与效期管理

主要功能：

- 低库存预警
- 临期预警
- 过期药品标记
- 预警列表查询

建议规则：

- 低库存：当前可用库存 < 最低库存阈值
- 临期预警：距有效期不足 30 天
- 过期状态：当前日期 > 有效期

### 4.4 处方管理

处方流程：

1. 医生直接开方，或药师输入医生 ID 发起代开申请
2. 录入处方明细
3. 若为药师代开，等待医生登录系统确认授权
4. 处方提交进入审核环节
5. 药师审核处方
6. 确认发药
7. 发药成功后扣减库存并记录流水

建议状态：

- `DRAFT`：草稿
- `PENDING_DOCTOR_APPROVAL`：待医生确认代开
- `SUBMITTED`：已提交待审核
- `APPROVED`：审核通过待发药
- `REJECTED`：审核驳回
- `DISPENSED`：已发药
- `CANCELLED`：已取消

建议记录字段：

- `created_by_user_id`：创建人 ID
- `created_by_role`：创建人角色
- `doctor_id`：处方归属医生 ID
- `doctor_approval_status`：医生授权状态
- `doctor_approved_at`：医生授权时间
- `pharmacist_operator_id`：代开药师 ID，可空

## 5. 分层架构要求

后端采用经典分层结构：

- `controller`：接收 HTTP 请求，参数校验，统一响应
- `service`：业务编排、事务控制、规则校验
- `dao`：数据访问层，对接 MyBatis Mapper
- `entity`：数据库实体对象
- `dto/vo`：接口入参与出参对象
- `mapper`：MyBatis 映射接口和 XML

推荐结构：

```text
backend/src/main/java/com/example/drugmanagement/
├── controller/
├── service/
├── service/impl/
├── dao/
├── dto/
├── vo/
├── entity/
├── mapper/
├── common/
│   ├── config/
│   ├── exception/
│   ├── response/
│   └── util/
└── DrugManagementApplication.java
```

## 6. RESTful API 设计建议

### 6.1 药品管理

- `GET /api/drugs`：分页查询药品
- `GET /api/drugs/{id}`：查询药品详情
- `POST /api/drugs`：新增药品
- `PUT /api/drugs/{id}`：更新药品
- `DELETE /api/drugs/{id}`：删除药品

### 6.2 库存管理

- `GET /api/inventories`：查询库存列表
- `GET /api/inventories/{drugId}`：查询某药品库存明细
- `POST /api/inventories/inbound`：库存入库
- `POST /api/inventories/outbound`：库存出库
- `POST /api/inventories/check`：发起库存盘点
- `GET /api/inventory-records`：查询库存流水

### 6.3 预警管理

- `GET /api/warnings/low-stock`：查询低库存预警
- `GET /api/warnings/expiry`：查询临期/过期预警

### 6.4 处方管理

- `GET /api/prescriptions`：查询处方列表
- `GET /api/prescriptions/{id}`：查询处方详情
- `POST /api/prescriptions`：创建处方
- `POST /api/prescriptions/proxy-request`：药师发起代开申请
- `POST /api/prescriptions/{id}/doctor-approve`：医生确认药师代开
- `POST /api/prescriptions/{id}/submit`：提交处方
- `POST /api/prescriptions/{id}/audit`：审核处方
- `POST /api/prescriptions/{id}/dispense`：确认发药并扣减库存
- `POST /api/prescriptions/{id}/cancel`：取消处方

接口规则补充：

- 医生创建处方时，`doctor_id` 必须为当前登录医生本人
- 药师代开时，必须提交目标医生 ID
- 医生确认代开前，处方不得进入审核流程
- 系统必须保留“谁录入、谁授权、谁审核、谁发药”的完整操作链路

## 7. 数据库设计概览

核心表建议：

- `drug`：药品主表
- `inventory`：库存表
- `inventory_record`：库存流水表
- `prescription`：处方主表
- `prescription_item`：处方明细表
- `warning_record`：预警记录表，可选

详细设计见 [docs/architecture.md](/Users/sunsetflower/myJobs/Java/J2EE/docs/architecture.md)。

当前已初始化的数据库脚本：

- [001_init.sql](/Users/sunsetflower/myJobs/Java/J2EE/docker/mysql/init/001_init.sql)：创建数据库与业务账号
- [002_schema.sql](/Users/sunsetflower/myJobs/Java/J2EE/docker/mysql/init/002_schema.sql)：创建业务表、索引、主外键与基础约束
- [003_seed.sql](/Users/sunsetflower/myJobs/Java/J2EE/docker/mysql/init/003_seed.sql)：写入示例药品种子数据

## 8. Docker 容器化要求

建议使用 `docker-compose` 编排以下服务：

- `mysql`：MySQL 数据库
- `backend`：Spring Boot 服务
- `frontend`：React/Vite 前端
- `nginx`：可选，统一代理前后端

建议端口：

- MySQL：`3306`
- Backend：`8080`
- Frontend：`5173`
- Nginx：`80`

建议环境变量：

```env
MYSQL_DATABASE=drug_management
MYSQL_ROOT_PASSWORD=root
MYSQL_USER=drug_user
MYSQL_PASSWORD=drug_pass
SPRING_PROFILES_ACTIVE=dev
```

## 9. 单元测试要求

后端至少覆盖以下测试场景：

- 药品 CRUD 服务层测试
- 入库与出库规则测试
- 低库存判断测试
- 临期与过期判断测试
- 医生直接开方测试
- 药师代开并等待医生授权测试
- 未授权药师代开拒绝测试
- 处方审核与发药流程测试
- 发药扣减库存事务测试

后端测试分层建议：

- `controller` 层：
  - 关注请求参数校验、返回码、异常映射
  - 优先使用 `@WebMvcTest`
- `service` 层：
  - 关注业务规则、状态流转、事务边界
  - 优先使用 `JUnit 5 + Mockito`
- `mapper/dao` 层：
  - 关注 SQL 映射、分页查询、条件过滤
  - 可使用 `@MybatisTest` 或后续引入 Testcontainers
- 集成测试：
  - 关注“处方审核后发药并扣减库存”这类跨模块主流程
  - 建议在项目进入开发中后期补充

后端测试命名建议：

- 正常路径：`shouldCreateDrugWhenRequestIsValid`
- 规则拒绝：`shouldRejectProxyPrescriptionWhenDoctorNotApproved`
- 状态流转：`shouldMovePrescriptionToApprovedWhenAuditPassed`
- 异常场景：`shouldThrowWhenInventoryIsInsufficient`

后端优先测试清单：

1. 药品编码唯一性与逻辑停用规则
2. 入库、出库、盘点对库存与流水的双写一致性
3. 临期、过期、低库存三类判定逻辑
4. 医生直开、药师代开、医生拒绝、重新提交等处方状态流转
5. 发药扣减库存时的事务回滚与重复发药保护
6. 非法角色越权操作拦截

前端测试分层建议：

- 组件测试：
  - 表单组件、表格组件、状态标签组件
- 页面测试：
  - 药品列表页、库存预警页、处方详情页
- 交互测试：
  - 提交表单、审核处方、发药确认、错误提示展示
- API 交互测试：
  - 使用 mock 数据验证加载中、成功、失败三种状态

前端测试命名建议：

- `rendersDrugTableWhenDataLoaded`
- `showsValidationErrorWhenDrugNameIsEmpty`
- `disablesDispenseButtonWhenPrescriptionNotApproved`
- `showsDoctorApprovalBannerForProxyPrescription`

前端至少覆盖以下测试场景：

- 药品列表页渲染
- 药品表单校验
- 库存预警列表展示
- 处方审核交互流程

测试数据准备建议：

- 固定基础数据：医生、药师、药品、库存批次
- 明确边界数据：零库存、低库存、过期批次、临期批次
- 为处方流程准备至少三类样例：
  - 医生直接开方
  - 药师代开待授权
  - 审核通过但库存不足

覆盖策略建议：

- 文档阶段先约定关键业务规则必须具备测试
- 开发第一阶段先补 `service` 层单元测试骨架
- 每完成一个业务模块，同步补齐对应测试，不积压到最后
- 处方与库存模块优先高于页面样式类测试

## 10. 非功能性要求

- 统一异常处理
- 统一返回结构
- 参数校验
- 日志记录
- 基础权限模型预留
- 事务一致性
- 支持分页、排序、条件查询

## 11. 开发阶段建议

建议按以下顺序实施：

1. 初始化 `docker-compose`、MySQL、前后端工程
2. 初始化后端基础结构：
   `controller`、`service`、`mapper`、`common`、统一异常与统一响应
3. 建立数据库建表脚本与基础字典数据
4. 完成药品信息 CRUD，并同步补齐药品模块单元测试
5. 完成库存管理与库存流水，并补齐出入库、盘点、库存不足测试
6. 完成低库存与效期预警，并补齐规则判断测试
7. 完成处方流程，并重点补齐授权、审核、发药事务测试
8. 前端按“药品 -> 库存 -> 预警 -> 处方”顺序落地页面与交互测试
9. 接入 Nginx、补充 Docker 文件、初始化脚本与部署说明

建议在开发启动时新增以下测试目录规划：

```text
backend/src/test/java/com/example/drugmanagement/
├── controller/
├── service/
├── mapper/
└── integration/

frontend/src/
├── components/
│   └── __tests__/
├── pages/
│   └── __tests__/
└── test/
    ├── mocks/
    └── setup/
```

## 12. 当前阶段说明

当前仓库已包含以下初始化内容：

- `backend` Spring Boot + MyBatis 基础工程
- `frontend` React + Vite + Vitest 基础工程
- `docker-compose.yml` 与 MySQL/Nginx 基础配置
- 后端健康检查接口与前端占位首页
- 基础测试目录与示例测试
- 后端公共响应、错误码、分页查询对象、基础实体与业务枚举
- 数据库业务表初始化脚本与示例种子数据
- 药品模块 CRUD 的 `controller/service/mapper/dto/vo`
- 药品模块控制器测试与服务层单元测试

当前仍不包含：

- 药品、库存、预警、处方等正式业务实现
- MyBatis Mapper XML 与数据访问实现
- 登录鉴权、角色权限、审计日志落库实现
- 前端业务页面、接口封装与状态管理

如果继续下一步，建议先完成数据库业务表脚本与后端公共模块，再开始药品模块 CRUD 开发。

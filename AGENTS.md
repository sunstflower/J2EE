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

### 2026-06-03 19:27（库存模块联调）
- 会话目标：按文档要求完成库存模块的前端联调和实现
- 修改文件：
  - `README.md`
  - `AGENTS.md`
  - `docs/api.md`
  - `docs/frontend-integration.md`
  - `frontend/src/api/inventories.js`
  - `frontend/src/pages/HomePage.jsx`
  - `frontend/src/pages/InventoryPage.jsx`
  - `frontend/src/pages/__tests__/App.test.jsx`
  - `frontend/src/pages/__tests__/InventoryPage.test.jsx`
  - `frontend/src/styles.css`
- 主要变更：
  - 新增库存模块 API 封装，接入库存查询、库存流水查询、入库、出库、盘点接口
  - 新增库存联调页面，完成库存列表、流水列表及三类库存操作的最小前端闭环
  - 在库存请求中自动使用当前登录用户填充 `operatorName`，保持与现有后端接口契约一致
  - 同步 README、前端联调文档与 API 文档，记录库存联调现状及后续字段收口约束
  - 执行 `frontend npm test`，验证前端 6 个测试全部通过
- 备注：
  - 当前未改动后端库存业务代码；`operatorName` 仍是现有后端请求体字段，后续可再按认证文档继续收口

### 2026-06-03 19:20（药品页面联调）
- 会话目标：进入前端统一请求头注入与药品页面联调
- 修改文件：
  - `README.md`
  - `AGENTS.md`
  - `docs/api.md`
  - `docs/frontend-integration.md`
  - `backend/src/main/java/com/example/drugmanagement/common/auth/AuthInterceptor.java`
  - `frontend/src/api/client.js`
  - `frontend/src/api/drugs.js`
  - `frontend/src/pages/DrugPage.jsx`
  - `frontend/src/pages/HomePage.jsx`
  - `frontend/src/pages/__tests__/App.test.jsx`
  - `frontend/src/pages/__tests__/DrugPage.test.jsx`
  - `frontend/src/styles.css`
- 主要变更：
  - 新增前端统一 `api client` 与药品模块 API 封装，自动注入当前用户请求头
  - 新增药品联调页面，完成药品列表查询、新增药品、删除药品的最小前端闭环
  - 修复浏览器中文请求头限制问题，前端对 `X-User-Name` 进行 URL 编码，后端拦截器统一解码
  - 同步 README、前端联调文档与 API 文档，明确当前联调基线和请求头约定
  - 执行 `frontend npm test` 与 `backend mvn test`，验证前端 4 个测试和后端 56 个测试全部通过
- 备注：
  - 当前尚未引入路由与更多页面，后续建议沿用同一请求层继续进入库存模块联调

### 2026-06-03 11:47（认证基线恢复编码）
- 会话目标：恢复编码，收口最小认证基线并修复前端测试
- 修改文件：
  - `README.md`
  - `AGENTS.md`
  - `docs/authentication.md`
  - `docs/execution-checklist.md`
  - `frontend/src/auth.js`
  - `frontend/src/pages/__tests__/App.test.jsx`
- 主要变更：
  - 为前端补充最小认证状态存储兜底，在测试环境中使用内存存储替代不完整的 `localStorage`
  - 修复前端登录页测试，补充 Vitest 显式导入、测试前状态清理与用例间 DOM 清理
  - 同步 README、认证文档与执行清单，将认证阶段更新为“已完成基线、待继续联调收口”的状态
  - 执行 `frontend npm test`，验证前端 2 个测试全部通过
- 备注：
  - 后端认证拦截器与 `/api/auth/me` 已在本轮恢复编码前完成，本次主要完成前端测试收口与文档状态同步

### 2026-06-03 11:01（执行清单收敛）
- 会话目标：将现有设计文档收敛成项目落地执行清单
- 修改文件：
  - `README.md`
  - `AGENTS.md`
  - `docs/execution-checklist.md`
- 主要变更：
  - 新增执行清单文档，统一后续顺序、阶段目标、产出物与验收标准
  - 将认证接入、前端联调、Docker 演示、部署验证收敛到同一落地路径中
  - 在 README 中补充执行清单入口，便于后续恢复编码时直接按清单推进
- 备注：
  - 本次仅整理执行清单，不修改业务代码

### 2026-06-03 11:01（落地设计文档补全）
- 会话目标：补全认证方案、前端联调约定、Docker 实操说明、部署说明的设计文档
- 修改文件：
  - `README.md`
  - `AGENTS.md`
  - `docs/architecture.md`
  - `docs/authentication.md`
  - `docs/frontend-integration.md`
  - `docs/deployment.md`
- 主要变更：
  - 新增轻量认证方案说明，明确当前用户请求头、前后端职责和后续升级路径
  - 新增前端联调约定，明确最小页面清单、路由结构、联调顺序和联动要求
  - 新增部署说明，明确当前单机容器化交付边界、部署检查项和主功能验收路径
  - 在 README 和架构文档中补充新文档入口，统一后续实施参考路径
- 备注：
  - 本次仅补全文档设计，不修改业务代码

### 2026-06-03 11:01（API 与演示文档补全）
- 会话目标：继续补全文档，聚焦主功能联调与最终演示准备
- 修改文件：
  - `README.md`
  - `AGENTS.md`
  - `docs/api.md`
  - `docs/demo-script.md`
- 主要变更：
  - 新增 API 文档，按药品、库存、预警、处方四个主模块整理接口、关键参数与规则
  - 新增演示脚本，明确主功能演示顺序、演示前准备与核心说明点
  - 在 README 中补充 API 文档和演示脚本索引，并修正章节编号
- 备注：
  - 本次仍只补全文档，不修改业务代码

### 2026-06-03 11:01（落地文档补全）
- 会话目标：补全文档说明，聚焦主功能落地与后续实施准备
- 修改文件：
  - `README.md`
  - `AGENTS.md`
  - `docs/docker.md`
  - `docs/database-init.md`
  - `docs/testing.md`
- 主要变更：
  - 在 README 中补充主功能落地路线和已补齐文档索引
  - 新增 Docker 使用说明，聚焦主功能演示路径、服务职责与最低验证项
  - 新增数据库初始化说明，明确初始化脚本顺序、核心表范围与最小补数建议
  - 新增测试说明，聚焦主功能测试层次、当前覆盖范围与后续补测优先级
- 备注：
  - 本次仅补全文档说明，不修改业务代码

### 2026-06-03 11:01（文档优化）
- 会话目标：暂停业务代码编写，优先优化项目文档并梳理后续实现路线
- 修改文件：
  - `README.md`
  - `AGENTS.md`
  - `docs/architecture.md`
- 主要变更：
  - 明确当前项目阶段定位，区分“后端主链路已完成”与“容器化演示仍待完善”两类状态
  - 梳理后续实现路线，补充文档优先级、阶段目标与交付顺序
  - 补充 Docker 与测试策略的边界说明，明确当前 H2 测试与后续 MySQL 容器验证的关系
  - 修正文档中少量已落后于现状的表述，例如处方代理接口与当前初始化状态描述
- 备注：
  - 本次仅优化文档，不新增业务代码与测试代码

### 2026-06-03 11:01
- 会话目标：对照现有文档要求补全处方链路与测试验证
- 修改文件：
  - `README.md`
  - `AGENTS.md`
  - `docs/architecture.md`
  - `backend/pom.xml`
  - `backend/src/main/java/com/example/drugmanagement/mapper/PrescriptionMapper.java`
  - `backend/src/main/java/com/example/drugmanagement/service/impl/PrescriptionServiceImpl.java`
  - `backend/src/main/resources/mapper/PrescriptionMapper.xml`
  - `backend/src/test/java/com/example/drugmanagement/mapper/PrescriptionMapperTest.java`
  - `backend/src/test/java/com/example/drugmanagement/service/PrescriptionFlowIntegrationTest.java`
  - `backend/src/test/java/com/example/drugmanagement/service/PrescriptionServiceTest.java`
  - `backend/src/test/resources/h2/prescription-flow-init.sql`
  - `backend/src/test/resources/h2/prescription-mapper-init.sql`
- 主要变更：
  - 为处方发药补充基于当前状态的条件更新，增强重复发药场景下的链路保护
  - 新增处方 Mapper 测试，覆盖分页筛选、详情查询与状态更新 SQL 映射
  - 新增基于 H2 的处方链路集成测试，覆盖代开授权、审核、发药、库存扣减与 `DISPENSE` 流水写入
  - 扩展处方服务层测试，补充过期批次不可发药和发药状态迁移失败场景
  - 执行 `mvn test`，验证后端 54 个测试全部通过
- 备注：
  - 当前环境不可用 Docker socket，因此链路测试采用 H2 落地而非 Testcontainers；后续如进入容器化联调阶段，可再补 MySQL 方言级测试

### 2026-06-03 10:42
- 会话目标：完成处方模块，并在模块完成后执行测试且同步文档设计约束
- 修改文件：
  - `README.md`
  - `AGENTS.md`
  - `docs/architecture.md`
  - `backend/src/main/java/com/example/drugmanagement/controller/PrescriptionController.java`
  - `backend/src/main/java/com/example/drugmanagement/dto/prescription/CreatePrescriptionRequest.java`
  - `backend/src/main/java/com/example/drugmanagement/dto/prescription/PrescriptionAuditRequest.java`
  - `backend/src/main/java/com/example/drugmanagement/dto/prescription/PrescriptionDispenseRequest.java`
  - `backend/src/main/java/com/example/drugmanagement/dto/prescription/PrescriptionDoctorApprovalRequest.java`
  - `backend/src/main/java/com/example/drugmanagement/dto/prescription/PrescriptionItemRequest.java`
  - `backend/src/main/java/com/example/drugmanagement/dto/prescription/PrescriptionQueryRequest.java`
  - `backend/src/main/java/com/example/drugmanagement/mapper/PrescriptionItemMapper.java`
  - `backend/src/main/java/com/example/drugmanagement/mapper/PrescriptionMapper.java`
  - `backend/src/main/java/com/example/drugmanagement/service/PrescriptionService.java`
  - `backend/src/main/java/com/example/drugmanagement/service/impl/PrescriptionServiceImpl.java`
  - `backend/src/main/java/com/example/drugmanagement/vo/prescription/PrescriptionItemVO.java`
  - `backend/src/main/java/com/example/drugmanagement/vo/prescription/PrescriptionVO.java`
  - `backend/src/main/resources/mapper/PrescriptionItemMapper.xml`
  - `backend/src/main/resources/mapper/PrescriptionMapper.xml`
  - `backend/src/test/java/com/example/drugmanagement/controller/DrugControllerTest.java`
  - `backend/src/test/java/com/example/drugmanagement/controller/HealthControllerTest.java`
  - `backend/src/test/java/com/example/drugmanagement/controller/InventoryControllerAdditionalTest.java`
  - `backend/src/test/java/com/example/drugmanagement/controller/InventoryControllerTest.java`
  - `backend/src/test/java/com/example/drugmanagement/controller/PrescriptionControllerTest.java`
  - `backend/src/test/java/com/example/drugmanagement/controller/WarningControllerTest.java`
  - `backend/src/test/java/com/example/drugmanagement/service/PrescriptionServiceTest.java`
- 主要变更：
  - 实现处方模块创建、详情、分页、医生授权、提交审核、药师审核、发药、取消完整后端流程
  - 落地医生直接开方与药师代开两条状态流转，并补充库存扣减与 `DISPENSE` 流水联动
  - 新增处方模块 DTO、VO、Mapper、MyBatis XML、Controller、Service 与单元测试
  - 为适配新增处方 Mapper，补齐现有 `@WebMvcTest` 控制器测试中的 mapper mock 隔离
  - 执行 `mvn test`，验证后端 49 个测试全部通过
- 备注：
  - 当前处方模块已按文档中的角色与状态机约束完成基础闭环，后续应优先补齐 Mapper 层与集成链路测试

### 2026-06-03 10:30
- 会话目标：完成预警模块，并在模块完成后执行测试
- 修改文件：
  - `README.md`
  - `AGENTS.md`
  - `docs/architecture.md`
  - `backend/src/main/java/com/example/drugmanagement/controller/DrugController.java`
  - `backend/src/main/java/com/example/drugmanagement/controller/HealthController.java`
  - `backend/src/main/java/com/example/drugmanagement/controller/InventoryControllerAdditionalTest.java`
  - `backend/src/main/java/com/example/drugmanagement/controller/InventoryControllerTest.java`
  - `backend/src/main/java/com/example/drugmanagement/controller/WarningController.java`
  - `backend/src/main/java/com/example/drugmanagement/dto/warning/ExpiryWarningQueryRequest.java`
  - `backend/src/main/java/com/example/drugmanagement/mapper/WarningMapper.java`
  - `backend/src/main/java/com/example/drugmanagement/service/WarningService.java`
  - `backend/src/main/java/com/example/drugmanagement/service/impl/WarningServiceImpl.java`
  - `backend/src/main/java/com/example/drugmanagement/vo/warning/ExpiryWarningVO.java`
  - `backend/src/main/java/com/example/drugmanagement/vo/warning/LowStockWarningVO.java`
  - `backend/src/main/resources/mapper/WarningMapper.xml`
  - `backend/src/test/java/com/example/drugmanagement/controller/DrugControllerTest.java`
  - `backend/src/test/java/com/example/drugmanagement/controller/HealthControllerTest.java`
  - `backend/src/test/java/com/example/drugmanagement/controller/WarningControllerTest.java`
  - `backend/src/test/java/com/example/drugmanagement/service/WarningServiceTest.java`
- 主要变更：
  - 实现低库存预警、临期预警和过期预警分页查询
  - 新增预警模块 DTO、VO、Mapper、Service、Controller 与 MyBatis XML 映射
  - 修复 `WarningMapper.xml` 中的 XML 转义问题，并补齐控制器测试对 `WarningMapper` 的 mock 隔离
  - 执行 `mvn test`，验证药品、库存、预警模块测试全部通过
- 备注：
  - 当前预警模块已完成，下一步可进入处方模块开发

### 2026-06-03 10:28
- 会话目标：完成库存模块，并在模块完成后执行测试
- 修改文件：
  - `README.md`
  - `AGENTS.md`
  - `docs/architecture.md`
  - `backend/src/main/java/com/example/drugmanagement/controller/InventoryController.java`
  - `backend/src/main/java/com/example/drugmanagement/dto/inventory/CreateInventoryCheckRequest.java`
  - `backend/src/main/java/com/example/drugmanagement/dto/inventory/CreateInventoryOutboundRequest.java`
  - `backend/src/main/java/com/example/drugmanagement/dto/inventory/InventoryRecordQueryRequest.java`
  - `backend/src/main/java/com/example/drugmanagement/mapper/InventoryMapper.java`
  - `backend/src/main/java/com/example/drugmanagement/mapper/InventoryRecordMapper.java`
  - `backend/src/main/java/com/example/drugmanagement/service/InventoryService.java`
  - `backend/src/main/java/com/example/drugmanagement/service/impl/InventoryServiceImpl.java`
  - `backend/src/main/java/com/example/drugmanagement/vo/inventory/InventoryRecordVO.java`
  - `backend/src/main/resources/mapper/InventoryMapper.xml`
  - `backend/src/main/resources/mapper/InventoryRecordMapper.xml`
  - `backend/src/test/java/com/example/drugmanagement/controller/InventoryControllerAdditionalTest.java`
  - `backend/src/test/java/com/example/drugmanagement/service/InventoryServiceTest.java`
- 主要变更：
  - 补齐库存出库、库存盘点、库存流水分页查询能力
  - 实现按最早到期批次优先扣减库存、库存不足拦截和盘点差异流水写入
  - 扩展库存模块 DTO、VO、Mapper 与 XML 映射
  - 执行 `mvn test`，验证药品模块与库存模块测试全部通过
- 备注：
  - 当前库存模块已形成完整后端闭环，下一步可进入预警模块或处方模块开发

### 2026-06-03 10:20
- 会话目标：进行库存入库模型的业务代码编写，并在模块完成后执行测试
- 修改文件：
  - `README.md`
  - `AGENTS.md`
  - `docs/architecture.md`
  - `backend/src/main/java/com/example/drugmanagement/controller/InventoryController.java`
  - `backend/src/main/java/com/example/drugmanagement/dto/inventory/CreateInventoryInboundRequest.java`
  - `backend/src/main/java/com/example/drugmanagement/dto/inventory/InventoryQueryRequest.java`
  - `backend/src/main/java/com/example/drugmanagement/mapper/InventoryMapper.java`
  - `backend/src/main/java/com/example/drugmanagement/mapper/InventoryRecordMapper.java`
  - `backend/src/main/java/com/example/drugmanagement/service/InventoryService.java`
  - `backend/src/main/java/com/example/drugmanagement/service/impl/InventoryServiceImpl.java`
  - `backend/src/main/java/com/example/drugmanagement/vo/inventory/InventoryVO.java`
  - `backend/src/main/resources/mapper/InventoryMapper.xml`
  - `backend/src/main/resources/mapper/InventoryRecordMapper.xml`
  - `backend/src/test/java/com/example/drugmanagement/controller/DrugControllerTest.java`
  - `backend/src/test/java/com/example/drugmanagement/controller/HealthControllerTest.java`
  - `backend/src/test/java/com/example/drugmanagement/controller/InventoryControllerTest.java`
  - `backend/src/test/java/com/example/drugmanagement/service/InventoryServiceTest.java`
- 主要变更：
  - 实现库存入库、库存分页查询、库存详情查询与库存流水写入
  - 增加入库请求 DTO、库存展示 VO、Inventory/InventoryRecord Mapper 与 XML 映射
  - 实现同批次同效期库存累加与首次入库建档逻辑
  - 补充库存模块控制器测试和服务层单元测试，并执行 `mvn test` 验证通过
- 备注：
  - 当前已完成库存入库能力，库存出库、盘点和预警模块仍待继续开发

### 2026-06-03 10:17
- 会话目标：继续实现药品模块 CRUD，并补充对应测试
- 修改文件：
  - `README.md`
  - `AGENTS.md`
  - `docs/architecture.md`
  - `backend/src/main/java/com/example/drugmanagement/common/dto/PageQuery.java`
  - `backend/src/main/java/com/example/drugmanagement/controller/DrugController.java`
  - `backend/src/main/java/com/example/drugmanagement/dto/drug/CreateDrugRequest.java`
  - `backend/src/main/java/com/example/drugmanagement/dto/drug/DrugQueryRequest.java`
  - `backend/src/main/java/com/example/drugmanagement/dto/drug/UpdateDrugRequest.java`
  - `backend/src/main/java/com/example/drugmanagement/mapper/DrugMapper.java`
  - `backend/src/main/java/com/example/drugmanagement/service/DrugService.java`
  - `backend/src/main/java/com/example/drugmanagement/service/impl/DrugServiceImpl.java`
  - `backend/src/main/java/com/example/drugmanagement/vo/drug/DrugVO.java`
  - `backend/src/main/resources/mapper/DrugMapper.xml`
  - `backend/src/test/java/com/example/drugmanagement/controller/DrugControllerTest.java`
  - `backend/src/test/java/com/example/drugmanagement/controller/HealthControllerTest.java`
  - `backend/src/test/java/com/example/drugmanagement/service/DrugServiceTest.java`
- 主要变更：
  - 实现药品模块新增、分页查询、详情查询、更新、逻辑删除接口
  - 增加药品模块 DTO、VO、MyBatis Mapper 与 XML 映射
  - 实现药品编码唯一性校验、逻辑删除与分页偏移计算
  - 补充药品模块控制器测试和服务层单元测试，并验证 `mvn test` 通过
- 备注：
  - 当前药品模块已具备基础 CRUD 能力，库存与处方模块尚未开始实现

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

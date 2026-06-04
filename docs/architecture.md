# 架构设计说明

## 1. 总体架构

系统采用前后端分离架构：

- 前端：React + Vite + 原生 CSS
- 后端：Spring Boot + MyBatis
- 数据库：MySQL
- 部署：Docker Compose

```text
[React/Vite]
    |
 REST API
    |
[Spring Boot]
    |
[Service Layer]
    |
[DAO / MyBatis]
    |
[MySQL]
```

### 1.1 当前阶段定位

当前项目处于“后端主链路已完成、容器已可启动、前端结构仍待整改”的阶段。

这意味着：

- 后端核心业务逻辑已经具备继续扩展的基础
- 当前优先事项应转向前端结构整改、认证收口与部署回归
- 后续开发重点应放在路由化前端、统一交互、Docker 演示验收和部署交付

## 2. 核心业务流程

### 2.0 角色模型

当前演示版本仅保留两种角色：

- 医生
- 药师

权限边界建议：

- 医生：
  - 直接创建处方
  - 查看本人相关处方
  - 同意或拒绝药师代开申请
- 药师：
  - 发起代开申请
  - 审核处方
  - 执行发药
  - 查看库存与预警

关键限制：

- 药师不得以本人身份直接开具处方
- 药师代开必须绑定一个真实医生
- 医生未授权前，代开处方不得流转到审核或发药环节

### 2.1 药品维护

1. 管理员新增药品信息
2. 系统校验药品编码唯一性
3. 保存药品主数据
4. 支持后续修改、停用、删除

当前已完成的后端实现范围：

- 药品新增
- 药品分页查询
- 药品详情查询
- 药品更新
- 药品逻辑删除

### 2.2 库存入库

1. 选择药品
2. 录入批次号、数量、有效期、入库时间
3. 更新库存数量
4. 写入库存流水

当前已完成的后端实现范围：

- 库存入库
- 库存出库
- 库存盘点
- 库存分页查询
- 库存详情查询
- 库存流水分页查询
- 入库/出库/盘点流水写入

### 2.3 库存盘点

1. 发起盘点
2. 录入实盘数量
3. 计算盘盈盘亏
4. 更新库存
5. 记录盘点流水

### 2.4 预警查询

1. 汇总药品可用库存
2. 对比药品最低库存阈值，筛选低库存药品
3. 扫描库存批次有效期
4. 标记临期或过期批次
5. 返回预警分页列表

当前已完成的后端实现范围：

- 低库存预警分页查询
- 临期预警分页查询
- 过期预警分页查询

### 2.5 处方发药

1. 医生直接创建处方，或药师输入医生 ID 发起代开申请
2. 填写处方药品明细
3. 如果是药师代开，医生登录后进行确认或拒绝
4. 医生确认后，处方进入待审核状态
5. 药师审核处方
6. 审核通过后执行发药
7. 按批次扣减库存
8. 写入库存流水
9. 更新处方状态

建议增加操作审计：

- 开方人
- 代开药师
- 授权医生
- 审核药师
- 发药药师
- 每一步对应操作时间

当前已完成的后端实现范围：

- 处方创建与处方明细保存
- 医生直接建方草稿流转
- 药师代开待医生授权流转
- 医生确认/拒绝代开
- 处方提交审核
- 药师审核通过/驳回
- 发药扣减库存并写入 `DISPENSE` 流水
- 处方详情与分页查询
- 未发药处方取消

### 2.6 后续实现主线

建议后续开发顺序固定为：

1. 文档与运行说明补全
2. 认证授权与用户上下文接入
3. 前端页面与 API 调用层实现
4. 前后端联调
5. Docker 演示链路整理
6. 部署说明与回归清单整理

当前对应文档：

- 认证方案：`docs/authentication.md`
- 前端联调：`docs/frontend-integration.md`
- Docker 运行：`docs/docker.md`
- 部署说明：`docs/deployment.md`

## 3. 数据表设计建议

### 3.1 `drug`

药品主数据表。

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint | 主键 |
| drug_code | varchar(64) | 药品编码，唯一 |
| drug_name | varchar(128) | 药品名称 |
| generic_name | varchar(128) | 通用名 |
| category | varchar(64) | 药品分类 |
| specification | varchar(128) | 规格 |
| unit | varchar(32) | 单位 |
| manufacturer | varchar(128) | 生产厂家 |
| approval_number | varchar(128) | 批准文号 |
| purchase_price | decimal(10,2) | 采购价 |
| sale_price | decimal(10,2) | 销售价 |
| low_stock_threshold | int | 最低库存阈值 |
| enabled | tinyint | 是否启用 |
| created_at | datetime | 创建时间 |
| updated_at | datetime | 更新时间 |

### 3.2 `inventory`

按批次记录库存。

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint | 主键 |
| drug_id | bigint | 药品 ID |
| batch_no | varchar(64) | 批次号 |
| expiry_date | date | 有效期 |
| quantity | int | 当前数量 |
| locked_quantity | int | 锁定数量，可选 |
| location_code | varchar(64) | 库位编码，可选 |
| created_at | datetime | 创建时间 |
| updated_at | datetime | 更新时间 |

约束建议：

- `drug_id + batch_no` 唯一，或 `drug_id + batch_no + expiry_date` 唯一

### 3.3 `inventory_record`

库存流水表。

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint | 主键 |
| drug_id | bigint | 药品 ID |
| inventory_id | bigint | 库存 ID |
| record_type | varchar(32) | INBOUND / OUTBOUND / CHECK / DISPENSE |
| quantity_change | int | 变动数量，入正出负 |
| before_quantity | int | 变动前数量 |
| after_quantity | int | 变动后数量 |
| biz_no | varchar(64) | 业务单号 |
| remark | varchar(255) | 备注 |
| operator_name | varchar(64) | 操作人 |
| created_at | datetime | 创建时间 |

### 3.4 `prescription`

处方主表。

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint | 主键 |
| prescription_no | varchar(64) | 处方编号，唯一 |
| patient_name | varchar(64) | 患者姓名 |
| created_by_user_id | bigint | 处方创建人 ID |
| created_by_role | varchar(32) | 创建人角色，DOCTOR / PHARMACIST |
| doctor_id | bigint | 处方归属医生 ID |
| doctor_name | varchar(64) | 医生姓名 |
| status | varchar(32) | 处方状态 |
| doctor_approval_status | varchar(32) | NONE / PENDING / APPROVED / REJECTED |
| doctor_approved_at | datetime | 医生授权时间 |
| pharmacist_operator_id | bigint | 代开药师 ID，可空 |
| audit_by | varchar(64) | 审核人 |
| audit_time | datetime | 审核时间 |
| dispense_by | varchar(64) | 发药人 |
| dispense_time | datetime | 发药时间 |
| reject_reason | varchar(255) | 驳回原因 |
| created_at | datetime | 创建时间 |
| updated_at | datetime | 更新时间 |

### 3.5 `prescription_item`

处方明细表。

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint | 主键 |
| prescription_id | bigint | 处方 ID |
| drug_id | bigint | 药品 ID |
| dosage | varchar(64) | 剂量 |
| frequency | varchar(64) | 频次 |
| days | int | 天数 |
| quantity | int | 开具数量 |
| created_at | datetime | 创建时间 |

### 3.6 `warning_record`

预警记录表，可选。如果只需要实时查询，也可以先不建表。

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint | 主键 |
| warning_type | varchar(32) | LOW_STOCK / EXPIRY / EXPIRED |
| drug_id | bigint | 药品 ID |
| inventory_id | bigint | 库存 ID，可选 |
| content | varchar(255) | 预警内容 |
| status | varchar(32) | NEW / READ / RESOLVED |
| created_at | datetime | 创建时间 |

## 4. 接口响应约定

建议统一响应结构：

```json
{
  "code": 0,
  "message": "success",
  "data": {}
}
```

分页响应建议：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "records": [],
    "total": 0,
    "pageNum": 1,
    "pageSize": 10
  }
}
```

## 5. 关键业务规则

### 5.1 库存规则

- 库存不得扣减为负数
- 发药和出库前必须校验可用库存
- 同一药品建议按最早到期批次优先出库
- 盘点差异必须生成库存流水
- 库存流水必须可追溯到业务单号、操作人和操作时间
- 同一发药请求必须具备幂等保护，避免重复扣减

### 5.2 效期规则

- 已过期药品默认不允许发药
- 临期阈值建议可配置，默认 30 天

### 5.3 处方规则

- 医生可直接开方，但必须记录医生身份与操作时间
- 药师只能在医生授权前提下代医生开方
- 药师代开时必须录入医生 ID
- 代开处方在医生确认前，状态应为 `PENDING_DOCTOR_APPROVAL`
- 医生拒绝后，处方不得提交审核，除非重新发起
- 只有 `APPROVED` 状态的处方允许发药
- 发药动作必须与库存扣减处于同一事务
- 已发药处方不可重复发药
- 发药时必须排除已过期批次，并优先扣减最早到期批次
- 发药前必须再次校验处方明细中的药品仍为启用状态

### 5.4 审计与可追溯规则

- 关键业务动作必须保留操作人、角色、时间、业务编号
- 关键状态流转应记录变更前状态与变更后状态
- 代开场景至少应能追溯创建人、授权医生、审核药师、发药药师

### 5.5 校验规则建议

- 药品编码、处方编号必须唯一
- 药品采购价、销售价不得小于 0
- 库存数量、处方数量必须大于 0
- 已停用药品不得新增处方明细
- 已过期批次不得参与发药扣减

建议状态流转：

- 医生直接开方：`DRAFT -> SUBMITTED -> APPROVED -> DISPENSED`
- 药师代开：`DRAFT -> PENDING_DOCTOR_APPROVAL -> SUBMITTED -> APPROVED -> DISPENSED`
- 驳回分支：`PENDING_DOCTOR_APPROVAL -> CANCELLED` 或 `SUBMITTED -> REJECTED`

## 6. Docker 规划

### 6.1 服务划分

- `mysql`
- `backend`
- `frontend`
- `nginx`

### 6.2 网络关系

- 前端通过 Nginx 或直接访问后端 REST API
- 后端通过容器服务名连接 MySQL，例如 `mysql:3306`

### 6.3 数据卷建议

- MySQL 数据目录挂载到卷
- 初始化 SQL 放在 `docker/mysql/init/`

### 6.4 Docker 与测试边界

Docker 在本项目中的主要职责是：

- 本地演示
- 容器化交付
- 最终联调环境

当前自动化测试不强制依赖 Docker，原因是：

- 单元测试需要快速、稳定、低成本执行
- 当前执行环境可能没有 Docker socket
- 在此约束下，允许使用 `H2` 进行主链路验证

后续仍应补充面向演示环境的容器化验证：

- `docker compose up` 启动验证
- MySQL 初始化脚本执行验证
- 后端连接容器数据库验证
- 前端与 Nginx 联调验证

## 7. 测试设计建议

### 7.1 后端单元测试

- `DrugServiceTest`
- `InventoryServiceTest`
- `WarningServiceTest`
- `PrescriptionServiceTest`

重点覆盖：

- 药品编码唯一性
- 库存扣减边界
- 临期与过期判定
- 医生直接开方成功
- 药师代开待授权
- 医生拒绝代开
- 未授权处方禁止提交审核
- 审核状态流转
- 发药事务一致性

测试分层建议：

- `Controller` 层：
  - 使用 `@WebMvcTest`
  - 校验请求参数、HTTP 状态码、统一返回结构、异常处理
- `Service` 层：
  - 使用 `JUnit 5 + Mockito`
  - 校验状态机、事务边界、业务拒绝逻辑
- `Mapper` 层：
  - 校验 SQL 条件、批次排序、分页与统计查询
- `Integration` 层：
  - 校验“审核通过 -> 发药 -> 扣库存 -> 写流水”完整链路

建议优先实现的后端测试类：

- `DrugControllerTest`
- `DrugServiceTest`
- `InventoryServiceTest`
- `PrescriptionServiceTest`
- `PrescriptionFlowIntegrationTest`

当前已完成的测试：

- `DrugControllerTest`
- `DrugServiceTest`
- `InventoryControllerTest`
- `InventoryControllerAdditionalTest`
- `InventoryServiceTest`
- `PrescriptionMapperTest`
- `PrescriptionControllerTest`
- `PrescriptionFlowIntegrationTest`
- `PrescriptionServiceTest`
- `WarningControllerTest`
- `WarningServiceTest`
- `HealthControllerTest`
- `ApiResponseTest`

测试环境分层说明：

- `Mock/Unit`：用于快速验证纯业务规则
- `H2 Integration`：用于验证主流程、事务链路与 Mapper 映射
- `Docker/MySQL Validation`：用于验证接近演示环境的配置与运行结果

下一步建议补充的测试：

- `PrescriptionServiceTest` 异常分支扩展：库存不足、医生越权审批、已发药重复发药
- 更细粒度事务失败回滚测试：模拟发药状态更新失败、库存流水写入失败
- 前后端联调阶段补充接口级集成测试：覆盖分页筛选参数与统一错误响应

建议重点断言：

- 状态是否按预期流转
- 是否调用库存流水记录逻辑
- 事务失败时数据是否回滚
- 非法角色或非法状态是否正确报错
- 过期批次是否被排除
- 最早到期批次是否优先扣减

### 7.2 前端测试

- 药品表格展示
- 药品表单新增/编辑校验
- 低库存列表展示
- 处方审核与发药按钮状态

前端测试拆分建议：

- 组件测试：
  - 药品表单输入校验
  - 处方状态标签渲染
  - 预警列表空态与异常态展示
- 页面测试：
  - 药品列表页加载与分页
  - 处方详情页的审核、授权、发药动作显示逻辑
- 交互测试：
  - 药师代开时医生 ID 必填
  - 未授权处方不得显示可发药状态
  - 审核驳回后需展示原因

### 7.3 测试数据策略

- 构造固定角色数据：
  - 医生 1 名
  - 药师 1 名
- 构造固定药品数据：
  - 正常药品
  - 低库存药品
  - 临期药品
  - 过期药品
- 构造固定处方数据：
  - 医生直开处方
  - 药师代开待授权处方
  - 审核通过待发药处方

### 7.4 测试阶段规划

1. 项目骨架初始化时，先落测试框架与基础 `setup`
2. 每完成一个后端服务模块，立即补对应 `service` 单元测试
3. 处方与库存联动完成后，补一条主流程集成测试
4. 前端页面开发完成后，同步补页面与交互测试
5. 上线前再补回归测试清单，覆盖核心业务闭环

### 7.5 当前文档阶段建议

在继续编码前，应先形成以下可直接使用的说明：

- Docker 启动与关闭说明
- 数据库初始化与重置说明
- 核心演示脚本
- 测试执行矩阵
- 前后端联调约定
## 8. 后续路线图

### 8.1 已完成阶段

1. 工程骨架初始化
2. 数据库表结构初始化
3. 后端公共基础建设
4. 药品、库存、预警、处方后端主链路实现
5. 后端单元测试与 H2 集成测试基线建立

### 8.2 下一阶段

1. 文档与说明体系补全
2. 认证授权方案明确并落地
3. 前端页面与交互实现
4. 前后端联调与接口级验证
5. Docker 演示脚本与部署说明补齐

### 8.3 交付阶段

1. 演示环境一键启动
2. 核心业务链路回归验证
3. 项目部署与答辩材料整理

## 9. 当前项目状态

当前仓库已完成以下工程初始化：

- 后端：
  - Spring Boot 启动类
  - 基础包结构
  - 统一响应对象
  - 错误码枚举与分页响应对象
  - 全局异常处理
  - MyBatis 基础配置
  - 基础实体父类与业务枚举
  - 健康检查接口
  - 药品模块 DTO、VO、Mapper、Service、Controller
  - 库存模块 DTO、VO、Mapper、Service、Controller
  - 预警模块 DTO、VO、Mapper、Service、Controller
  - 基础测试样例
- 前端：
  - Vite + React 基础入口
  - 基础样式与首页占位
  - Vitest + Testing Library 初始化
  - 基础页面测试样例
- 容器化：
  - `docker-compose.yml`
  - MySQL 初始化脚本目录
  - 前后端 `Dockerfile`
  - Nginx 反向代理配置
- 数据库：
  - 业务表 DDL
  - 主外键与索引
  - 基础种子数据

当前尚未完成的内容：

- 权限认证模块
- 业务页面路由与 API 调用层
- Docker 使用说明与演示脚本
- 数据库初始化说明
- API 文档与测试说明
- 部署说明

说明：

- 药品、库存、预警、处方模块的 MyBatis XML 映射已完成
- 当前文档阶段的重点是把“可开发、可测试、可演示”的路线拆清楚
- 预警模块相关 MyBatis XML 映射已完成
- 处方模块仍待继续实现

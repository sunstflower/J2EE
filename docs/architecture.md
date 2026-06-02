# 架构设计说明

## 1. 总体架构

系统采用前后端分离架构：

- 前端：React + Vite + shadcn/ui
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

### 2.2 库存入库

1. 选择药品
2. 录入批次号、数量、有效期、入库时间
3. 更新库存数量
4. 写入库存流水

### 2.3 库存盘点

1. 发起盘点
2. 录入实盘数量
3. 计算盘盈盘亏
4. 更新库存
5. 记录盘点流水

### 2.4 处方发药

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

## 8. 后续实现顺序

1. 初始化 `backend`、`frontend`、`docker-compose.yml`
2. 完成数据库建表脚本
3. 实现药品 CRUD
4. 实现库存管理
5. 实现预警管理
6. 实现处方管理
7. 补齐测试

## 9. 当前初始化状态

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

当前尚未初始化的内容：

- MyBatis XML 映射
- 权限认证模块
- 业务页面路由与 API 调用层
- 具体业务模块的 Mapper/Service/Controller

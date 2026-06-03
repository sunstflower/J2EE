# API 文档

## 1. 目标

本 API 文档只聚焦当前项目已经落地或即将联调的主功能：

- 药品管理
- 库存管理
- 预警查询
- 处方管理

统一目标是让后续前端联调、接口验证和演示流程有明确依据。

## 2. 通用约定

### 2.1 基础路径

- 后端统一前缀：`/api`

### 2.2 统一响应

成功响应示例：

```json
{
  "code": 0,
  "message": "success",
  "data": {}
}
```

分页响应示例：

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

### 2.3 当前主要错误类型

- `4001`
  - 参数校验失败
- `4002`
  - 业务规则不满足
- `4010`
  - 未登录或当前用户请求头不合法
- `4040`
  - 资源不存在

### 2.4 当前用户请求头

除 `GET /api/health` 外，当前所有 `/api/**` 请求都应携带：

- `X-User-Id`
- `X-User-Name`
- `X-User-Role`

联调约定：

- `X-User-Role` 仅允许 `DOCTOR` 或 `PHARMACIST`
- 浏览器侧发送中文姓名时，`X-User-Name` 需要先做 URL 编码
- 后端会在拦截器中统一解码并写入当前用户上下文

## 3. 健康检查

### 3.1 健康接口

- `GET /api/health`

用途：

- 验证后端服务是否启动成功

## 3.2 当前用户校验

- `GET /api/auth/me`

用途：

- 验证当前请求头是否生效
- 供前端联调快速确认当前登录身份

## 4. 药品管理

### 4.1 分页查询药品

- `GET /api/drugs`

主要查询参数：

- `pageNum`
- `pageSize`
- `keyword`
- `category`
- `enabled`

用途：

- 药品列表页展示
- 演示时快速确认基础药品数据

### 4.2 查询药品详情

- `GET /api/drugs/{id}`

用途：

- 药品详情页
- 库存入库前确认药品信息

### 4.3 新增药品

- `POST /api/drugs`

核心字段：

- `drugCode`
- `drugName`
- `unit`
- `purchasePrice`
- `salePrice`
- `lowStockThreshold`

关键规则：

- 药品编码不可重复
- 价格不得小于 0

### 4.4 更新药品

- `PUT /api/drugs/{id}`

用途：

- 修改药品基础信息

### 4.5 删除药品

- `DELETE /api/drugs/{id}`

说明：

- 当前为逻辑删除

## 5. 库存管理

### 5.1 分页查询库存

- `GET /api/inventories`

主要查询参数：

- `pageNum`
- `pageSize`
- `drugId`
- `keyword`

### 5.2 查询库存详情

- `GET /api/inventories/{id}`

用途：

- 查看具体批次库存信息

### 5.3 库存入库

- `POST /api/inventories/inbound`

核心字段：

- `drugId`
- `batchNo`
- `expiryDate`
- `quantity`
- `locationCode`
- `bizNo`
- `operatorName`

关键规则：

- 药品必须存在
- 同药品同批次同效期库存会累加
- 入库后必须写库存流水

### 5.4 库存出库

- `POST /api/inventories/outbound`

核心字段：

- `drugId`
- `quantity`
- `bizNo`
- `operatorName`

关键规则：

- 库存不足时拒绝
- 按最早到期批次优先扣减
- 出库后必须写库存流水

### 5.5 库存盘点

- `POST /api/inventories/check`

核心字段：

- `inventoryId`
- `actualQuantity`
- `bizNo`
- `operatorName`

关键规则：

- 盘点后直接修正库存
- 必须写盘点流水

### 5.6 查询库存流水

- `GET /api/inventories/records`

主要查询参数：

- `pageNum`
- `pageSize`
- `drugId`
- `recordType`
- `bizNo`

用途：

- 演示库存变动轨迹
- 验证发药、出库、盘点结果

## 6. 预警查询

### 6.1 查询低库存预警

- `GET /api/warnings/low-stock`

主要查询参数：

- `pageNum`
- `pageSize`

### 6.2 查询临期/过期预警

- `GET /api/warnings/expiry`

主要查询参数：

- `pageNum`
- `pageSize`
- `expiryDays`

用途：

- 预警页面展示
- 演示当前效期风险

## 7. 处方管理

### 7.1 分页查询处方

- `GET /api/prescriptions`

主要查询参数：

- `pageNum`
- `pageSize`
- `status`
- `doctorId`
- `patientName`

### 7.2 查询处方详情

- `GET /api/prescriptions/{id}`

用途：

- 查看处方主信息与处方明细

### 7.3 创建处方

- `POST /api/prescriptions`

核心字段：

- `patientName`
- `createdByRole`
- `createdByUserId`
- `createdByName`
- `doctorId`
- `doctorName`
- `items`

关键规则：

- 医生可直接开方
- 药师不可用本人医生身份开方
- 药师代开必须指定医生

### 7.4 医生确认或拒绝代开

- `POST /api/prescriptions/{id}/doctor-approve`

核心字段：

- `action`
  - `APPROVE`
  - `REJECT`
- `doctorId`
- `doctorName`

关键规则：

- 仅待医生授权的处方可执行
- 医生只能审批属于自己的代开处方

### 7.5 提交处方

- `POST /api/prescriptions/{id}/submit`

关键规则：

- 医生直开处方从 `DRAFT` 提交
- 药师代开处方必须先完成医生授权

### 7.6 审核处方

- `POST /api/prescriptions/{id}/audit`

核心字段：

- `action`
  - `APPROVE`
  - `REJECT`
- `operatorId`
- `operatorName`
- `rejectReason`

关键规则：

- 只有 `SUBMITTED` 状态允许审核

### 7.7 发药

- `POST /api/prescriptions/{id}/dispense`

核心字段：

- `operatorId`
- `operatorName`

关键规则：

- 只有 `APPROVED` 状态允许发药
- 发药会扣减库存并写 `DISPENSE` 流水
- 已过期批次不参与发药
- 优先扣减最早到期批次

### 7.8 取消处方

- `POST /api/prescriptions/{id}/cancel`

关键规则：

- 已发药处方不可取消

## 8. 联调优先顺序

为保证项目落地，前后端联调建议按以下顺序执行：

1. 药品列表与详情
2. 库存入库、库存列表、库存流水
3. 低库存与临期预警
4. 处方创建与详情
5. 医生授权、审核、发药完整链路

# 数据库初始化说明

## 1. 目标

本说明只关注项目主功能落地所需的数据库初始化内容。

初始化目标：

- 创建业务数据库
- 创建业务账号
- 创建药品、库存、处方等核心表
- 写入最小可演示的种子数据

## 2. 初始化脚本

当前使用以下脚本：

- [001_init.sql](/Users/sunsetflower/myJobs/Java/J2EE/docker/mysql/init/001_init.sql)
  - 创建数据库与业务账号
- [002_schema.sql](/Users/sunsetflower/myJobs/Java/J2EE/docker/mysql/init/002_schema.sql)
  - 创建业务表、索引、主外键与基础约束
- [003_seed.sql](/Users/sunsetflower/myJobs/Java/J2EE/docker/mysql/init/003_seed.sql)
  - 写入示例药品种子数据

## 3. 执行顺序

脚本必须严格按以下顺序执行：

1. `001_init.sql`
2. `002_schema.sql`
3. `003_seed.sql`

## 4. 当前已覆盖的核心表

当前主功能依赖以下表：

- `drug`
- `inventory`
- `inventory_record`
- `prescription`
- `prescription_item`
- `warning_record`

这些表已经足以支撑当前后端主链路：

- 药品管理
- 库存入库、出库、盘点
- 低库存与效期预警
- 处方创建、审核、发药

## 5. 当前种子数据用途

当前种子数据的目标不是模拟完整生产数据，而是支撑最小演示。

主要用途：

- 提供基础药品数据
- 避免系统启动后出现空库
- 为后续库存、预警、处方演示提供起点

## 6. 后续最小补数建议

为了让主功能演示更顺畅，后续建议补充以下最小数据：

- 1 到 2 个医生标识
- 1 到 2 个药师标识
- 2 到 3 个药品库存批次
- 1 条低库存数据
- 1 条临期批次数据
- 1 条待审核处方数据

## 7. 重置原则

演示环境重置时，应遵循以下原则：

- 优先通过重建数据库或重建数据卷恢复初始状态
- 避免手工逐条删改业务数据
- 保证每次演示前的数据状态一致

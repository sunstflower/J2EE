# 架构设计说明

## 1. 总体结构

系统采用前后端分离架构：

- 前端：React + Vite
- 后端：Spring Boot + MyBatis
- 数据库：MySQL 8
- 容器化：Docker Compose + Nginx

```text
[Login]
   |
[POST /api/auth/login]
   |
[Main Route /]
   |
  +----+----+----+----+
  |    |    |    |    |
drugs inventories warnings prescriptions
       |                    |        |
   records               new page   detail page
```

## 2. 认证与角色

当前前端使用“后端登录 + 前端持有 token”的演示认证模型：

- 进入系统前必须先经过登录页
- 登录动作由后端 `POST /api/auth/login` 完成
- 前端仅保存 token 和当前用户信息，不再保存明文密码
- 用户号首位仅用于登录页角色提示：
  - `1` 开头：药师
  - `2` 开头：医生

默认演示账号：

- `1001 / pharm123`：张药师
- `2001 / doctor123`：王医生

前端登录成功后会保存 token 与当前用户，并在 API 请求头中自动注入：

- `Authorization: Bearer <token>`

## 3. 路由设计

主路由负责系统导航入口：

- `/`
  - 展示主路由总览
  - 提供“药物管理、库存管理、预警查询、处方工作台”按钮

业务分路由：

- `/drugs`
- `/inventories`
- `/inventories/records`
- `/warnings`
- `/prescriptions`
- `/prescriptions/new`
- `/prescriptions/:id`

其中以下页面已具备独立职责，不再复用父页面壳组件：

- `/inventories/records`：仅负责库存流水查询
- `/prescriptions/new`：仅负责处方新建
- `/prescriptions/:id`：通过路由参数读取处方详情

## 4. 前端落地原则

- 登录页与业务页分离，避免默认直接进入业务工作区
- 主路由只做模块分发，不把所有业务模块堆到一个长页面
- 分路由应承担真实职责，避免“只有 URL 变化、页面内容完全复用”的假路由
- 数据加载与提交需要统一处理 loading / error / success 提示
- 每次结构调整后必须补对应前端测试并执行回归

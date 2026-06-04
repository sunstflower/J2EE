# Docker 使用说明

## 1. 目标

本说明只服务于项目落地和演示，重点是：

- 快速启动 MySQL、后端、前端与 Nginx
- 验证主功能链路是否可运行
- 为后续答辩、演示和部署准备统一入口

## 2. 当前容器化范围

仓库中已经具备以下容器化基础：

- `docker-compose.yml`
- `backend/Dockerfile`
- `frontend/Dockerfile`
- `docker/mysql/init/`
- `docker/nginx/default.conf`

当前阶段不继续扩展额外基础设施，先保证以下主路径可跑通：

1. MySQL 启动
2. 后端连接数据库启动
3. 前端启动
4. Nginx 代理前后端
5. 演示药品、库存、预警、处方主链路

## 3. 服务职责

- `mysql`
  - 存储药品、库存、处方等业务数据
  - 首次启动时执行初始化 SQL
- `backend`
  - 提供 REST API
  - 承载药品、库存、预警、处方核心业务逻辑
- `frontend`
  - 提供业务页面与交互入口
- `nginx`
  - 统一入口
  - 代理前端静态资源与后端 API

## 4. 推荐启动顺序

推荐按以下顺序验证：

1. 启动 `mysql`
2. 确认初始化 SQL 执行完成
3. 启动 `backend`
4. 验证后端健康检查接口
5. 启动 `frontend`
6. 启动 `nginx`
7. 执行主功能演示

实际建议直接使用统一入口：

```bash
docker compose up --build -d
```

验证服务状态：

```bash
docker compose ps
```

查看关键日志：

```bash
docker compose logs -f mysql
docker compose logs -f backend
docker compose logs -f frontend
docker compose logs -f nginx
```

## 5. 启动后最低验证项

容器启动后至少验证以下内容：

- 数据库中已存在业务表
- 后端健康接口返回成功
- 药品列表接口可访问
- 库存列表接口可访问
- 预警接口可访问
- 处方接口可访问

推荐验证地址：

- Nginx 统一入口：`http://localhost:3000`
- 后端健康检查：`http://localhost:3000/api/health`
- 药品接口：`http://localhost:3000/api/drugs`

当前容器职责已收口为：

- `frontend` 容器负责构建并提供静态页面
- `nginx` 容器作为对外统一入口，同时代理前端页面和 `/api/**`
- 浏览器访问前端时，容器构建阶段会将 API 基地址注入为空前缀，页面继续请求现有 `/api/**` 接口路径，由 Nginx 统一转发到后端
- 外层 Nginx 代理前端容器时应指向 `frontend:80`，因为前端镜像当前是静态 Nginx 服务，不再监听 `5173`

## 6. 演示主链路

演示时优先使用以下顺序：

1. 查询药品列表
2. 执行一次库存入库
3. 查询库存列表与库存流水
4. 查询低库存或临期预警
5. 创建处方
6. 医生授权或直接提交
7. 审核处方
8. 发药并查询库存扣减结果

## 7. 当前阶段约束

当前需要明确两点：

- Docker 的目标是运行与演示，不是替代所有本地测试
- 当前自动化测试已使用 `H2` 支撑快速验证，后续仍需补容器环境验证

## 8. 下一步文档配套

为了真正完成容器化落地，下一步仍需补齐：

- 常见启动失败排查
- 演示环境重置步骤

## 9. 常用操作

启动并后台运行：

```bash
docker compose up --build -d
```

停止服务：

```bash
docker compose down
```

重置容器和数据库卷：

```bash
docker compose down -v
```

重建单个服务：

```bash
docker compose build backend
docker compose up -d backend
```

## 10. 常见排查

- `80` 端口冲突：
  - 当前已默认改为宿主机 `3000` 端口
  - 如仍冲突，再修改 `docker-compose.yml` 中 `nginx` 的宿主机端口映射
- 本机提示 `docker: command not found`：
  - 说明当前机器未安装 Docker Desktop 或 Docker Engine
  - 需要先安装 Docker，并确认 `docker compose version` 可执行
- 后端启动失败：
  - 先看 `docker compose logs backend`
  - 再确认 `mysql` 健康检查是否已通过
- 页面打开但接口报错：
  - 先访问 `http://localhost:3000/api/health`
  - 再确认浏览器请求是否命中 `/api`
- 数据异常需要重置：
  - 执行 `docker compose down -v`
  - 再重新 `docker compose up --build -d`

## 11. 当前验证结论

本轮已完成的非 Docker-socket 校验：

- `frontend` 生产构建通过：`npm run build`
- `backend` 打包通过：`mvn -q -DskipTests package`
- `docker-compose.yml` 结构已校验，包含 `mysql`、`backend`、`frontend`、`nginx`
- `docker/nginx/default.conf` 已按 `http` 上下文方式完成语法校验

本轮未完成项：

- 未能实际执行 `docker compose up --build -d`

阻塞原因：

- 当前环境缺少 `docker` 命令，属于机器环境缺失，不是项目仓库配置报错

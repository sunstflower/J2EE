# 药品管理最小演示项目

本项目当前已经收敛为一个可演示的最小闭环，前端只保留以下 3 个核心功能：

- 库存预览
- 开药
- 药物入库

项目目录：

- `backend`：Spring Boot + MyBatis 后端
- `frontend`：React + Vite 前端
- `docker`：MySQL 初始化脚本与 Nginx 配置
- `docs`：架构、前端范围、测试、部署等补充文档

## 1. 环境要求

本地开发或演示前，建议先准备以下环境：

- `Java 17`
- `Maven 3.9+`
- `Node.js 18+`
- `npm 9+`
- `MySQL 8.x`
- `Docker` 与 `Docker Compose`（用于容器化启动）

## 2. 快速启动方式

当前推荐两种启动方式：

1. 本地分开启动前后端
2. 使用 `Docker Compose` 一键启动整套演示环境

如果你只是为了最快看到完整效果，优先使用 `Docker Compose`。

## 3. 本地启动

### 3.1 启动数据库

需要先准备一个本地 MySQL，并创建项目数据库。项目默认连接信息在 [application.yml](/Users/sunsetflower/myJobs/Java/J2EE/backend/src/main/resources/application.yml)：

- 数据库：`drug_management`
- 用户名：`drug_user`
- 密码：`drug_pass`
- 端口：`3306`

如果你希望直接复用仓库中的初始化脚本，可以参考 `docker/mysql/init` 目录中的 SQL。

### 3.2 启动后端

在项目根目录执行：

```bash
cd backend
mvn spring-boot:run
```

后端默认启动在：

- `http://localhost:8080`

常用后端命令：

```bash
cd backend
mvn clean test
```

```bash
cd backend
mvn clean package
```

### 3.3 启动前端

新开一个终端，在项目根目录执行：

```bash
cd frontend
npm install
npm run dev
```

前端默认启动在：

- `http://localhost:5173`

如果前端本地开发时需要显式指定后端地址，可以使用环境变量：

```bash
cd frontend
VITE_API_BASE_URL=http://localhost:8080 npm run dev
```

### 3.4 本地开发常用脚本汇总

后端：

```bash
cd backend
mvn spring-boot:run
mvn clean test
mvn clean package
```

前端：

```bash
cd frontend
npm install
npm run dev
npm run build
npm run test
npm run test:watch
npm run preview
```

## 4. Docker Compose 启动

如果你希望直接拉起 MySQL、后端、前端和 Nginx，可以在项目根目录执行：

```bash
docker compose up --build -d
```

启动后主要访问地址：

- 演示入口：`http://localhost:3000`
- 后端接口：`http://localhost:8080`

常用容器命令：

```bash
docker compose ps
```

```bash
docker compose logs -f
```

```bash
docker compose down
```

```bash
docker compose down -v
```

说明：

- `down -v` 会删除 MySQL 数据卷
- 重新启动后会重新执行 `docker/mysql/init` 中的初始化脚本

## 5. 测试脚本

当前测试重点围绕最小演示闭环，建议按以下顺序执行。

### 5.1 后端测试

```bash
cd backend
mvn clean test
```

### 5.2 前端测试

```bash
cd frontend
npm install
npm run test
```

需要持续监听时：

```bash
cd frontend
npm run test:watch
```

### 5.3 前端生产构建验证

```bash
cd frontend
npm install
npm run build
```

## 6. 最小演示路径

当前推荐按以下顺序验收：

1. 登录系统
2. 进入库存预览，确认库存列表与低库存提醒
3. 执行一次药物入库
4. 返回库存预览，确认库存变化
5. 使用医生账号进入开药页并提交处方

如果使用容器化部署，建议先确认健康检查：

```bash
curl http://localhost:3000/api/health
```

## 7. 常见排查命令

查看容器状态：

```bash
docker compose ps
```

查看后端日志：

```bash
docker compose logs -f backend
```

查看前端日志：

```bash
docker compose logs -f frontend
```

查看 Nginx 日志：

```bash
docker compose logs -f nginx
```

查看 MySQL 日志：

```bash
docker compose logs -f mysql
```

如果容器化环境修改了数据库初始化脚本，通常需要重置数据卷后再重启：

```bash
docker compose down -v
docker compose up --build -d
```

## 8. 相关文档

- [架构设计说明](/Users/sunsetflower/myJobs/Java/J2EE/docs/architecture.md)
- [前端范围收缩说明](/Users/sunsetflower/myJobs/Java/J2EE/docs/frontend-scope.md)
- [测试说明](/Users/sunsetflower/myJobs/Java/J2EE/docs/testing.md)
- [部署说明](/Users/sunsetflower/myJobs/Java/J2EE/docs/deployment.md)

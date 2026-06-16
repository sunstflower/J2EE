# 药品管理最小演示项目

本项目用于演示药房场景下的最小业务闭环，当前只保留 3 个核心功能：

- 库存预览
- 药物入库
- 开药

项目目录：

- `backend`：Spring Boot 3 + MyBatis 后端
- `frontend`：React 18 + Vite 前端
- `docker`：MySQL 初始化脚本与 Nginx 配置
- `docs`：架构、前端范围、测试、部署等补充文档

## 1. 环境要求

本地开发或演示前建议准备：

- `Java 17`
- `Maven 3.9+`
- `Node.js 22+`
- `npm 10+`
- `MySQL 8.x`
- `Docker` 与 `Docker Compose`

## 2. 快速开始

推荐两种使用方式：

1. 使用 `Docker Compose` 一键启动完整演示环境
2. 本地分别启动 MySQL、后端和前端

如果目标是最快看到可用页面，优先使用 `Docker Compose`。

## 3. 演示账号与登录方式

当前登录页使用的是 `userId + password`，不是用户名登录。

可直接使用以下初始化账号：

- 医生账号：`2001 / doctor123`
- 药师账号：`1001 / pharm123`

角色差异：

- 医生可以访问“库存预览”“药物入库”“开药”
- 药师只显示“库存预览”“药物入库”，不显示“开药”

## 4. Docker Compose 使用方法

如果目标是最快完成演示，推荐直接使用 Docker Compose。

### 4.1 启动前检查

在项目根目录执行前，先确认：

- Docker Desktop 已启动，`docker info` 能正常返回 `Server` 信息
- 本机端口 `3000`、`8080`、`3306` 未被其他程序占用
- 当前目录为项目根目录，即存在 `docker-compose.yml`

可先执行：

```bash
docker info
docker compose config
```

### 4.2 首次启动

在项目根目录执行：

```bash
docker compose up --build -d
```

说明：

- `--build` 会强制按当前代码重新构建前后端镜像
- `-d` 表示后台启动容器
- 后端镜像会在容器内执行 `mvn clean package -DskipTests`
- 前端镜像会在容器内执行 `npm ci` 和 `npm run build`

首次执行说明：

- 第一次构建需要下载 Maven、npm 依赖和基础镜像，可能持续数分钟
- 如果输出长时间停在依赖下载步骤，通常不是卡死，而是在拉取依赖
- 当前 `docker-compose.yml` 已为构建阶段默认配置 Maven 阿里云镜像和 npm `npmmirror` 源，以降低首次冷启动耗时

如果网络环境特殊，需要覆盖默认镜像源，可在执行前临时指定：

```bash
MAVEN_REPO_URL=https://repo.maven.apache.org/maven2 \
NPM_REGISTRY=https://registry.npmjs.org \
docker compose up --build -d
```

### 4.3 启动成功后的访问地址

容器启动完成后访问：

- 演示入口：`http://localhost:3000`
- 后端接口：`http://localhost:8080`
- MySQL：`localhost:3306`

当前默认演示账号：

- 医生：`2001 / doctor123`
- 药师：`1001 / pharm123`

### 4.4 启动后验证

建议按以下顺序检查：

```bash
docker compose ps
curl http://localhost:3000/api/health
```

若启动成功，`docker compose ps` 应看到 `mysql`、`backend`、`frontend`、`nginx` 四个服务处于 `Up` 状态，其中 `mysql` 应显示 `healthy`。

健康接口正常返回示例：

```json
{"code":0,"message":"success","data":"ok"}
```

如果你还想进一步确认页面链路，可直接打开：

- `http://localhost:3000`

然后执行最小演示路径：

1. 使用医生账号登录
2. 打开“库存预览”确认库存和低库存提醒
3. 打开“药物入库”完成一次入库
4. 返回“库存预览”确认库存变化
5. 打开“开药”完成一次处方提交

### 4.5 常用 Docker 命令

常用命令：

```bash
docker compose ps
docker compose logs -f
docker compose logs -f backend
docker compose logs -f frontend
docker compose logs -f mysql
docker compose logs -f nginx
docker compose down
docker compose down -v
```

补充说明：

- `docker compose down -v` 会删除 MySQL 数据卷
- 重新执行 `up --build` 后，会重新加载 `docker/mysql/init` 中的初始化脚本
- 如果你修改了种子数据或表结构，通常需要先执行一次 `docker compose down -v`

### 4.6 停止、重启与重置

只停止容器但保留数据：

```bash
docker compose down
```

停止后重新启动已有容器：

```bash
docker compose up -d
```

代码或 Dockerfile 变更后重新构建并启动：

```bash
docker compose up --build -d
```

重置数据库并重新初始化演示数据：

```bash
docker compose down -v
docker compose up --build -d
```

### 4.7 常见问题

如果执行 `docker compose up --build -d` 报错无法连接 Docker daemon，通常说明 Docker Desktop 尚未启动。先启动 Docker Desktop，再确认：

```bash
docker info
```

只有当 `docker info` 能正常显示 `Server` 段时，再执行 Compose。

如果启动后页面无法访问，可优先检查：

```bash
docker compose ps
docker compose logs --tail=200 nginx
docker compose logs --tail=200 backend
docker compose logs --tail=200 mysql
curl -i http://localhost:3000/api/health
```

如果数据库脚本、种子数据或字符集配置有调整，建议直接重置数据卷再复验：

```bash
docker compose down -v
docker compose up --build -d
```

## 5. 本地开发使用方法

### 5.1 准备数据库

需要先准备一个本地 MySQL，并创建数据库 `drug_management`。默认连接配置见 [application.yml](/Users/sunsetflower/myJobs/Java/J2EE/backend/src/main/resources/application.yml)：

- 地址：`localhost:3306`
- 数据库：`drug_management`
- 用户名：`drug_user`
- 密码：`drug_pass`

如果要导入演示数据，可执行 `docker/mysql/init` 目录下的初始化脚本。

### 5.2 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端默认地址：

- `http://localhost:8080`

### 5.3 启动前端

新开一个终端执行：

```bash
cd frontend
npm install
VITE_API_BASE_URL=http://localhost:8080 npm run dev
```

前端默认地址：

- `http://localhost:5173`

说明：

- 本地开发时建议显式设置 `VITE_API_BASE_URL=http://localhost:8080`
- 容器化部署时，前端会通过同源 `/api` 访问后端，不需要额外设置该变量

## 6. 常用开发命令

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

## 7. 最小演示路径

建议按以下顺序验收：

1. 使用 `2001 / doctor123` 登录
2. 进入“库存预览”，确认库存列表和低库存提醒正常显示
3. 进入“药物入库”，选择一条药品完成入库
4. 返回“库存预览”，确认库存数量发生变化
5. 进入“开药”，选择药品并提交处方
6. 切换 `1001 / pharm123` 登录，确认页面中不显示“开药”入口

## 8. 测试与构建

后端测试：

```bash
cd backend
mvn clean test
```

前端测试：

```bash
cd frontend
npm install
npm run test
```

前端生产构建：

```bash
cd frontend
npm install
npm run build
```

## 9. 常见排查

如果 `docker compose up --build -d` 长时间没有结束，优先判断是否仍在首次下载依赖：

```bash
docker compose build --progress=plain
```

如果日志停在 `mvn dependency:go-offline` 或 `npm ci`，通常不是卡死，而是在拉取依赖。首次完整构建成功一次后，BuildKit 缓存会显著缩短后续重建时间。

如果登录后返回 `500` 或页面请求失败，依次检查：

```bash
docker compose ps
docker compose logs --tail=200 backend
docker compose logs --tail=200 mysql
curl -i http://localhost:3000/api/health
```

如果数据库初始化脚本已变更，重建数据卷后再复验：

```bash
docker compose down -v
docker compose up --build -d
```

## 10. 相关文档

- [架构设计说明](/Users/sunsetflower/myJobs/Java/J2EE/docs/architecture.md)
- [前端范围收缩说明](/Users/sunsetflower/myJobs/Java/J2EE/docs/frontend-scope.md)
- [测试说明](/Users/sunsetflower/myJobs/Java/J2EE/docs/testing.md)
- [部署说明](/Users/sunsetflower/myJobs/Java/J2EE/docs/deployment.md)

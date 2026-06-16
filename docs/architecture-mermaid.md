# 系统架构图（Mermaid）

以下架构图对应当前项目的最小演示闭环，突出浏览器、前端、反向代理、后端、认证、业务服务、数据访问与 MySQL 初始化关系。

```mermaid
flowchart LR
    doctor[医生]
    pharmacist[药师]

    subgraph client[浏览器侧]
        browser[浏览器]
        frontend[前端 SPA\nVite + React]
        pages[核心页面\n登录 / 库存预览 / 开药 / 药物入库]
        auth[本地登录态\nToken 或兼容请求头]
        browser --> frontend
        frontend --> pages
        frontend --> auth
    end

    subgraph gateway[访问入口]
        nginx[Nginx\nlocalhost:3000]
    end

    subgraph server[应用服务侧]
        backend[Spring Boot API\nlocalhost:8080]
        interceptor[AuthInterceptor\n角色与会话校验]
        controllers[Controller\nAuth / Inventory / Prescription / Warning / Drug / Health]
        services[Service\nInventory / Prescription / Warning / Drug]
        mapper[MyBatis Mapper XML]
        backend --> interceptor
        interceptor --> controllers
        controllers --> services
        services --> mapper
    end

    subgraph data[数据层]
        mysql[(MySQL 8.4\n药品 / 库存 / 处方 / 用户)]
        seed[初始化脚本\n002_schema.sql\n003_seed.sql]
        mapper --> mysql
        seed --> mysql
    end

    doctor --> browser
    pharmacist --> browser
    frontend -->|/api| nginx
    nginx -->|反向代理| backend

    services --> inventoryFlow[库存预览与低库存查询]
    services --> inboundFlow[药物入库]
    services --> prescribeFlow[医生开药]

    classDef actor fill:#f6efe6,stroke:#8c5a2b,color:#3a2412;
    classDef clientNode fill:#f3f8ef,stroke:#5b7f42,color:#203016;
    classDef serverNode fill:#eef4fb,stroke:#3e6b96,color:#10263d;
    classDef dataNode fill:#fbf1f4,stroke:#9a4f68,color:#421725;

    class doctor,pharmacist actor;
    class browser,frontend,pages,auth clientNode;
    class nginx,backend,interceptor,controllers,services,mapper,inventoryFlow,inboundFlow,prescribeFlow serverNode;
    class mysql,seed dataNode;
```

## 说明

- 当前主演示链路为：登录 -> 库存预览 -> 药物入库 -> 返回库存确认 -> 医生开药。
- 药师只使用库存预览与药物入库；开药能力由后端角色校验与前端入口控制共同约束。
- 部署时浏览器统一访问 `localhost:3000`，由 Nginx 代理前端静态资源与 `/api` 请求。
- 前端优先使用 `Authorization: Bearer <token>`，同时兼容旧的用户信息请求头。

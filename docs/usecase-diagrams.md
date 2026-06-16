# 用例图说明

当前项目按最小演示范围整理为以下 3 张用例图：

- `docs/usecase-overview.puml`
  - 系统总览用例图
- `docs/usecase-doctor.puml`
  - 医生角色用例图
- `docs/usecase-pharmacist.puml`
  - 药师角色用例图

## 建议用途

- 在课程设计文档中展示系统总体参与者与核心功能
- 单独说明医生与药师的角色边界
- 对应当前前端收口后的三个核心能力：库存预览、开药、药物入库

## 导出示例

如果本机已安装 PlantUML，可执行：

```bash
plantuml docs/usecase-overview.puml docs/usecase-doctor.puml docs/usecase-pharmacist.puml
```


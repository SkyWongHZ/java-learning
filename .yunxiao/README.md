# 云效测试环境镜像流水线

这里保存两条可以版本化的云效 Flow YAML：

- `backend-pipeline.yml`：Maven 自动化测试与打包、云效构建后端镜像、推送 ACR、
  测试 ECS 拉取镜像并只更新 `backend`。
- `frontend-pipeline.yml`：TypeScript 类型检查与 Vite 测试构建、云效构建前端镜像、
  推送 ACR、测试 ECS 拉取镜像并只更新 `frontend`。

两条流水线均不配置自动触发。将代码推送到 GitHub `main` 后，在云效手动点击
“运行”才会部署。

后端测试由 `.yunxiao/scripts/backend-ci.sh` 在云效构建机中启动一次性的
MariaDB 和 Redis，测试结束立即销毁。测试不会连接、清空或修改 ECS 上的真实
测试数据库。

## 已配置的云资源

| 资源 | 值 |
| --- | --- |
| ACR 地域 | `cn-hangzhou` |
| ACR 服务连接 | `java-learning-acr-hangzhou` / `xeg5ogxm1de5yrcm` |
| ACR 私有命名空间 | `docker_sky_private` |
| 后端镜像仓库 | `springboot-demo-backend` |
| 前端镜像仓库 | `bajie-ops-frontend` |
| 测试主机组 | `java-learning-test` / `XgsRq1BEiPbpjfE1` |
| 云效环境 | 日常环境（测试用途） |

## 流水线私密变量

两条流水线都需要在“变量和缓存”中配置：

| 变量 | 说明 |
| --- | --- |
| `ACR_USERNAME` | ACR 个人版访问凭证用户名 |
| `ACR_PASSWORD` | ACR 固定密码，必须设置为私密变量 |

真实密码不能写进 YAML、Compose、GitHub 或 ECS 的项目目录。发布脚本只使用临时
Docker 配置目录登录，发布结束即删除。

## 发布行为

镜像标签使用 `test-${DATETIME}`，不会覆盖上一版。ECS 不运行 Maven、npm 或
`docker build`，只执行以下操作：

1. 下载当前提交对应的 Compose 发布配置。
2. 临时登录杭州 ACR VPC 地址。
3. `docker compose pull <service>`。
4. `docker compose up -d --no-deps <service>`。
5. 等待容器健康，并检查前端入口或后端接口。
6. 失败时恢复 `/etc/springboot-demo-test/images.env` 中的上一镜像并重新启动。

数据库 SQL 不在流水线中重复执行，数据库变更继续单独管理。

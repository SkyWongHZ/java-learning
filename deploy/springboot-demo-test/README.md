# Spring Boot Demo 测试环境 Docker Compose

该目录是云效两条测试流水线共用的镜像构建和 ECS 发布定义。

## 发布拓扑

```text
GitHub main
  ├─ 后端流水线：Maven verify -> 云效构建后端镜像 -> ACR
  └─ 前端流水线：类型检查/Vite -> 云效构建前端镜像 -> ACR
                                      |
                                      v
                                测试 ECS 主机组
                                      |
                         docker compose pull + up
                                      |
                         健康检查失败则恢复上一镜像
```

镜像由云效公共构建集群生成，2 核 2 GB 的测试 ECS 不负责编译代码或构建镜像。
ECS 只拉取 ACR 私有镜像并更新对应的单个服务。

## 测试环境入口

| 用途 | 地址 |
| --- | --- |
| 前端入口 | <http://118.178.184.13/operation/> |
| 学生管理 | <http://118.178.184.13/operation/students> |
| 后端接口文档 | <http://118.178.184.13/doc.html> |
| 后端云效流水线 | <https://flow.aliyun.com/pipelines/5156664> |
| 前端云效流水线 | <https://flow.aliyun.com/pipelines/5156671> |

云效构建出的 ACR 镜像标签为 `test-${DATETIME}`，每次运行都会变化；上表中的
ECS 公网访问入口不会随镜像标签变化。若 ECS 更换公网 IP 或后续绑定测试域名，
必须同步更新本表和仓库根目录 `README.md`。

当前入口使用公网 HTTP，只允许保存测试数据，不得录入真实个人信息或其他敏感
数据。账号密码、数据库连接信息和 ACR 凭据仍只能保存在既定私密配置中。

## ACR

- 地域：华东 1（杭州）
- 命名空间：`docker_sky_private`
- 后端仓库：`springboot-demo-backend`
- 前端仓库：`bajie-ops-frontend`
- ECS 拉取地址使用杭州 VPC 域名。

## ECS 文件

- 版本目录：`/opt/springboot-demo-test/releases/<流水线时间>-<服务>`
- 当前配置：`/opt/springboot-demo-test/current`
- 运行时密钥：`/etc/springboot-demo-test/runtime.env`
- 当前镜像版本：`/etc/springboot-demo-test/images.env`

`runtime.env` 和 ACR 密码都不能提交到 Git。流水线通过云效私密变量把 ACR
凭证临时传给部署脚本；脚本使用临时 Docker 配置目录，发布结束后立即删除。

## 测试账号

测试环境真实登录凭据仅保存在本机
`deploy/springboot-demo-test/.env.test-account.local`。该文件匹配仓库根目录
`.gitignore` 中的 `**/.env.*.local` 规则，不得提交到 GitHub。

README、接口文档和部署脚本只能记录账号文件的位置或使用占位示例，不能包含
真实密码。需要在云效流水线中使用凭据时，应配置为云效私密变量。

## 文件职责

- `backend.Dockerfile`：把云效构建出的 Spring Boot JAR 封装成运行镜像。
- `frontend.Dockerfile`：把 Vite `dist` 和 Nginx 配置封装成前端镜像。
- `compose.yaml`：只引用镜像，不包含 `build:`。
- `deploy-image.sh`：串行锁、登录 ACR、拉取、单服务更新、健康检查、失败回滚。
- `deploy.sh`：需要手工重拉两个当前镜像时使用。

数据库建表和迁移 SQL 不在每次发布时自动执行，继续按独立变更流程管理。

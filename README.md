# Java Learning

用于学习 Java 17、Spring Boot 2.7、Spring MVC、MyBatis-Plus 和 MySQL 的后端项目。

## 项目

- [`springboot-demo`](springboot-demo)：Spring Boot 后端、数据库脚本和自动化测试。
- [`bajie-ops-frontend`](bajie-ops-frontend)：Vue 3 + Vite 管理端。
- [`deploy/springboot-demo-test`](deploy/springboot-demo-test)：测试环境 Docker Compose 发布配置。
- [`.yunxiao`](.yunxiao)：前后端两条阿里云云效测试流水线。

## 阿里云测试环境

当前测试环境部署在阿里云 ECS，通过同一个 Nginx 公网入口访问前端和后端：

- 前端入口：<http://118.178.184.13/operation/>
- 学生管理：<http://118.178.184.13/operation/students>
- 后端接口文档：<http://118.178.184.13/doc.html>
- 后端云效流水线：<https://flow.aliyun.com/pipelines/5156664>
- 前端云效流水线：<https://flow.aliyun.com/pipelines/5156671>

每次运行流水线都会生成新的 `test-${DATETIME}` ACR 镜像标签，但应用访问地址
不会随镜像标签变化。若 ECS 公网 IP 或测试域名发生变更，需要同步更新本节和
[`deploy/springboot-demo-test/README.md`](deploy/springboot-demo-test/README.md)。

测试站点当前使用公网 HTTP，只能放测试数据，不得录入真实个人信息或敏感数据。

## 克隆后运行

需要准备：

- Git
- Java 17
- Docker Desktop（推荐，用于一键启动本地 MySQL）；也可以使用自己的 MySQL 5.7/8.0

```bash
git clone https://github.com/SkyWongHZ/java-learning.git
cd java-learning/springboot-demo

docker compose up -d
cp .env.example .env
./scripts/start-dev.sh
```

启动后访问：

- <http://localhost:18763/hello>
- <http://localhost:18763/doc.html>

运行自动化测试：

```bash
./scripts/start-dev.sh --test
```

详细说明见 [`springboot-demo/readme.md`](springboot-demo/readme.md)。

## 敏感信息约定

- 真实 `.env`、数据库账号密码、私钥和本机构建产物不得提交。
- 仓库只保留可以公开的本地开发示例配置。
- 示例密码仅用于绑定在 `127.0.0.1` 的本地 Docker MySQL，不得用于公网或生产环境。
- 公司项目、公司域名、内部接口和内部文档不属于本仓库内容。

完整约定见 [`SECURITY.md`](SECURITY.md)。

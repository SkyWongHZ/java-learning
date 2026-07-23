# Java Learning

用于学习 Java 17、Spring Boot 2.7、Spring MVC、MyBatis-Plus 和 MySQL 的后端项目。

## 项目

- [`springboot-demo`](springboot-demo)：包含分层 CRUD、参数校验、统一响应、全局异常、请求链路日志、Knife4j 和 MockMvc 测试。

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
./scripts/test.sh
```

详细说明见 [`springboot-demo/readme.md`](springboot-demo/readme.md)。

## 敏感信息约定

- 真实 `.env`、数据库账号密码、私钥和本机构建产物不得提交。
- 仓库只保留可以公开的本地开发示例配置。
- 示例密码仅用于绑定在 `127.0.0.1` 的本地 Docker MySQL，不得用于公网或生产环境。
- 公司项目、公司域名、内部接口和内部文档不属于本仓库内容。

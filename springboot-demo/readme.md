# Spring Boot Demo

## 快速启动

项目使用真实 MySQL，不使用 H2。首次克隆后，推荐通过 Docker Compose启动一个仅绑定本机的 MySQL 8：

```bash
docker compose up -d
cp .env.example .env
./scripts/start-dev.sh
```

`compose.yaml` 将 MySQL端口映射到 `127.0.0.1:13306`，示例账号密码只用于本机开发。不要把这些示例密码用于公网或生产环境。

访问：

- <http://localhost:18763/hello>
- <http://localhost:18763/doc.html>

停止本地数据库：

```bash
docker compose down
```

如需同时删除本地测试数据卷：

```bash
docker compose down -v
```

## 使用自己的 MySQL

可以连接 MySQL 5.7或 8.0。复制 `.env.example` 后，将 `DB_URL`、`DB_USERNAME` 和 `DB_PASSWORD` 改成自己的连接信息，再执行建表脚本：

```bash
mysql -h 数据库地址 -P 3306 -u root -p < sql/20260717_create_demo_user.sql
```

如使用 ECS自建 MySQL，也可以使用项目提供的初始化脚本。完整步骤见：

- [ECS MySQL连接指南](docs/ECS-MySQL连接指南.md)

敏感配置只保存在本地 `.env` 中，不要提交真实 ECS地址、数据库账号或密码。

首次初始化 ECS数据库和本地 `.env`：

```bash
ECS_HOST='你的 ECS地址或 SSH别名' ./scripts/provision-ecs-mysql-dev.sh
```

初始化完成后执行开发启动脚本：

```bash
./scripts/start-dev.sh
```

脚本会自动加载 `.env`、查找并校验 Java 17、校验 `dev` profile，然后执行 Maven Wrapper。找不到 Java时可以设置 `SPRINGBOOT_JAVA_HOME=/path/to/jdk-17`。按 `Ctrl+C` 停止应用。

ECS 当前运行 MySQL `5.7.43`。本项目的 Connector/J `8.0.33` 可以兼容，不为本阶段 CRUD验收升级数据库。

## 自动测试

```bash
./scripts/test.sh
```

测试脚本自动加载 `.env` 并强制启用 `test` profile。CRUD测试使用唯一用户名并通过事务回滚；执行前需要确认 `.env` 指向的 MySQL可用。

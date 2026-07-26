# Spring Boot Demo

## 快速启动

项目使用真实 MySQL 和 Redis，不使用 H2。首次克隆后，可通过 Docker Compose
启动仅绑定本机的 MySQL 8 和 Redis 7：

```bash
cp .env.example .env
openssl rand -hex 32
# 将上一行生成的值填入 .env 的 REDIS_PASSWORD
docker compose up -d
./scripts/start-dev.sh
```

`compose.yaml` 将 MySQL 映射到 `127.0.0.1:13306`、Redis 映射到
`127.0.0.1:16379`。示例 MySQL 账号密码只用于本机开发，不要用于公网或生产环境。

访问：

- <http://localhost:18763/hello>
- <http://localhost:18763/doc.html>

停止本地 MySQL 和 Redis：

```bash
docker compose down
```

如需同时删除本地数据卷：

```bash
docker compose down -v
```

## 使用自己的 MySQL

可以连接 MySQL 5.7或 8.0。复制 `.env.example` 后，将 `DB_URL`、`DB_USERNAME` 和 `DB_PASSWORD` 改成自己的连接信息，再执行建表脚本：

```bash
mysql -h 数据库地址 -P 3306 -u root -p 数据库名 \
  < sql/20260726_create_student_management_tables.sql
```

如使用 ECS 自建 MySQL，完整初始化步骤见：

- [ECS MySQL连接指南](docs/ECS-MySQL连接指南.md)

敏感配置只保存在本地 `.env` 中，不要提交真实 ECS 地址、数据库账号、Redis
密码或其他凭据。

全新数据库执行鉴权表建表脚本：

```bash
mysql -h 数据库地址 -P 3306 -u root -p < sql/20260723_create_auth_tables.sql
```

再执行学生信息管理业务表建表脚本：

```bash
mysql -h 数据库地址 -P 3306 -u root -p 数据库名 \
  < sql/20260726_create_student_management_tables.sql
```

如果数据库已经使用过旧版“仅 MySQL Token”表结构，再执行一次迁移：

```bash
mysql -h 数据库地址 -P 3306 -u root -p < sql/20260724_migrate_auth_token_to_redis.sql
```

本项目在 ECS 上使用已经部署好的独立 `springboot-demo-redis` 容器。Redis 只监听
ECS 的 `127.0.0.1:16379`，不对公网开放，也不使用其他项目的 Redis。日常开发只需：

```bash
./scripts/start-dev.sh
```

脚本会自动加载 `.env`、建立并验证 Redis SSH 隧道、查找并校验 Java 17、校验
`dev` profile，然后执行 Maven Wrapper。按 `Ctrl+C` 停止应用时，脚本会同时关闭
本次创建的 Redis 隧道。找不到 Java 时可以设置
`SPRINGBOOT_JAVA_HOME=/path/to/jdk-17`。

ECS 当前运行 MySQL `5.7.43`。本项目的 Connector/J `8.0.33` 可以兼容，不为本阶段 CRUD验收升级数据库。

## 自动测试

```bash
./scripts/start-dev.sh --test
```

同一个脚本会复用自动 Redis 隧道逻辑，并强制启用 `test` profile。CRUD 测试
使用唯一用户名并通过事务回滚，测试产生的 Redis key 会主动清理。

## 登录鉴权

Token 方案与 `bjsm-cloud` 保持一致：MySQL 保存 Token 白名单，Redis 保存
`token -> 用户 ID` 会话并负责过期时间。Token 是移除连字符后的 UUID（32 位），
默认 30 天滑动过期。客户端必须通过 `system` 请求头声明登录端：

- `1`：PC
- `2`：APP
- `3`：儿童端
- `4`：成人端
- `5`：OSA 患者端
- `6`：数据大屏
- `7`：第三方 API
- `8`：微信
- `9`：睡眠患者 H5
- `10`：设备运营平台

同一用户在同一 `system` 再次登录，会删除该端旧 Token；不同 `system` 的会话
相互独立。MySQL 中没有 Token、Redis 会话过期，以及 `system` 不匹配时，会分别
返回对应的统一业务错误。

首次启动前在本地 `.env` 中设置：

```bash
AUTH_BOOTSTRAP_ADMIN_USERNAME='admin'
AUTH_BOOTSTRAP_ADMIN_PASSWORD='替换为至少8位且包含三类字符的强密码'
AUTH_BOOTSTRAP_ADMIN_DISPLAY_NAME='系统管理员'
```

应用只在管理员表为空时创建初始账号，密码使用 BCrypt 保存。创建成功后应从运行环境中移除
`AUTH_BOOTSTRAP_ADMIN_PASSWORD`，以后启动不会覆盖已有账号密码。

登录：

```bash
curl -X POST 'http://localhost:18763/api/v1/auth/login' \
  -H 'system: 1' \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"你的密码"}'
```

业务接口使用参考项目兼容的 `token` 请求头，并在每次请求中携带相同的
`system`；同时兼容标准 Bearer 头：

```bash
curl 'http://localhost:18763/api/v1/auth/me' \
  -H 'system: 1' \
  -H 'token: 登录返回的token'

curl 'http://localhost:18763/api/v1/auth/me' \
  -H 'system: 1' \
  -H 'Authorization: Bearer 登录返回的token'
```

登录失败计数和锁定状态也保存在 Redis：5 分钟内连续失败 5 次后锁定 30 分钟；
登录成功会清理失败状态。调用 `POST /api/v1/auth/logout` 后当前 Token 立即失效。
密码仍使用 BCrypt 保存，不复刻参考项目较旧的双 MD5 密码算法。

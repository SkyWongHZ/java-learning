# 本地 Spring Boot直连 ECS自建 MySQL

## 1. 目标架构

```text
Mac 本地
└── Spring Boot（dev profile）
          │ JDBC公网直连 ECS:3306
          ▼
阿里云 ECS
└── 公网 3306 -> Docker my-mysql:3306
```

当前阶段的重点是学习 Spring Boot真实 CRUD，因此采用与旧 Go/Nest项目相同的公网直连方式。加载 `.env` 后启动 Spring Boot即可，不需要额外保持 MySQL SSH隧道终端。

以后如需收紧公网端口，可以直接使用以下命令临时建立 MySQL 隧道：

```bash
ssh -N -L 127.0.0.1:13306:127.0.0.1:3306 ECS用户@ECS地址
```

是否使用隧道只影响 JDBC连接地址，不影响 Controller、Service、DAO、Mapper或 MyBatis-Plus代码。

## 2. 项目中的环境划分

| profile | 数据库 | 用途 |
| --- | --- | --- |
| `dev` | ECS自建 MySQL（当前公网直连） | 真实数据库接口联调 |
| `test` | 与 dev相同的 ECS自建 MySQL | JUnit 5 + MockMvc自动测试 |

项目不再保留 local/H2配置，默认 profile为 `dev`。日常开发和自动测试统一通过
`scripts/start-dev.sh` 运行；传入 `--test` 时加载相同数据库环境变量并强制使用
`test` profile。

2026-07-19真实环境已确认：MySQL运行在 Docker容器 `my-mysql` 中，版本为 `5.7.43`，数据目录使用 Docker volume持久化；本机可以通过 SSH密钥登录 ECS。

## 3. 先确认 ECS上的 MySQL

登录 ECS后确认安装方式、服务端版本、端口和资源使用情况：

```bash
mysql --version
sudo ss -lntp | grep 3306
free -h
df -h
```

`mysql --version`主要显示客户端版本。能够使用管理员账号时，再查询真实服务端版本：

```bash
mysql -uroot -p -e "SELECT VERSION() AS mysql_version, @@version_comment AS distribution;"
```

如果 MySQL运行在 Docker中：

```bash
docker ps --format 'table {{.Names}}\t{{.Image}}\t{{.Ports}}'
docker exec -it MYSQL容器名 mysql -uroot -p \
  -e "SELECT VERSION() AS mysql_version, @@version_comment AS distribution;"
```

当前项目使用 MySQL Connector/J `8.0.33`，能够连接当前 MySQL `5.7.43`。本阶段不升级 MySQL服务端：升级到8.x还需要验证认证插件、字符集/排序规则、SQL行为以及旧 Go/Nest服务，和当前 Spring Boot CRUD学习目标无关。

## 4. ECS连接前置条件

当前学习阶段需要满足：

- ECS安全组允许当前开发网络访问 `3306`。
- Docker已将 MySQL容器的 `3306` 发布到 ECS宿主机。
- MySQL容器正常运行，并已将 `3306` 发布到 ECS宿主机。
- Spring Boot使用独立数据库和最小权限账号，不复用 `root` 或其他项目账号。

当前已确认公网端口可以从本机访问。安全组收紧、旧服务迁移和 Docker内网化作为后续部署阶段任务，不阻塞本阶段 CRUD。

## 5. 创建独立数据库和账号

当前项目已经完成首次初始化，不需要重复创建数据库和账号。新环境需要手工初始化时，
按以下步骤执行。

数据库管理员在 ECS上执行项目脚本：

```bash
mysql -uroot -p < sql/20260717_create_demo_user.sql
```

然后创建只访问 `springboot_demo` 的账号。账号允许的来源需要匹配 MySQL实际部署方式：宿主机安装通常是 `localhost`/`127.0.0.1`，Docker端口转发可能显示为 Docker网桥地址。

示例仅展示权限范围，实际密码不要写入脚本、Git或聊天记录：

```sql
CREATE USER 'springboot_app'@'连接来源' IDENTIFIED BY '本地生成的强密码';
GRANT SELECT, INSERT, UPDATE, DELETE
ON springboot_demo.*
TO 'springboot_app'@'连接来源';
FLUSH PRIVILEGES;
```

首次建表由管理员账号执行，所以应用账号不需要 `CREATE`、`DROP` 等 DDL权限。

## 6. 准备本地环境变量

复制示例文件：

```bash
cp .env.example .env
```

编辑 `.env`：

- `DB_URL`：指向 `jdbc:mysql://ECS公网IP:3306/springboot_demo`。
- `DB_USERNAME`：独立的 `springboot_app` 账号。
- `DB_PASSWORD`：数据库密码。
- `ECS_HOST`、`ECS_USER` 等用于 `start-dev.sh` 自动建立 Redis SSH 隧道。

`.env` 已被 `.gitignore` 排除。不要把文件内容提交到 Git，也不要通过聊天发送真实密码。

## 7. 检查公网直连

先检查 ECS MySQL端口是否可达：

```bash
nc -vz ECS公网IP 3306
```

本机安装了 MySQL客户端时，可以先绕过 Spring Boot验证账号、版本和数据库：

```bash
mysql --protocol=TCP \
  -h ECS公网IP \
  -P 3306 \
  -u springboot_app \
  -p \
  -e "SELECT VERSION(), DATABASE();" springboot_demo
```

密码通过交互提示输入，不要把密码直接放在命令行中。

需要切回 MySQL SSH 隧道时，可以执行第 1 节给出的 `ssh -N -L` 命令，并临时把
`DB_URL` 主机端口改为 `127.0.0.1:13306`；这不是当前默认开发流程。

## 8. 启动 Spring Boot

项目根目录执行：

```bash
./scripts/start-dev.sh
```

脚本会自动加载 `.env`、使用 Java 17并校验 `dev` profile和数据库环境变量。按 `Ctrl+C` 停止应用。

`application-dev.properties` 从 `.env` 的 `DB_URL` 读取连接地址，当前形态为：

```text
jdbc:mysql://ECS公网IP:3306/springboot_demo
```

当前 JDBC URL延续学习项目的 `sslMode=DISABLED` 配置。MySQL TLS和公网入口收紧留到部署与安全阶段统一处理。

为了适应 2核 ECS，dev环境 HikariCP默认最多建立5个连接、最少保留1个空闲连接。

当前 MySQL容器使用 UTC。dev/test连接池在每条连接创建时执行 `SET time_zone = '+08:00'`，确保数据库自动更新时间和接口的 `Asia/Shanghai` 时间约定一致。

## 9. 自动测试

项目的测试不再使用 H2。执行：

```bash
./scripts/start-dev.sh --test
```

脚本会加载 `.env`、自动管理 Redis 隧道、切换到 `test` profile并连接与 dev相同的
ECS MySQL。CRUD测试使用每次运行唯一的用户名，并由 Spring测试事务在用例结束后
回滚；共享数据库仍意味着测试依赖网络和 ECS MySQL可用性，不应在测试中加入清表
或操作非测试数据的语句。MySQL自增序列不会随事务回滚，因此测试后出现不连续的
ID属于预期现象。

## 10. CRUD联调

创建用户：

```bash
curl -X POST 'http://localhost:18763/api/v1/users' \
  -H 'Content-Type: application/json' \
  -d '{"username":"ecs-user","displayName":"ECS MySQL User"}'
```

查询用户：

```bash
curl 'http://localhost:18763/api/v1/users/1'
```

在 ECS上确认数据：

```sql
SELECT id, username, display_name, gmt_create, gmt_modify, deleted
FROM springboot_demo.demo_user
ORDER BY id DESC;
```

## 11. 常见问题

### `Communications link failure`

检查本机是否能访问 ECS公网 `3306`、安全组是否允许当前出口 IP，以及 Docker是否正确发布端口。

### `Access denied for user`

账号密码错误，或 MySQL账号的 Host来源不允许公网客户端。检查应用账号配置和 MySQL错误日志，不要改用 `root`。

### `Connection refused`

通常是 ECS上的 `127.0.0.1:3306` 没有服务监听。如果 MySQL在 Docker内且未发布宿主机端口，需要将 `REMOTE_MYSQL_HOST`/`REMOTE_MYSQL_PORT`改成 ECS能访问的实际地址。

### 换网络后无法连接

公网出口 IP可能已经变化，需要检查 ECS安全组来源规则。若不想临时调整3306规则，
可以改用第 1 节的手工 SSH 隧道命令。

## 12. 验收结果

2026-07-19已经完成：

- `SELECT VERSION()` 返回 MySQL `5.7.43`。
- 本机 Spring Boot通过 ECS公网 `3306` 直连 MySQL，不依赖 SSH隧道。
- 应用使用独立 `springboot_app`，只有 `SELECT`、`INSERT`、`UPDATE`、`DELETE` 权限。
- `SPRING_PROFILES_ACTIVE=dev` 启动成功，连接池名称为 `SpringbootDemoDevHikariPool`。
- 创建、查询、修改、逻辑删除接口均已操作 ECS上的 `demo_user`。
- 接口时间与 MySQL原始数据均按 `Asia/Shanghai` 对齐。
- `./scripts/start-dev.sh --test` 使用 test profile连接 ECS MySQL；测试数据使用唯一
  用户名并由事务回滚。

后续部署与安全阶段再处理：将 ECS上的应用改为 Docker内网连接、收紧公网3306、按需启用 MySQL TLS或 SSH隧道。本阶段不升级 MySQL 5.7.43。

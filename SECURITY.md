# 安全配置约定

本仓库只提交代码、公开配置模板和部署定义，不提交任何真实访问凭证。

## 本地配置

- 从 `.env.example` 或 `*.env.example` 复制本地配置。
- 真实 `.env`、`runtime.env`、`images.env` 和 `*.local` 文件必须留在本机或服务器。
- 不要使用 `git add -f` 强制加入被 `.gitignore` 排除的文件。

## 云效与阿里云

- ACR 用户名、固定密码等认证信息只保存在云效私密变量或阿里云凭据服务中。
- 流水线 YAML 中的服务连接 ID、主机组 ID、地域、仓库名和 Registry 地址只是资源标识，不包含登录权限。
- ECS 上的 `/etc/springboot-demo-test/runtime.env` 和
  `/etc/springboot-demo-test/images.env` 权限应保持为仅管理员可读。

## 前端环境变量

所有 `VITE_*` 变量都会进入浏览器构建产物，因此不得存放密码、Token 或私钥。
需要认证的操作应由后端完成。

## 泄露处置

如果凭证曾被提交或出现在日志中，应立即在对应平台撤销或轮换。仅从 Git 历史中删除并不能代替凭证轮换。

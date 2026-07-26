# 学生信息管理系统前端

基于原 `bajie-ops-frontend` Vue 脚手架改造的学生信息管理后台，对接同级目录下的 `springboot-demo`。

技术栈：Vue 3.4、Vite 5、TypeScript、Vue Router 4、Pinia、Element Plus 2.7、Axios、SCSS。

## 本地运行

```sh
npm install
npm run dev
```

开发环境默认访问 `http://localhost:18763/` 的 Spring Boot 接口。前端开发地址通常为：

```text
http://localhost:5173/operation/
```

## 常用命令

```sh
npm run type-check
npm run build:dev
npm run build:prod
npm run preview
```

## 页面

- `/login`：账号密码登录。
- `/students`：学生分页查询、详情、新增、修改、删除。
- `/courses`：课程全量查询、新增、修改、删除。
- `/classes`：班级全量查询、新增、修改、删除。

## 接口约定

- 后端地址由 `VITE_BASE_URL` 配置。
- 请求头统一携带 `system: 1`。
- 登录后的请求携带 `token`。
- `code = 1` 表示成功；其他业务失败直接显示后端 `msg`。
- `code = 8` 或 `11` 时清理登录信息并返回登录页。
- 不使用 AES 请求加密。

详细页面范围与验收标准见 [学生信息管理前端实现与验收](docs/学生信息管理前端实现与验收.md)。

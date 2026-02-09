# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working in this repository.

## 项目概述

AI 知识库（ai-kb）是一个基于 Spring Boot + Vue 3 的前后端分离项目，提供文档管理、知识库管理、部门管理和用户权限管理等功能。

## 开发命令

### 后端 (Java Spring Boot)
```bash
# 进入后端目录
cd backend

# 使用 Maven 构建项目
mvn clean compile

# 运行后端服务 (默认端口 8080)
mvn spring-boot:run

# 打包
mvn clean package
```

### 前端 (Vue 3 + Vite)
```bash
# 进入前端目录
cd frontend

# 安装依赖
npm install

# 运行开发服务器 (端口 5173，代理 /api 到 http://localhost:8080)
npm run dev

# 构建生产版本
npm run build

# 预览生产构建
npm run preview
```

## 架构概览

### 后端架构

**包结构**：`com.itpan.backend`

- **config**: 配置类
  - `SecurityConfig`: JWT 认证配置，无状态会话，允许 `/api/auth/**` 和 `/api/public/**` 公开访问
  - `MyBatisPlusConfig`: 分页插件、乐观锁插件、自动填充（createTime/updateTime/createBy/updateBy）
  - `OssConfig`: 阿里云 OSS 配置

- **controller**: REST API 控制器
  - `AuthController`: 认证（登录、注册、刷新 token）
  - `UserController`: 用户管理
  - `DepartmentController`: 部门管理
  - `KnowledgeBaseController`: 知识库管理
  - `DocumentController`: 文档管理
  - `RecycleBinController`: 回收站管理
  - `ResourceController`: 资源上传（头像上传 `/api/resource/avatar`）

- **service**: 业务逻辑接口
- **service/impl**: 业务逻辑实现

- **mapper**: MyBatis-Plus 数据访问层

- **model**: 数据模型
  - `entity`: 实体类，继承 `BaseEntity`（包含 id、审计字段、逻辑删除 deleted、乐观锁 version）
  - `dto`: 数据传输对象（按模块分目录，如 `dto/user/UserCreateDTO.java`）
  - `vo`: 视图对象

- **security**: 安全相关
  - `JwtAuthenticationFilter`: JWT 过滤器
  - `JwtAuthenticationEntryPoint`: 认证入口点
  - `CustomUserDetails`: 自定义用户详情

- **util**: 工具类
  - `JwtUtil`: JWT 生成和验证
  - `DocumentParser`: Apache Tika 文档解析
  - `OssUtil`: 阿里云 OSS 上传
  - `UserContext`: ThreadLocal 存储当前用户信息（getUserId()、getDeptId()）

- **common/enums**: 枚举类（如 `KBOwnerType`）

**关键技术栈**：
- Spring Boot 3.5.9
- Spring Security (JWT 认证)
- MyBatis-Plus 3.5.6 (分页、乐观锁、逻辑删除)
- MySQL
- 阿里云 OSS (文件存储)
- Apache Tika (文档解析)
- Lombok

**安全配置**：
- 认证方式：JWT (Access Token 1小时，Refresh Token 7天)
- 公开端点：`/api/auth/**`，`/api/public/**`，OPTIONS 请求
- 密码加密：BCrypt
- 逻辑删除：`deleted` 字段（1=已删除，0=未删除）

### 前端架构

**目录结构**：
- `api`: API 请求封装（使用 `http` 工具，类型化返回值）
- `router`: Vue Router 路由配置
- `stores`: Pinia 状态管理
- `utils`: `request.ts` - axios 实例（拦截器、401 自动跳转登录）
- `views`: 页面组件
- `layouts`: 布局组件（MainLayout.vue）
- `types`: TypeScript 类型定义

**关键技术栈**：
- Vue 3.5.24 + TypeScript
- Vite 7.2.4
- Element Plus 2.13.1
- Pinia 3.0.4
- Vue Router 4.6.4
- Axios 1.13.4

**请求配置**：
- baseURL: `VITE_API_BASE_URL` 环境变量，默认 `http://localhost:8080`
- 超时：60秒
- Token 存储：localStorage (`access_token`, `refresh_token`, `user_info`)
- 响应拦截：`success: false` 时显示错误消息
- 开发代理：`/api` → `http://localhost:8080`

## 核心业务模块

### 认证与授权
- 双 token 机制（access + refresh）
- 用户信息存储在 ThreadLocal（UserContext）

### 用户管理
- DTO 模式：`UserCreateDTO`、`UserUpdateDTO`、`UserUpdateAdminDTO`、`PasswordChangeDTO`
- 头像上传：`POST /api/resource/avatar`，返回 `{ success, data: url, hash: fileHash }`
- 头像去重：通过 MD5 hash 查询 User 表避免重复上传

### 部门管理
- 树形结构，前端使用 `el-tree-select` 组件
- leader 字段可能返回 User 对象或 userId

### 知识库管理
- 三种空间类型：`KBOwnerType.PERSONAL(10)`、`DEPARTMENT(20)`、`ENTERPRISE(30)`
- 可见性：`visibility` 字段（0-私有, 1-公开）

### 文档管理
- 文档上传 OSS 后用 Apache Tika 解析内容
- 支持目录和文件层级
- 回收站：逻辑删除的文档/知识库

## 重要配置文件

- **后端配置**: `backend/src/main/resources/application.yml`
  - MySQL 数据源、JWT 配置、阿里云 OSS、MyBatis-Plus 配置
  - 逻辑删除字段 `deleted`，SQL 日志开启

- **前端配置**: `frontend/vite.config.ts`
  - 路径别名：`@` → `src`

## 开发注意事项

1. **后端 DTO 模式**：控制器参数应使用 DTO 而非 Entity
2. **前端 API 调用**：使用封装的 `http` 工具，返回值需指定类型
3. **头像上传**：需保存 `avatarUrl` 和 `avatarHash` 两个字段
4. **部门树**：`getTree()` 返回树形结构，不要扁平化
5. **删除操作**：后端使用 `@RequestBody` 接收参数（如 deleteUser @RequestBody Long id）

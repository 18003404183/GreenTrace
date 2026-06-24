# EnvWatch 公境守望

> 环保公众监督系统。基于 Vue 3 + Spring Boot + MyBatis 的公众举报、监督派单、预警分析一体化平台。

EnvWatch 面向环境保护公众监督场景，模拟“公众举报 -> 后台受理 -> 任务派单 -> 现场处理 -> 预警分析 -> 公众查询进度”的完整业务闭环。项目适合作为 Java Web、软件工程课程设计、毕业设计原型或前后端分离综合实践项目。

## 项目亮点

- 前后端分离：Vue 3 + Vite 前端，Spring Boot 3 后端
- 角色权限完整：管理员、执法监督员、数据分析员三类角色
- 后端接口拦截：基于 token 的登录校验和角色访问控制
- 监督闭环清晰：举报、受理、派单、办理记录、办结归档
- 预警能力完善：人工预警、规则预警、任务逾期预警
- 消息通知中心：新举报、任务、逾期、预警自动生成提醒
- 数据分析看板：举报分类、区域热度、状态统计、设备在线率
- 公众端模拟：公众举报提交和办理进度查询
- 接口文档可视化：内置 Swagger / OpenAPI 文档
- 本地即可运行：使用 H2 文件数据库，无需额外安装 MySQL

## 技术栈

| 层级 | 技术 |
| --- | --- |
| 前端 | Vue 3、Vite、原生 CSS |
| 后端 | Spring Boot 3.5、Spring MVC、MyBatis |
| 数据库 | H2 File Database |
| 接口文档 | springdoc-openapi / Swagger UI |
| 文件上传 | Spring Multipart，本地 `uploads` 存储 |
| 构建工具 | Maven Wrapper、npm |

## 功能模块

| 模块 | 功能说明 |
| --- | --- |
| 用户权限 | 登录、角色菜单、接口级权限校验、用户列表 |
| 区域管理 | 区域网格、区域编码、负责人、风险等级 |
| 设备对接 | 设备台账、设备状态、监测指标、历史趋势 |
| 监督数据 | 举报录入、举报查询、受理、办结、详情时间线 |
| 任务派单 | 关联举报生成任务、负责人、截止时间、任务状态 |
| 数据分析 | 看板指标、举报类型分布、处理状态分析、区域热度 |
| 监督预警 | 人工预警、规则预警、逾期预警、预警处置 |
| 消息通知 | 未读提醒、系统消息、标记已读 |
| 公众模块 | 公告展示、公众举报、进度查询 |
| 操作日志 | 登录、派单、状态流转、导出、预警生成留痕 |
| 报表导出 | 举报、任务、预警 CSV 导出 |
| 监管地图 | 设备、举报、预警点位展示 |

## 项目结构

```text
.
├── backend/                 # Spring Boot 后端
│   ├── src/main/java/       # Java 业务代码
│   ├── src/main/resources/  # 配置、schema.sql、data.sql
│   ├── data/                # H2 数据文件，本地运行后生成
│   └── uploads/             # 上传附件，本地运行后生成
├── frontend/                # Vue 3 前端
│   ├── src/
│   │   ├── App.vue
│   │   ├── main.js
│   │   └── style.css
│   └── vite.config.js
├── API.md                   # 接口说明
├── DESIGN.md                # 设计说明
└── README.md
```

## 环境要求

- JDK 17+
- Node.js 18+
- npm 9+

## 快速启动

### 1. 启动后端

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

后端默认运行在：

```text
http://localhost:8080
```

常用地址：

- Swagger UI：`http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON：`http://localhost:8080/v3/api-docs`
- H2 控制台：`http://localhost:8080/h2-console`

H2 连接信息：

```text
JDBC URL: jdbc:h2:file:./data/env_supervision
User Name: sa
Password: 留空
```

### 2. 启动前端

```powershell
cd frontend
npm install
npm run dev
```

前端默认运行在：

```text
http://localhost:5173
```

Vite 已配置代理，前端请求 `/api` 会自动转发到 `http://localhost:8080`。

## 演示账号

| 角色 | 用户名 | 密码 | 主要权限 |
| --- | --- | --- | --- |
| 管理员 | `admin` | `123456` | 全部模块、用户管理、日志查看 |
| 执法监督员 | `inspector` | `123456` | 举报处理、任务派单、预警处置 |
| 数据分析员 | `analyst` | `123456` | 数据看板、设备趋势、分析统计 |

## 推荐演示流程

1. 使用 `admin / 123456` 登录系统，展示数据看板和完整菜单。
2. 进入“公众模块”，模拟提交一条环境问题举报。
3. 进入“消息通知”，展示新举报自动生成待办提醒。
4. 进入“监督数据”，查看举报台账和举报详情。
5. 对举报执行“受理”和“派单”，生成现场核查任务。
6. 进入“任务派单”，查看任务负责人、截止时间和状态流转。
7. 执行“逾期检查”，演示逾期任务自动生成预警和通知。
8. 进入“监督预警”，按规则生成预警并标记处置。
9. 回到举报详情，追加整改反馈或回访确认记录，并办结归档。
10. 使用公众查询功能，通过手机号或举报编号查询办理进度。
11. 切换 `inspector` 和 `analyst` 账号，展示不同角色的菜单和接口权限差异。

## 接口示例

登录：

```http
POST /api/auth/login
Content-Type: application/json
```

```json
{
  "username": "admin",
  "password": "123456"
}
```

访问受保护接口时携带 token：

```http
Authorization: Bearer <token>
```

更多接口请查看：

- [API.md](API.md)
- `http://localhost:8080/swagger-ui/index.html`

## 数据持久化

项目使用 H2 文件数据库，运行后会生成：

```text
backend/data/env_supervision.mv.db
```

上传附件会保存到：

```text
backend/uploads/
```

这些运行时数据不建议提交到 GitHub，仓库已通过 `.gitignore` 忽略。

## 构建与测试

后端测试：

```powershell
cd backend
.\mvnw.cmd test
```

前端构建：

```powershell
cd frontend
npm run build
```

## 文档

- [接口文档](API.md)
- [设计说明](DESIGN.md)

## 开源协议

本项目推荐使用 [MIT License](LICENSE)。

选择 MIT 的原因：

- 足够宽松，允许学习、复制、修改、分发和二次开发
- 很适合课程设计、毕业设计、个人作品集这类展示型项目
- 使用者只需要保留版权和协议声明，理解成本低
- GitHub 对 MIT 识别友好，仓库页面会自动展示许可证信息

> 说明：如果项目后续用于商业交付、政府真实业务或包含第三方私有代码，建议在正式发布前再确认许可证和依赖合规性。

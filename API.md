# 环保公众监督系统接口文档

后端地址：`http://localhost:8080`  
Swagger UI：`http://localhost:8080/swagger-ui/index.html`  
统一返回格式：

```json
{
  "success": true,
  "message": "ok",
  "data": {}
}
```

除 `/api/auth/login`、`/api/public/**`、`/uploads/**` 和 Swagger/H2 页面外，业务接口需要请求头：

```http
Authorization: Bearer <login-token>
```

## 认证

- `POST /api/auth/login`：登录，返回 token 和用户信息

## 用户权限

- `GET /api/users`：用户列表，管理员
- `POST /api/users`：新增用户，管理员

## 区域管理

- `GET /api/areas`：区域列表，登录用户
- `POST /api/areas`：新增区域，管理员

## 设备对接

- `GET /api/devices`：设备列表，管理员/数据分析员
- `POST /api/devices`：新增设备，管理员
- `PATCH /api/devices/{id}/status`：更新设备状态，管理员
- `GET /api/devices/{id}/readings`：设备历史趋势，管理员/数据分析员

## 监督数据

- `GET /api/reports`：举报线索列表，管理员/执法监督员
- `GET /api/reports/{id}`：举报详情，包含办理记录、任务、附件
- `POST /api/reports`：后台录入举报
- `PATCH /api/reports/{id}/status`：受理或办结举报
- `POST /api/reports/{id}/records`：追加办理记录
- `POST /api/reports/{id}/attachments`：上传举报证据附件，`multipart/form-data`

附件上传字段：

- `file`：图片文件
- `usageType`：附件类型，例如 `现场证据`
- `uploadedBy`：上传人

## 任务派单

- `GET /api/tasks`：任务列表，管理员/执法监督员
- `POST /api/tasks`：创建任务
- `PATCH /api/tasks/{id}/status`：更新任务状态
- `POST /api/tasks/check-overdue`：检查逾期任务并生成预警和通知

## 数据分析

- `GET /api/analysis/overview`：看板统计、举报分类、区域热度、状态统计

## 监督预警

- `GET /api/warnings`：预警列表
- `POST /api/warnings`：新增预警，管理员/执法监督员
- `PATCH /api/warnings/{id}/status`：更新预警状态，管理员/执法监督员
- `GET /api/warnings/rules`：预警规则列表
- `POST /api/warnings/rules`：新增预警规则，管理员
- `PATCH /api/warnings/rules/{id}/enabled`：启停规则，管理员
- `POST /api/warnings/generate`：按设备最新读数生成预警，管理员/执法监督员

## 消息通知

- `GET /api/notifications`：消息列表，登录用户
- `PATCH /api/notifications/{id}/read`：标记消息已读

## 公众模块

- `GET /api/public/announcements`：公众公告
- `POST /api/public/reports`：公众提交举报
- `GET /api/public/progress?phone=13800000001`：按手机号查询办理进度
- `GET /api/public/progress?reportId=1`：按举报编号查询办理进度

## 监管地图

- `GET /api/map-points`：地图点位列表，包含设备、举报、预警点位

## 操作日志

- `GET /api/logs`：关键操作日志列表，管理员

## 报表导出

- `GET /api/exports/reports`：导出监督数据 CSV
- `GET /api/exports/tasks`：导出任务派单 CSV
- `GET /api/exports/warnings`：导出预警 CSV

--
task-management-api
核心任务管理API

运行环境：JDK 25、Maven。执行 `mvn spring-boot:run` 后，服务监听默认的 8080 端口。
启动类为 `cn.bugstack.TaskApplication`。已包含 Spring Web、Validation、Spring Data JPA 和 PostgreSQL JDBC 驱动。
默认使用 `local` 配置，暂不连接数据库。
任务目前保存在进程内存中，重启后会清空。

健康检查：请求 `GET http://localhost:8080/health`，返回 HTTP 200 和 `{"status":"UP"}`。
该接口检查应用是否可以响应 HTTP 请求，不检查数据库连接。

启用 PostgreSQL 时，先创建数据库 `task_management`，然后设置连接信息并启动（PowerShell）：

```powershell
$env:SPRING_PROFILES_ACTIVE = "postgres"
$env:DB_URL = "jdbc:postgresql://localhost:5432/task_management"
$env:DB_USERNAME = "postgres"
$env:DB_PASSWORD = "你的数据库密码"
mvn spring-boot:run
```

`postgres` 配置启用数据库连接，但当前任务接口仍使用内存存储；实体的 JPA 映射与数据库持久化尚未实现。
执行 `mvn test` 运行测试，其中健康检查测试会启动真实 HTTP 服务并验证响应。

任务接口：

- `POST /tasks`：创建任务，JSON 示例：`{"title":"准备发布","description":"检查清单","projectId":"00000000-0000-0000-0000-000000000001","assigneeId":null,"priority":"HIGH","dueDate":"2026-09-30T18:00:00"}`。`title`、`projectId`、`priority` 必填；`assigneeId` 和 `dueDate` 可省略。
- `GET /tasks`、`GET /tasks/{id}`：列出或查询任务。
- `PATCH /tasks/{id}`：更新 `title`、`description`、`priority`、`dueDate` 中提供的字段。
- `POST /tasks/{id}/start`、`/complete`、`/cancel`：改变任务状态。
- `DELETE /tasks/{id}`：删除任务。

无截止时间的任务不会出现在逾期和最早截止时间统计中；未分配负责人的任务不会计入按负责人统计。
--

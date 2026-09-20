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

任务接口统一使用 `/api/tasks`（替代原来的 `/tasks`）：

| 方法 | 路径 | 行为 | 成功状态码 |
| --- | --- | --- | --- |
| POST | `/api/tasks` | 创建任务，返回任务及 Location 头 | 201 |
| GET | `/api/tasks/{id}` | 查询单个任务 | 200 |
| GET | `/api/tasks` | 按创建时间列出任务，空列表返回 `[]` | 200 |
| PATCH | `/api/tasks/{id}` | 部分更新任务，返回更新结果 | 200 |
| DELETE | `/api/tasks/{id}` | 删除任务，响应无正文 | 204 |

创建请求示例：

```json
{
  "title": "准备发布",
  "description": "检查清单",
  "projectId": "00000000-0000-0000-0000-000000000001",
  "assigneeId": null,
  "priority": "HIGH",
  "dueDate": null
}
```

`title`、`projectId`、`priority` 必填，标题不能全为空白。`description`、`assigneeId`、`dueDate` 可省略。
优先级为 `LOW`、`MEDIUM`、`HIGH` 或 `URGENT`。新任务状态固定为 `TODO`。
当前仅校验项目和负责人 ID 格式，不校验关联记录是否存在。

创建和更新请求使用 `CreateTaskRequest`、`UpdateTaskRequest`，统一返回 `TaskResponse`，不直接返回实体。
请求参数校验规则：

| 字段 | 规则 |
| --- | --- |
| title | 创建时必填；非空白，最多 100 字符 |
| description | 可选，最多 2000 字符，允许空字符串 |
| dueDate | 可选；填写时必须是合法的 ISO 本地日期时间，例如 `yyyy-MM-ddTHH:mm:ss`，且晚于服务器当前时间；不带时区 |
| priority | 创建时必填；只接受上述四个枚举名称，不接受数字序号或非法名称 |

更新时对非 null 字段执行同样的校验；校验失败返回 HTTP 400，数据和更新时间均保持不变。
例如过长标题的错误响应包含 `"status":400`，以及 `detail` 中的 `title must be at most 100 characters`；
非法优先级或日期格式的错误响应包含 `"status":400` 和 `Request body must be valid JSON with valid field values`。

PATCH 支持 `title`、`description`、`priority`、`dueDate`。例如 `{"title":"新标题"}` 只修改标题；省略或设为 `null` 的字段保持原值，空对象为无操作。描述可用空字符串清空。
原有状态操作保留为 `POST /api/tasks/{id}/start`、`/complete`、`/cancel`。

任务不存在（含重复删除）返回 `404`；参数、JSON、UUID、日期、枚举值或状态操作不合法时返回 `400`。
错误响应为包含 `status`、`title` 和 `detail` 的 JSON Problem Detail。

启动服务后，在项目根目录使用 PowerShell 执行 curl 冒烟测试：

```powershell
.\scripts\test-task-api.ps1
# 也可以指定服务地址
.\scripts\test-task-api.ps1 -BaseUrl http://localhost:8081
```

脚本调用 `curl.exe`，覆盖五个 CRUD 接口、部分更新、400 和 404 场景，并清理它创建的测试任务。
`mvn test` 还会运行真实 HTTP 集成测试，验证请求绑定、校验、Location、响应内容和失败更新不改变数据。
数据由 `TaskController -> TaskService -> InMemoryTaskRepository` 管理。

无截止时间的任务不会出现在逾期和最早截止时间统计中；未分配负责人的任务不会计入按负责人统计。
--

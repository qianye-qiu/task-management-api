# task-management-api

基于 Spring Boot、Spring Data JPA 和 PostgreSQL 的任务管理 API，支持任务增删改查及状态流转。

## 运行环境

- JDK 25、Maven。
- PostgreSQL。
- Spring Boot 4.1.1，PostgreSQL JDBC Driver 42.7.13（以 `pom.xml` 为准）。

启动类：`cn.bugstack.TaskApplication`。默认 HTTP 端口为 `8080`，默认配置为 `postgres`。

## 数据库准备与启动

先使用数据库管理工具连接 PostgreSQL，创建数据库：

```sql
CREATE DATABASE task_management;
```

然后连接到 `task_management`，手动执行 [建表脚本](src/main/resources/db/schema-postgresql.sql)。如果使用 `psql`，可在项目根目录执行：

```powershell
psql -h localhost -p 5432 -U postgres -d task_management -W -v ON_ERROR_STOP=1 -f src/main/resources/db/schema-postgresql.sql
```

脚本创建 `users`、`projects`、`tasks` 表。当前配置为 `spring.jpa.hibernate.ddl-auto=validate`，应用启动时仅校验 JPA 映射对应的表结构，不自动建表；项目也未配置自动执行上述 SQL 文件。脚本中的 `CREATE TABLE IF NOT EXISTS` 不会更新已有表的结构。

当前 [数据库配置](src/main/resources/application-postgres.properties) 读取以下环境变量：

| 变量 | 默认值或要求 |
| --- | --- |
| `DB_URL` | `jdbc:postgresql://localhost:5432/task_management` |
| `DB_USERNAME` | `postgres` |
| `POSTGRES_PASSWORD` | 必须提供数据库用户的实际密码，无默认值 |

在 PowerShell 中启动：

```powershell
$env:DB_URL = "jdbc:postgresql://localhost:5432/task_management"
$env:DB_USERNAME = "postgres"
$env:POSTGRES_PASSWORD = "你的数据库密码"
mvn spring-boot:run
```

在 IDEA 中运行时，在 **Run → Edit Configurations → TaskApplication → Environment variables** 中设置相同变量，再运行启动类。`${POSTGRES_PASSWORD}` 表示读取该配置值；当前代码不读取 `DB_PASSWORD`。单独在终端设置环境变量不会传给已打开的 IDEA。

启动后访问 `GET http://localhost:8080/health`，正常返回：

```json
{"status":"UP"}
```

该接口只返回应用响应状态，不执行数据库健康检查。

## HTTP 接口

| 方法 | 路径 | 功能 | 成功状态码 |
| --- | --- | --- | --- |
| GET | `/health` | 应用健康检查 | 200 |
| POST | `/api/tasks` | 创建任务，返回任务及 Location 响应头 | 201 |
| GET | `/api/tasks` | 查询全部任务 | 200 |
| GET | `/api/tasks/{id}` | 查询单个任务 | 200 |
| PATCH | `/api/tasks/{id}` | 部分更新任务 | 200 |
| POST | `/api/tasks/{id}/start` | 开始任务 | 200 |
| POST | `/api/tasks/{id}/complete` | 完成任务 | 200 |
| POST | `/api/tasks/{id}/cancel` | 取消任务 | 200 |
| DELETE | `/api/tasks/{id}` | 删除任务，无响应正文 | 204 |

列表按 `createdAt` 升序排列，相同创建时间按 ID 排序；无数据时返回 `[]`，当前没有分页或筛选参数。

### 创建任务

向 `/api/tasks` 发送 `Content-Type: application/json` 请求：

```json
{
  "title": "准备发布",
  "description": "检查清单",
  "projectId": "20000000-0000-4000-8000-000000000001",
  "assigneeId": null,
  "priority": "HIGH",
  "dueDate": null
}
```

| 字段 | 创建规则 |
| --- | --- |
| `title` | 必填，不能全为空白，最多 100 字符 |
| `description` | 可选，最多 2000 字符 |
| `projectId` | 必填，UUID |
| `assigneeId` | 可选，UUID |
| `priority` | 必填，使用 `LOW`、`MEDIUM`、`HIGH`、`URGENT` |
| `dueDate` | 可选，提供时须晚于校验时刻，使用不带时区的 ISO 本地日期时间，例如 `yyyy-MM-ddTHH:mm:ss` |

新任务状态为 `TODO`，ID 由应用生成。当前不校验项目或负责人记录是否存在。

任务响应包含 `id`、`title`、`description`、`projectId`、`assigneeId`、`status`、`priority`、`createdAt`、`updatedAt`、`dueDate`、`completedAt`，不包含数据库的 `version` 字段。

### 更新任务与状态流转

PATCH 支持 `title`、`description`、`priority`、`dueDate`，例如：

```json
{"title":"新标题"}
```

省略或设为 `null` 的字段保持原值；`description` 可用空字符串清空，`dueDate` 不能通过传 `null` 清空。空对象不改变业务字段。提供字段时仍需满足标题、描述长度和未来截止时间约束。PATCH 不提供修改项目、负责人或状态的字段。

状态操作不需要请求体：

| 操作 | 允许的当前状态 | 操作后状态 |
| --- | --- | --- |
| `start` | `TODO` | `IN_PROGRESS` |
| `complete` | `IN_PROGRESS` | `COMPLETED`，同时设置完成时间 |
| `cancel` | 除 `COMPLETED` 外的状态（含 `CANCELLED`） | `CANCELLED` |

### 错误响应

已处理的 API 异常使用 Spring `ProblemDetail`，包含 `status`、`title`、`detail` 等字段。

| 状态码 | 场景 |
| --- | --- |
| 400 | 请求校验失败、无法解析的 JSON 或字段值、无效 UUID、非法状态操作 |
| 404 | 任务不存在，包括重复删除 |
| 409 | 捕获到乐观锁并发更新异常 |

## 数据模型与代码范围

任务持久化调用链为 `TaskController → TaskService → JpaTaskRepository → SpringDataTaskRepository`。
`Task` 是映射到 `tasks` 表的 JPA 实体，使用 UUID 主键、字符串枚举和 `@Version` 乐观锁；Service 写操作使用事务，查询使用只读事务。

| 表 | 当前实现 |
| --- | --- |
| `tasks` | JPA 持久化及任务 HTTP 接口；`project_id`、`assignee_id` 为 UUID，建表脚本未为它们设置外键 |
| `users` | 建表脚本及普通 Java `User` 类；邮箱有唯一约束，尚无 JPA 映射或用户 HTTP 接口 |
| `projects` | 建表脚本、普通 Java `Project` 类及内存仓库类；`owner_id` 外键引用 `users.id`，尚无 JPA 映射或项目 HTTP 接口 |

`User` 类直接接收并保存传入的密码字符串，当前没有登录认证或密码哈希实现。
`TaskStatistics` 提供按状态、优先级分组、按项目和负责人计数、逾期任务查询及最早截止任务查询，尚未暴露为 HTTP 接口。

## 冒烟验证

项目提供 [PowerShell 冒烟脚本](scripts/test-task-api.ps1)。先启动应用，再在项目根目录执行（需要 `curl.exe`）：

```powershell
.\scripts\test-task-api.ps1
# 指定其他服务地址
.\scripts\test-task-api.ps1 -BaseUrl http://localhost:8081
```

脚本验证健康检查、任务创建/查询/列表/更新/删除、部分更新保留原字段，以及部分 400 和 404 场景。脚本会写入测试任务，并在结束时尝试清理；未覆盖状态流转接口或并发冲突。

当前源码中没有 `src/test` 下的 Java 测试，`pom.xml` 虽声明了 Spring Boot 测试依赖，但没有 H2 依赖或独立测试数据库配置。

## 三张表的测试数据

连接到 PostgreSQL 的 `task_management` 数据库，确认已执行 `src/main/resources/db/schema-postgresql.sql` 建表，再在 SQL 控制台中执行以下完整脚本。
按 `users → projects → tasks` 顺序插入，每张表 3 条，项目负责人和任务关联的 ID 相互对应。
`users.password` 中的值仅为测试占位符；当前项目没有登录认证或密码哈希实现。
脚本使用固定 ID 和邮箱，重复执行会触发唯一约束；执行失败后需执行 `ROLLBACK;` 结束失败的事务。

```sql
BEGIN;

-- 用户表
INSERT INTO users (
    id, name, email, password, status, create_time, update_time
) VALUES
(
    '10000000-0000-4000-8000-000000000001',
    '张三', 'zhangsan@example.com', 'TEST_HASH_PLACEHOLDER_1',
    'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
),
(
    '10000000-0000-4000-8000-000000000002',
    '李四', 'lisi@example.com', 'TEST_HASH_PLACEHOLDER_2',
    'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
),
(
    '10000000-0000-4000-8000-000000000003',
    '王五', 'wangwu@example.com', 'TEST_HASH_PLACEHOLDER_3',
    'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
);

-- 项目表：owner_id 对应上面的用户
INSERT INTO projects (
    id, name, description, owner_id, status, create_time, update_time
) VALUES
(
    '20000000-0000-4000-8000-000000000001',
    '任务管理系统', '实现任务创建、分配和状态管理',
    '10000000-0000-4000-8000-000000000001',
    'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
),
(
    '20000000-0000-4000-8000-000000000002',
    '用户管理系统', '实现用户资料和权限管理',
    '10000000-0000-4000-8000-000000000002',
    'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
),
(
    '20000000-0000-4000-8000-000000000003',
    '数据报表系统', '实现任务统计和报表展示',
    '10000000-0000-4000-8000-000000000003',
    'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
);

-- 任务表：覆盖待处理、进行中、已完成状态
INSERT INTO tasks (
    id, title, description, project_id, assignee_id,
    status, priority, created_at, updated_at,
    due_date, completed_at, version
) VALUES
(
    '30000000-0000-4000-8000-000000000001',
    '实现任务创建接口', '支持标题、描述、优先级和截止时间',
    '20000000-0000-4000-8000-000000000001',
    '10000000-0000-4000-8000-000000000001',
    'TODO', 'HIGH',
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP + INTERVAL '7 days', NULL, 0
),
(
    '30000000-0000-4000-8000-000000000002',
    '实现用户资料查询', '根据用户 ID 查询用户基本信息',
    '20000000-0000-4000-8000-000000000002',
    '10000000-0000-4000-8000-000000000002',
    'IN_PROGRESS', 'MEDIUM',
    CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP + INTERVAL '3 days', NULL, 0
),
(
    '30000000-0000-4000-8000-000000000003',
    '完成报表需求整理', '整理任务状态和优先级统计需求',
    '20000000-0000-4000-8000-000000000003',
    '10000000-0000-4000-8000-000000000003',
    'COMPLETED', 'LOW',
    CURRENT_TIMESTAMP - INTERVAL '5 days', CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP + INTERVAL '1 day', CURRENT_TIMESTAMP, 0
);

COMMIT;
```



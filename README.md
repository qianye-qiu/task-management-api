--
task-management-api
核心任务管理API

运行环境：JDK 25、Maven。执行 `mvn spring-boot:run` 后，服务监听默认的 8080 端口。
任务目前保存在进程内存中，重启后会清空。

任务接口：

- `POST /tasks`：创建任务，JSON 示例：`{"title":"准备发布","description":"检查清单","projectId":"00000000-0000-0000-0000-000000000001","assigneeId":null,"priority":"HIGH","dueDate":"2026-09-30T18:00:00"}`。`title`、`projectId`、`priority` 必填；`assigneeId` 和 `dueDate` 可省略。
- `GET /tasks`、`GET /tasks/{id}`：列出或查询任务。
- `PATCH /tasks/{id}`：更新 `title`、`description`、`priority`、`dueDate` 中提供的字段。
- `POST /tasks/{id}/start`、`/complete`、`/cancel`：改变任务状态。
- `DELETE /tasks/{id}`：删除任务。

无截止时间的任务不会出现在逾期和最早截止时间统计中；未分配负责人的任务不会计入按负责人统计。
--
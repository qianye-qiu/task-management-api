CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(254) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL, -- Store a password hash, not plaintext.
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
    CHECK (status IN ('ACTIVE', 'DISABLED')),
    create_time TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS projects (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(2000),
    owner_id UUID NOT NULL REFERENCES users(id),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
    CHECK (status IN ('ACTIVE', 'ARCHIVED')),
    create_time TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

CREATE INDEX IF NOT EXISTS idx_projects_owner_id ON projects(owner_id);

CREATE TABLE IF NOT EXISTS tasks (
    id UUID PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    description VARCHAR(2000),
    project_id UUID NOT NULL,
    assignee_id UUID,
    status VARCHAR(20) NOT NULL,
    priority VARCHAR(20) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    due_date TIMESTAMP(6),
    completed_at TIMESTAMP(6),
    version BIGINT NOT NULL
    );

COMMENT ON TABLE users IS '用户表';
COMMENT ON COLUMN users.id IS '用户唯一标识，由应用生成的 UUID';
COMMENT ON COLUMN users.name IS '用户名称，最多 100 字符';
COMMENT ON COLUMN users.email IS '用户邮箱，唯一且不能为空';
COMMENT ON COLUMN users.password IS '密码哈希，写入前由应用完成哈希处理';
COMMENT ON COLUMN users.status IS '用户状态：ACTIVE 启用，DISABLED 禁用';
COMMENT ON COLUMN users.create_time IS '创建时间，不含时区';
COMMENT ON COLUMN users.update_time IS '最后更新时间，不含时区，由应用维护';

COMMENT ON TABLE projects IS '项目表';
COMMENT ON COLUMN projects.id IS '项目唯一标识，由应用生成的 UUID';
COMMENT ON COLUMN projects.name IS '项目名称，最多 100 字符';
COMMENT ON COLUMN projects.description IS '项目描述，最多 2000 字符，可为空';
COMMENT ON COLUMN projects.owner_id IS '项目负责人 ID，外键关联 users.id';
COMMENT ON COLUMN projects.status IS '项目状态：ACTIVE 活跃，ARCHIVED 已归档';
COMMENT ON COLUMN projects.create_time IS '创建时间，不含时区';
COMMENT ON COLUMN projects.update_time IS '最后更新时间，不含时区，由应用维护';
COMMENT ON INDEX idx_projects_owner_id IS '项目负责人查询索引';

COMMENT ON TABLE tasks IS '任务表';
COMMENT ON COLUMN tasks.id IS '任务唯一标识，由应用生成的 UUID';
COMMENT ON COLUMN tasks.title IS '任务标题，最多 100 字符';
COMMENT ON COLUMN tasks.description IS '任务描述，最多 2000 字符，可为空';
COMMENT ON COLUMN tasks.project_id IS '所属项目 ID，逻辑关联 projects.id，当前未设置外键';
COMMENT ON COLUMN tasks.assignee_id IS '任务负责人 ID，逻辑关联 users.id，当前未设置外键，为空表示未分配';
COMMENT ON COLUMN tasks.status IS '任务状态：TODO 待做，IN_PROGRESS 进行中，COMPLETED 已完成，CANCELLED 已取消';
COMMENT ON COLUMN tasks.priority IS '任务优先级：LOW 低，MEDIUM 中，HIGH 高，URGENT 紧急';
COMMENT ON COLUMN tasks.created_at IS '创建时间，不含时区，由应用维护';
COMMENT ON COLUMN tasks.updated_at IS '最后更新时间，不含时区，由应用维护';
COMMENT ON COLUMN tasks.due_date IS '截止时间，不含时区，为空表示未设置';
COMMENT ON COLUMN tasks.completed_at IS '完成时间，不含时区，任务完成时记录';
COMMENT ON COLUMN tasks.version IS '乐观锁版本号，由 JPA 维护，用于检测并发修改';

-- 员工表
CREATE TABLE employees (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    department VARCHAR(50) NOT NULL,
    position VARCHAR(50) NOT NULL,
    salary DECIMAL(10,2) NOT NULL,
    hire_date DATE NOT NULL,
    email VARCHAR(100) UNIQUE
);

COMMENT ON TABLE employees IS '员工信息表';
COMMENT ON COLUMN employees.id IS '员工ID';
COMMENT ON COLUMN employees.name IS '员工姓名';
COMMENT ON COLUMN employees.department IS '所属部门';
COMMENT ON COLUMN employees.position IS '职位';
COMMENT ON COLUMN employees.salary IS '工资';
COMMENT ON COLUMN employees.hire_date IS '入职日期';
COMMENT ON COLUMN employees.email IS '邮箱地址';

-- 部门表
CREATE TABLE departments (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    manager_id BIGINT DEFAULT NULL,
    budget DECIMAL(15,2) NOT NULL,
    location VARCHAR(50) NOT NULL
);

COMMENT ON TABLE departments IS '部门信息表';
COMMENT ON COLUMN departments.id IS '部门ID';
COMMENT ON COLUMN departments.name IS '部门名称';
COMMENT ON COLUMN departments.manager_id IS '部门经理ID';
COMMENT ON COLUMN departments.budget IS '部门预算';
COMMENT ON COLUMN departments.location IS '办公地点';

-- 项目表
CREATE TABLE projects (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    budget DECIMAL(15,2) NOT NULL
);

COMMENT ON TABLE projects IS '项目信息表';
COMMENT ON COLUMN projects.id IS '项目ID';
COMMENT ON COLUMN projects.name IS '项目名称';
COMMENT ON COLUMN projects.description IS '项目描述';
COMMENT ON COLUMN projects.start_date IS '开始日期';
COMMENT ON COLUMN projects.end_date IS '结束日期';
COMMENT ON COLUMN projects.status IS '项目状态';
COMMENT ON COLUMN projects.budget IS '项目预算';

-- 项目成员关系表
CREATE TABLE project_members (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES projects(id),
    employee_id BIGINT NOT NULL REFERENCES employees(id),
    role VARCHAR(50) NOT NULL,
    join_date DATE NOT NULL
);

COMMENT ON TABLE project_members IS '项目成员关系表';
COMMENT ON COLUMN project_members.id IS '关系ID';
COMMENT ON COLUMN project_members.project_id IS '项目ID';
COMMENT ON COLUMN project_members.employee_id IS '员工ID';
COMMENT ON COLUMN project_members.role IS '在项目中的角色';
COMMENT ON COLUMN project_members.join_date IS '加入项目日期';

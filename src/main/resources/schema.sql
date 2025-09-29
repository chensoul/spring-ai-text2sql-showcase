-- 创建数据库表结构
-- 员工表
CREATE TABLE employees (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '员工ID',
    name VARCHAR(100) NOT NULL COMMENT '员工姓名',
    department VARCHAR(50) NOT NULL COMMENT '所属部门',
    position VARCHAR(50) NOT NULL COMMENT '职位',
    salary DECIMAL(10,2) NOT NULL COMMENT '工资',
    hire_date DATE NOT NULL COMMENT '入职日期',
    email VARCHAR(100) UNIQUE COMMENT '邮箱地址'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='员工信息表';

-- 部门表
CREATE TABLE departments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '部门ID',
    name VARCHAR(50) NOT NULL UNIQUE COMMENT '部门名称',
    manager_id BIGINT DEFAULT NULL COMMENT '部门经理ID',
    budget DECIMAL(15,2) NOT NULL COMMENT '部门预算',
    location VARCHAR(50) NOT NULL COMMENT '办公地点'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='部门信息表';

-- 项目表
CREATE TABLE projects (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '项目ID',
    name VARCHAR(100) NOT NULL COMMENT '项目名称',
    description TEXT COMMENT '项目描述',
    start_date DATE NOT NULL COMMENT '开始日期',
    end_date DATE NOT NULL COMMENT '结束日期',
    status VARCHAR(20) NOT NULL COMMENT '项目状态',
    budget DECIMAL(15,2) NOT NULL COMMENT '项目预算'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目信息表';

-- 项目成员关系表
CREATE TABLE project_members (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '关系ID',
    project_id BIGINT NOT NULL COMMENT '项目ID',
    employee_id BIGINT NOT NULL COMMENT '员工ID',
    role VARCHAR(50) NOT NULL COMMENT '在项目中的角色',
    join_date DATE NOT NULL COMMENT '加入项目日期',
    FOREIGN KEY (project_id) REFERENCES projects(id),
    FOREIGN KEY (employee_id) REFERENCES employees(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目成员关系表';
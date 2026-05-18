-- 维修人员表增加登录字段
ALTER TABLE maintenance_person
ADD COLUMN username VARCHAR(50) COMMENT '登录账号',
ADD COLUMN password VARCHAR(100) COMMENT '密码(BCrypt加密)',
ADD COLUMN last_login_time DATETIME COMMENT '最后登录时间';

-- 报修表增加维修人员ID关联
ALTER TABLE repair
ADD COLUMN maintenance_id BIGINT COMMENT '维修人员ID（关联maintenance_person.id）',
ADD COLUMN accept_time DATETIME COMMENT '维修人员接单时间';

-- 给已有维修人员设置默认账号（根据实际情况修改）
-- UPDATE maintenance_person SET username = CONCAT('wx', id), password = '$2a$10$...' WHERE username IS NULL;

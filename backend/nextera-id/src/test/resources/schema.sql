-- H2测试数据库初始化脚本
DROP TABLE IF EXISTS id_generator;

-- 创建ID生成器表
CREATE TABLE id_generator (
    business_type VARCHAR(50) PRIMARY KEY,
    max_id BIGINT NOT NULL DEFAULT 0,
    step_size INT NOT NULL DEFAULT 1000,
    total_generated BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    remark VARCHAR(255)
);

-- 插入测试数据
INSERT INTO id_generator (business_type, max_id, step_size, total_generated, remark) VALUES
('test-order', 1000, 20, 0, '测试订单ID生成器'),
('test-product', 0, 50, 0, '测试产品ID生成器'); 
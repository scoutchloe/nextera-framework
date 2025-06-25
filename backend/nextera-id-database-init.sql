-- Nextera ID 数据库初始化脚本
-- 用于PostgreSQL数据库

-- 创建数据库（如果不存在）
-- CREATE DATABASE nextera_id;

-- 使用数据库
-- \c nextera_id;

-- 创建ID生成器表
CREATE TABLE IF NOT EXISTS id_generator (
    business_type VARCHAR(50) PRIMARY KEY,
    max_id BIGINT NOT NULL DEFAULT 0,
    step_size INT NOT NULL DEFAULT 1000,
    total_generated BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    remark VARCHAR(255)
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_id_generator_updated_at ON id_generator(updated_at);

-- 创建更新时间触发器函数
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- 创建更新时间触发器
DROP TRIGGER IF EXISTS update_id_generator_updated_at ON id_generator;
CREATE TRIGGER update_id_generator_updated_at
    BEFORE UPDATE ON id_generator
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- 创建获取下一个ID批次的函数
CREATE OR REPLACE FUNCTION get_next_id_batch(
    p_business_type VARCHAR(50),
    p_batch_size INT DEFAULT 100
)
RETURNS TABLE(start_id BIGINT, end_id BIGINT) AS $$
DECLARE
    current_max_id BIGINT;
    new_max_id BIGINT;
BEGIN
    -- 使用行锁确保并发安全
    SELECT max_id INTO current_max_id
    FROM id_generator
    WHERE business_type = p_business_type
    FOR UPDATE;
    
    -- 如果业务类型不存在，则初始化
    IF current_max_id IS NULL THEN
        INSERT INTO id_generator (business_type, max_id, step_size, total_generated)
        VALUES (p_business_type, 0, p_batch_size, 0);
        current_max_id := 0;
    END IF;
    
    -- 计算新的最大ID
    new_max_id := current_max_id + p_batch_size;
    
    -- 更新最大ID和总生成数量
    UPDATE id_generator
    SET max_id = new_max_id,
        total_generated = total_generated + p_batch_size,
        updated_at = CURRENT_TIMESTAMP
    WHERE business_type = p_business_type;
    
    -- 返回ID段
    RETURN QUERY SELECT current_max_id + 1, new_max_id;
END;
$$ LANGUAGE plpgsql;

-- 创建重置ID计数器的函数
CREATE OR REPLACE FUNCTION reset_id_counter(
    p_business_type VARCHAR(50),
    p_start_value BIGINT DEFAULT 1
)
RETURNS BOOLEAN AS $$
BEGIN
    UPDATE id_generator
    SET max_id = p_start_value - 1,
        total_generated = 0,
        updated_at = CURRENT_TIMESTAMP
    WHERE business_type = p_business_type;
    
    -- 如果业务类型不存在，则创建
    IF NOT FOUND THEN
        INSERT INTO id_generator (business_type, max_id, step_size, total_generated)
        VALUES (p_business_type, p_start_value - 1, 1000, 0);
    END IF;
    
    RETURN TRUE;
END;
$$ LANGUAGE plpgsql;

-- 插入初始测试数据
INSERT INTO id_generator (business_type, max_id, step_size, total_generated, remark)
VALUES 
    ('user', 0, 100, 0, '用户ID生成器'),
    ('order', 0, 500, 0, '订单ID生成器'),
    ('article', 0, 50, 0, '文章ID生成器')
ON CONFLICT (business_type) DO NOTHING;

-- 创建用于监控的视图
CREATE OR REPLACE VIEW id_generator_status AS
SELECT 
    business_type,
    max_id,
    step_size,
    total_generated,
    CASE 
        WHEN max_id > 0 THEN 'ACTIVE'
        ELSE 'INACTIVE'
    END as status,
    created_at,
    updated_at,
    remark
FROM id_generator
ORDER BY business_type;

-- 验证函数是否正常工作
DO $$
DECLARE
    result RECORD;
BEGIN
    -- 测试获取ID批次函数
    SELECT * INTO result FROM get_next_id_batch('test', 10);
    RAISE NOTICE '测试获取ID批次: start_id=%, end_id=%', result.start_id, result.end_id;
    
    -- 清理测试数据
    DELETE FROM id_generator WHERE business_type = 'test';
END $$;

-- 显示当前状态
SELECT * FROM id_generator_status; 
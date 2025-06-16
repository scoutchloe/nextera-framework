-- =============================================
-- 用户表索引优化脚本
-- 用于优化查询性能的额外索引
-- =============================================

-- 用户表额外索引
-- 复合索引：状态+删除标志，用于查询正常用户
CREATE INDEX `idx_status_deleted` ON `user` (`status`, `is_deleted`);

-- 复合索引：创建时间+状态，用于按时间排序的用户列表查询
CREATE INDEX `idx_create_time_status` ON `user` (`create_time`, `status`);

-- 复合索引：最后登录时间+状态，用于查询活跃用户
CREATE INDEX `idx_last_login_status` ON `user` (`last_login_time`, `status`);

-- 邮箱部分索引，用于邮箱域名统计
CREATE INDEX `idx_email_domain` ON `user` ((SUBSTRING_INDEX(`email`, '@', -1)));

-- 用户资料扩展表额外索引
-- 复合索引：职业+所在地，用于职业地域分析
CREATE INDEX `idx_profession_location` ON `user_profile` (`profession`, `location`);

-- 年龄索引，用于年龄统计分析
CREATE INDEX `idx_age` ON `user_profile` (`age`);

-- 学历索引，用于学历统计
CREATE INDEX `idx_education` ON `user_profile` (`education`);

-- =============================================
-- 查询性能优化建议
-- =============================================

/*
常用查询优化建议：

1. 查询正常用户列表：
   SELECT * FROM user WHERE status = 0 AND is_deleted = 0 ORDER BY create_time DESC;
   使用索引：idx_status_deleted, idx_create_time_status

2. 查询活跃用户：
   SELECT * FROM user WHERE status = 0 AND last_login_time > DATE_SUB(NOW(), INTERVAL 30 DAY);
   使用索引：idx_last_login_status

3. 用户详情查询（包含资料）：
   SELECT u.*, p.* FROM user u 
   LEFT JOIN user_profile p ON u.id = p.user_id 
   WHERE u.id = ?;
   使用索引：PRIMARY KEY, uk_user_id

4. 按职业和地区查询用户：
   SELECT u.username, u.nickname, p.profession, p.location 
   FROM user u 
   JOIN user_profile p ON u.id = p.user_id 
   WHERE p.profession = ? AND p.location LIKE ?;
   使用索引：idx_profession_location

5. 邮箱域名统计：
   SELECT SUBSTRING_INDEX(email, '@', -1) as domain, COUNT(*) as count 
   FROM user 
   WHERE is_deleted = 0 
   GROUP BY domain;
   使用索引：idx_email_domain

注意事项：
- 索引会占用额外存储空间
- 索引会影响INSERT/UPDATE/DELETE性能
- 根据实际查询需求选择性创建索引
- 定期分析查询性能，调整索引策略
*/ 
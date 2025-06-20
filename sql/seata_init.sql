-- Seata分布式事务回滚日志表
-- 需要在每个参与分布式事务的数据库中创建这些表

-- undo_log表（回滚日志表）
-- 用于存储事务的回滚信息
DROP TABLE IF EXISTS undo_log;
CREATE TABLE undo_log (
    id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'increment id',
    branch_id BIGINT(20) NOT NULL COMMENT 'branch transaction id',
    xid VARCHAR(100) NOT NULL COMMENT 'global transaction id',
    context VARCHAR(128) NOT NULL COMMENT 'undo_log context,such as serialization',
    rollback_info LONGBLOB NOT NULL COMMENT 'rollback info',
    log_status INT(11) NOT NULL COMMENT '0:normal status,1:defense status',
    log_created DATETIME(6) NOT NULL COMMENT 'create datetime',
    log_modified DATETIME(6) NOT NULL COMMENT 'modify datetime',
    PRIMARY KEY (id),
    UNIQUE KEY ux_undo_log (xid, branch_id)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='AT transaction mode undo table';

-- 为nextera_user数据库创建undo_log表
USE nextera_user;
DROP TABLE IF EXISTS undo_log;
CREATE TABLE undo_log (
    id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'increment id',
    branch_id BIGINT(20) NOT NULL COMMENT 'branch transaction id',
    xid VARCHAR(100) NOT NULL COMMENT 'global transaction id',
    context VARCHAR(128) NOT NULL COMMENT 'undo_log context,such as serialization',
    rollback_info LONGBLOB NOT NULL COMMENT 'rollback info',
    log_status INT(11) NOT NULL COMMENT '0:normal status,1:defense status',
    log_created DATETIME(6) NOT NULL COMMENT 'create datetime',
    log_modified DATETIME(6) NOT NULL COMMENT 'modify datetime',
    PRIMARY KEY (id),
    UNIQUE KEY ux_undo_log (xid, branch_id)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='AT transaction mode undo table';

-- 为nextera_article数据库创建undo_log表
USE nextera_article;
DROP TABLE IF EXISTS undo_log;
CREATE TABLE undo_log (
    id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'increment id',
    branch_id BIGINT(20) NOT NULL COMMENT 'branch transaction id',
    xid VARCHAR(100) NOT NULL COMMENT 'global transaction id',
    context VARCHAR(128) NOT NULL COMMENT 'undo_log context,such as serialization',
    rollback_info LONGBLOB NOT NULL COMMENT 'rollback info',
    log_status INT(11) NOT NULL COMMENT '0:normal status,1:defense status',
    log_created DATETIME(6) NOT NULL COMMENT 'create datetime',
    log_modified DATETIME(6) NOT NULL COMMENT 'modify datetime',
    PRIMARY KEY (id),
    UNIQUE KEY ux_undo_log (xid, branch_id)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='AT transaction mode undo table';

-- 为nextera_auth数据库创建undo_log表
USE nextera_auth;
DROP TABLE IF EXISTS undo_log;
CREATE TABLE undo_log (
    id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'increment id',
    branch_id BIGINT(20) NOT NULL COMMENT 'branch transaction id',
    xid VARCHAR(100) NOT NULL COMMENT 'global transaction id',
    context VARCHAR(128) NOT NULL COMMENT 'undo_log context,such as serialization',
    rollback_info LONGBLOB NOT NULL COMMENT 'rollback info',
    log_status INT(11) NOT NULL COMMENT '0:normal status,1:defense status',
    log_created DATETIME(6) NOT NULL COMMENT 'create datetime',
    log_modified DATETIME(6) NOT NULL COMMENT 'modify datetime',
    PRIMARY KEY (id),
    UNIQUE KEY ux_undo_log (xid, branch_id)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='AT transaction mode undo table'; 
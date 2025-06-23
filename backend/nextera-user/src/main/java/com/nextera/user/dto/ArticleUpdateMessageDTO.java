package com.nextera.user.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 文章更新消息DTO
 * 用于RocketMQ事务消息传输
 *
 * @author nextera
 * @since 2025-06-23
 */
@Data
@Accessors(chain = true)
public class ArticleUpdateMessageDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 事务ID
     */
    private String transactionId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 文章ID
     */
    private Long articleId;

    /**
     * 文章标题
     */
    private String title;

    /**
     * 文章内容
     */
    private String content;

    /**
     * 文章分类ID
     */
    private Long categoryId;

    /**
     * 文章摘要
     */
    private String summary;

    /**
     * 文章标签
     */
    private String tags;

    /**
     * 是否发布：0-草稿，1-发布
     */
    private Integer isPublished;

    /**
     * 操作类型：UPDATE, CREATE, DELETE
     */
    private String operationType;

    /**
     * 客户端IP
     */
    private String clientIp;

    /**
     * 用户代理
     */
    private String userAgent;

    /**
     * 操作时间戳
     */
    private Long timestamp;

    /**
     * 扩展信息
     */
    private String extInfo;
} 
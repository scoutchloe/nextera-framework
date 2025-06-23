package com.nextera.user.config;

/**
 * RocketMQ配置类
 *
 * @author nextera
 * @since 2025-06-23
 */
public class RocketMQConfig {

    /**
     * RocketMQ Topic和Tag常量
     */
    public static final class Topics {
        public static final String ARTICLE_UPDATE_TOPIC = "ARTICLE_UPDATE_TOPIC";
        public static final String USER_UPDATE_TOPIC = "USER_UPDATE_TOPIC";
    }

    public static final class Tags {
        public static final String ARTICLE_UPDATE_TAG = "ARTICLE_UPDATE";
        public static final String USER_UPDATE_TAG = "USER_UPDATE";
        public static final String ROLLBACK_TAG = "ROLLBACK";
    }

    public static final class ProducerGroups {
        public static final String ARTICLE_PRODUCER_GROUP = "ARTICLE_PRODUCER_GROUP";
        public static final String USER_PRODUCER_GROUP = "USER_PRODUCER_GROUP";
    }

    public static final class ConsumerGroups {
        public static final String ARTICLE_CONSUMER_GROUP = "ARTICLE_CONSUMER_GROUP";
        public static final String USER_CONSUMER_GROUP = "USER_CONSUMER_GROUP";
    }
} 
package com.nextera.idapi.enums;

/**
 * ID生成器类型枚举
 *
 * @author Nextera Framework
 * @since 1.0.0
 */
public enum IdGeneratorType {
    
    /**
     * Redis方式生成ID
     */
    REDIS("redis", "基于Redis的分布式ID生成"),
    
    /**
     * PostgreSQL方式生成ID
     */
    POSTGRESQL("postgresql", "基于PostgreSQL的分布式ID生成");
    
    private final String code;
    private final String description;
    
    IdGeneratorType(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 根据代码获取枚举
     *
     * @param code 代码
     * @return 枚举
     */
    public static IdGeneratorType fromCode(String code) {
        for (IdGeneratorType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown IdGeneratorType code: " + code);
    }
} 
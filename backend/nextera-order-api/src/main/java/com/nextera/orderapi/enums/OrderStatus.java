package com.nextera.orderapi.enums;

/**
 * 订单状态枚举
 *
 * @author Nextera Framework
 * @since 1.0.0
 */
public enum OrderStatus {

    PENDING_PAYMENT(1, "待支付"),
    PAID(2, "已支付"),
    SHIPPED(3, "已发货"),
    COMPLETED(4, "已完成"),
    CANCELLED(5, "已取消");

    private final Integer code;
    private final String description;

    OrderStatus(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public Integer getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据状态码获取枚举
     *
     * @param code 状态码
     * @return 订单状态枚举
     */
    public static OrderStatus fromCode(Integer code) {
        for (OrderStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 验证状态流转是否合法
     *
     * @param currentStatus 当前状态
     * @param newStatus 新状态
     * @return 是否合法
     */
    public static boolean isValidTransition(Integer currentStatus, Integer newStatus) {
        if (currentStatus.equals(newStatus)) {
            return false;
        }

        // 定义状态流转规则
        switch (currentStatus) {
            case 1: // 待支付
                return newStatus == 2 || newStatus == 5; // 可以支付或取消
            case 2: // 已支付
                return newStatus == 3 || newStatus == 5; // 可以发货或取消
            case 3: // 已发货
                return newStatus == 4; // 可以完成
            case 4: // 已完成
            case 5: // 已取消
                return false; // 终态，不能再变更
            default:
                return false;
        }
    }
} 
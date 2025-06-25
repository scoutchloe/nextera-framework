package com.nextera.orderapi.enums;

/**
 * 支付方式枚举
 *
 * @author Nextera Framework
 * @since 1.0.0
 */
public enum PaymentMethod {

    WECHAT(1, "微信支付"),
    ALIPAY(2, "支付宝"),
    BANK_CARD(3, "银行卡");

    private final Integer code;
    private final String description;

    PaymentMethod(Integer code, String description) {
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
     * 根据代码获取枚举
     *
     * @param code 代码
     * @return 支付方式枚举
     */
    public static PaymentMethod fromCode(Integer code) {
        for (PaymentMethod method : values()) {
            if (method.code.equals(code)) {
                return method;
            }
        }
        return null;
    }
} 
package com.nextera.managenextera.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 带签名的系统角色DTO
 * 用于新增角色接口的签名验证
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysRoleSignDTO extends SysRoleDTO {

    /**
     * 签名
     */
    @NotBlank(message = "签名不能为空")
    private String sign;

    /**
     * 时间戳
     */
    @NotNull(message = "时间戳不能为空")
    private Long timestamp;
} 
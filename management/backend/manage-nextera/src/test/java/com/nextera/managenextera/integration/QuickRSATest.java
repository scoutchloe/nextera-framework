package com.nextera.managenextera.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextera.managenextera.dto.SysRoleDTO;
import com.nextera.managenextera.util.RSAUtil;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

/**
 * 快速RSA测试
 */
public class QuickRSATest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testRSABasicFunctionality() throws Exception {
        // 准备测试数据
        SysRoleDTO roleDTO = new SysRoleDTO();
        roleDTO.setId(1L);
        roleDTO.setRoleName("测试角色");
        roleDTO.setRoleCode("TEST_ROLE");
        roleDTO.setDescription("测试角色描述");
        roleDTO.setStatus(1);
        roleDTO.setPermissionIds(Arrays.asList(1L, 2L, 3L));

        String jsonData = objectMapper.writeValueAsString(roleDTO);
        System.out.println("原始JSON数据: " + jsonData);

        // RSA加密
        String encryptedData = RSAUtil.encrypt(jsonData, RSAUtil.getPublicKey());
        System.out.println("加密后数据: " + encryptedData);
        System.out.println("加密后数据长度: " + encryptedData.length());

        // RSA解密
        String decryptedData = RSAUtil.decrypt(encryptedData);
        System.out.println("解密后数据: " + decryptedData);

        // 验证解密后的数据
        SysRoleDTO decryptedRole = objectMapper.readValue(decryptedData, SysRoleDTO.class);
        System.out.println("解密后的角色对象: " + decryptedRole);

        // 验证数据一致性
        assert roleDTO.getId().equals(decryptedRole.getId());
        assert roleDTO.getRoleName().equals(decryptedRole.getRoleName());
        assert roleDTO.getRoleCode().equals(decryptedRole.getRoleCode());

        System.out.println("RSA加密解密测试通过！");
    }

    @Test
    public void testCurrentErrorData() throws Exception {
        // 测试用户遇到的具体加密数据
        String encryptedData = "bCAEmrH5DtRU/GBF1QNAAGveBBCG0EATW2xKxNHR29mINq0heg12RfgpChgstT4Ug3XTIaN/tZ68+HFD9t/nbcEHcfq5F75In5kDnus4fYKPvr/pyoxaB7/uJiiPwHstvZ2u8ibhRb6onSHwOJT92A+e3pTYHFpqUxeUUWIyFhpFnVj8cphedvwUaYonOv8tRRsG/WojrWRYTZYqz1xo0k3lhgUbJzrr4BOBiiPK/R0EvAZ112fXvwLBUVFXQ09gOB1UQ95fa2NEcPrlwKQmPyuFNfGlqfWIU1pMH8ebTRZyeM5kn2MZ6ZDU4wIxMzeC55BbdjkvbHVgX1BJnr4rUg==";
        
        System.out.println("尝试解密用户的加密数据...");
        System.out.println("加密数据: " + encryptedData);
        
        try {
            String decryptedData = RSAUtil.decrypt(encryptedData);
            System.out.println("解密成功，明文数据: " + decryptedData);
            
            // 尝试解析为JSON
            SysRoleDTO roleDTO = objectMapper.readValue(decryptedData, SysRoleDTO.class);
            System.out.println("解析的角色对象: " + roleDTO);
        } catch (Exception e) {
            System.err.println("解密失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 
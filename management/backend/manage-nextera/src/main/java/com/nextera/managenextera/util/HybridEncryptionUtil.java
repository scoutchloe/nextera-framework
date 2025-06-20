package com.nextera.managenextera.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextera.managenextera.dto.HybridEncryptionRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 混合加密工具类
 * 结合RSA和AES加密，提供安全的数据传输
 */
public class HybridEncryptionUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(HybridEncryptionUtil.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 混合加密：使用RSA加密AES密钥，使用AES加密业务数据
     * @param data 要加密的业务数据
     * @param rsaPublicKey RSA公钥
     * @return 混合加密结果的JSON字符串
     */
    public static String hybridEncrypt(String data, String rsaPublicKey) {
        try {
            // 1. 生成随机AES密钥
            String aesKey = AESUtil.generateKey();
            logger.debug("生成AES密钥成功");
            
            // 2. 使用AES加密业务数据
            String encryptedData = AESUtil.encrypt(data, aesKey);
            logger.debug("AES加密业务数据成功");
            
            // 3. 使用RSA加密AES密钥
            String encryptedKey = RSAUtil.encrypt(aesKey, rsaPublicKey);
            logger.debug("RSA加密AES密钥成功");
            
            // 4. 构造混合加密请求对象
            HybridEncryptionRequest request = new HybridEncryptionRequest(encryptedKey, encryptedData);
            
            // 5. 转换为JSON字符串
            return objectMapper.writeValueAsString(request);
        } catch (Exception e) {
            logger.error("混合加密失败", e);
            throw new RuntimeException("混合加密失败", e);
        }
    }
    
    /**
     * 混合解密：使用RSA解密AES密钥，使用AES解密业务数据
     * @param hybridEncryptedData 混合加密的JSON数据
     * @param rsaPrivateKey RSA私钥
     * @return 解密后的业务数据
     */
    public static String hybridDecrypt(String hybridEncryptedData, String rsaPrivateKey) {
        try {
            // 1. 解析混合加密请求对象
            HybridEncryptionRequest request = objectMapper.readValue(hybridEncryptedData, HybridEncryptionRequest.class);
            logger.debug("解析混合加密请求成功");
            
            // 2. 使用RSA解密AES密钥
            String aesKey = RSAUtil.decrypt(request.getEncryptedKey(), rsaPrivateKey);
            logger.debug("RSA解密AES密钥成功");
            
            // 3. 验证AES密钥格式
            if (!AESUtil.isValidKey(aesKey)) {
                throw new RuntimeException("解密得到的AES密钥格式无效");
            }
            
            // 4. 使用AES解密业务数据
            String decryptedData = AESUtil.decrypt(request.getEncryptedData(), aesKey);
            logger.debug("AES解密业务数据成功");
            
            return decryptedData;
        } catch (Exception e) {
            logger.error("混合解密失败", e);
            throw new RuntimeException("混合解密失败", e);
        }
    }
    
    /**
     * 验证混合加密数据格式是否正确
     * @param hybridEncryptedData 混合加密的JSON数据
     * @return 是否有效
     */
    public static boolean isValidHybridEncryptedData(String hybridEncryptedData) {
        try {
            HybridEncryptionRequest request = objectMapper.readValue(hybridEncryptedData, HybridEncryptionRequest.class);
            return request.getEncryptedKey() != null && 
                   request.getEncryptedData() != null &&
                   !request.getEncryptedKey().trim().isEmpty() &&
                   !request.getEncryptedData().trim().isEmpty();
        } catch (Exception e) {
            logger.debug("混合加密数据格式验证失败: {}", e.getMessage());
            return false;
        }
    }
} 
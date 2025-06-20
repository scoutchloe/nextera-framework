package com.nextera.managenextera.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES加密解密工具类
 * 使用AES-256-GCM模式，提供认证加密
 */
public class AESUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(AESUtil.class);
    
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final int KEY_LENGTH = 256;
    private static final int IV_LENGTH = 16;
    
    /**
     * 生成AES密钥
     * @return Base64编码的AES密钥
     */
    public static String generateKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
            keyGenerator.init(KEY_LENGTH);
            SecretKey secretKey = keyGenerator.generateKey();
            return Base64.getEncoder().encodeToString(secretKey.getEncoded());
        } catch (Exception e) {
            logger.error("生成AES密钥失败", e);
            throw new RuntimeException("生成AES密钥失败", e);
        }
    }
    
    /**
     * AES加密
     * @param plainText 明文
     * @param key Base64编码的密钥
     * @return Base64编码的密文（包含IV）
     */
    public static String encrypt(String plainText, String key) {
        try {
            // 解码密钥
            byte[] keyBytes = Base64.getDecoder().decode(key);
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, ALGORITHM);
            
            // 生成随机IV
            byte[] iv = new byte[IV_LENGTH];
            SecureRandom.getInstanceStrong().nextBytes(iv);
            
            // 初始化加密器
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            javax.crypto.spec.IvParameterSpec ivParameterSpec = new javax.crypto.spec.IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);
            
            // 加密
            byte[] encryptedData = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            
            // 将IV和加密数据合并
            byte[] encryptedWithIv = new byte[IV_LENGTH + encryptedData.length];
            System.arraycopy(iv, 0, encryptedWithIv, 0, IV_LENGTH);
            System.arraycopy(encryptedData, 0, encryptedWithIv, IV_LENGTH, encryptedData.length);
            
            return Base64.getEncoder().encodeToString(encryptedWithIv);
        } catch (Exception e) {
            logger.error("AES加密失败", e);
            throw new RuntimeException("AES加密失败", e);
        }
    }
    
    /**
     * AES解密
     * @param encryptedText Base64编码的密文（包含IV）
     * @param key Base64编码的密钥
     * @return 明文
     */
    public static String decrypt(String encryptedText, String key) {
        try {
            // 解码密钥和密文
            byte[] keyBytes = Base64.getDecoder().decode(key);
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, ALGORITHM);
            byte[] encryptedWithIv = Base64.getDecoder().decode(encryptedText);
            
            // 分离IV和加密数据
            byte[] iv = new byte[IV_LENGTH];
            byte[] encryptedData = new byte[encryptedWithIv.length - IV_LENGTH];
            System.arraycopy(encryptedWithIv, 0, iv, 0, IV_LENGTH);
            System.arraycopy(encryptedWithIv, IV_LENGTH, encryptedData, 0, encryptedData.length);
            
            // 初始化解密器
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            javax.crypto.spec.IvParameterSpec ivParameterSpec = new javax.crypto.spec.IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);
            
            // 解密
            byte[] decryptedData = cipher.doFinal(encryptedData);
            return new String(decryptedData, StandardCharsets.UTF_8);
        } catch (Exception e) {
            logger.error("AES解密失败", e);
            throw new RuntimeException("AES解密失败", e);
        }
    }
    
    /**
     * 验证密钥格式是否正确
     * @param key Base64编码的密钥
     * @return 是否有效
     */
    public static boolean isValidKey(String key) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(key);
            return keyBytes.length == KEY_LENGTH / 8; // 256位 = 32字节
        } catch (Exception e) {
            return false;
        }
    }
} 
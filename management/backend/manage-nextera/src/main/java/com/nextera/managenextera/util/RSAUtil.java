package com.nextera.managenextera.util;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * RSA加密解密工具类
 */
@Slf4j
public class RSAUtil {

    private static final String RSA_ALGORITHM = "RSA";
    private static final int KEY_SIZE = 2048;

    /**
     * RSA公钥（用于前端加密）
     * 实际项目中应该从配置文件或密钥管理服务获取
     */
    private static String PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtyM8/s3uzLdBUDxrrZz4Bu0YvSwpdGdzp3/1Nz6GIGcTcFVoFrwMrCbCnXXEVM6odTibved1zEFFubz+XAE8qapwRBjzmw64Tkz6ELQq2hM2tQY9alTg3Fcdg/cnKgHLksd8X6wBk3ORR21DN6LFTpEEslc9ByJbz3lx702lpZmdUy4pXzT5YtkT0hsC6BzDR/pp9HYaKT995slPF7cjU6cxZEVTApfnqi2aO9RVWjAHLkHx3xlIYbr/TKk/ZYfLhEZlFbi27jlgOVTav6cXBjiPbhDBrwBU7mQwrftDQeTAVNQ8cDIDLOluTZQyLqDarIYVMPnONuCJcxA8OqOqMQIDAQAB";

    /**
     * RSA私钥（用于后端解密）
     * 实际项目中应该从配置文件或密钥管理服务获取，并做好安全保护
     */
    private static String PRIVATE_KEY = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC3Izz+ze7Mt0FQPGutnPgG7Ri9LCl0Z3Onf/U3PoYgZxNwVWgWvAysJsKddcRUzqh1OJu953XMQUW5vP5cATypqnBEGPObDrhOTPoQtCraEza1Bj1qVODcVx2D9ycqAcuSx3xfrAGTc5FHbUM3osVOkQSyVz0HIlvPeXHvTaWlmZ1TLilfNPli2RPSGwLoHMNH+mn0dhopP33myU8XtyNTpzFkRVMCl+eqLZo71FVaMAcuQfHfGUhhuv9MqT9lh8uERmUVuLbuOWA5VNq/pxcGOI9uEMGvAFTuZDCt+0NB5MBU1DxwMgMs6W5NlDIuoNqshhUw+c424IlzEDw6o6oxAgMBAAECggEABWl+bjZGQhkBTl52vSr4HnZHxqFlOTosbg0HQWJw4sjBB48SlCdrBXRgyHf4PRxdWJd7bMWIEujz500JRE1KDmaJyk1ld5XcBv5Z3oAAizDJq1hbnMaU3ZYaenGln+VtL3GDQK2L32BpMO/u7lg6VfvNpHqVBF600WEQp6aHCGmAosIvPBQVpdUOcbppjwRHqYV7gPwxTYnSzxDyUJHM9rQ72VjVdiTz1uSIgq9wtuigaR7FmDTy9Ha5/nE6LCiKJjHZJfuDEI5rbuX8UhooVfL8p6FuHr12jQgOcyR1Rc9TvgcCNwo1tEa0ysWWQJlCHIw3U6tT1Vhf+MmzRWkIMQKBgQDD2tK8ZmMY0yEYt0CA2oncSKCGYpgwgBWI7HLagAG2Gt6c+ccWOGCGmeu6jR96mBIttcDAQvcIETiwqNI+zj/MbC6Is/T7JpQu/V6hWb8EPPsFMBdjCvbU8Png1/UG0wszXwUFCNTC8sbHZ3i2QJh5NFFO3y/p/gPy1AXdfTQl+QKBgQDvYKeP9wa4KIZo6LXKRA2skSjpr/pcCEA3abtbk0xns+Id1vSyPCJMbkmNkdTjb5/GAfKs1nPZ4uhbG9SKs4otly9PgAuzDAR+4Y+wg40CaikBWpzo2IBsdgP5TD0aIT8wt7e47xcY1tjJiHDNoC1dCLwkz3i7hVVb8dyYSjNT+QKBgQC2hrfBFxahqxn8I8R8ka7LWfAobbhx8tye9+VuFsABi/cNiobtAWuL5eTXve+pXBINAYb6Zy48GXigwyT1nVe91tBjXHsimOifBkhrzO0FMSOryQ7yoeQ5vb7SudDEJGka3iIaeAl5CbMRYTARgg/XXVetTrkjtDTP2KMCsU2pWQKBgFZEHN7TBUbkw9uW0by83bis73OYCb+U8Z+GqRCNeuF8gCaEytZYuXuV20f7oB7+oTO4i/4hosqI7Xe4BdaecVOQwmoCGTVDRXN8Uk4oem6+V1y3jQiUzWNI/JGhGfwKsz0ZWEp7T3WSJ7opQ423BeIbPcJ+SmI54NMrzHXNd8XxAoGAdBgOntc6mOwJsPH8w2OEsFuUfBg8Y1AUWFFkZpa4e1oma3H0u+5rBFvxQoZunSrRCJiR5AwfVrjAt0fcY5JxZUp4FOmGhj6FxBk1dZ2BMw3Jv+MjZXoGGNeFce+VMVNQkUKWNC0kAkwRdS1xpMPs9vsJi0yYH0eL2YTWr4cgiUA=";

    /**
     * 生成RSA密钥对
     * @return 密钥对
     */
    public static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(RSA_ALGORITHM);
            keyPairGenerator.initialize(KEY_SIZE);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            
            // 更新静态密钥变量（主要用于测试）
            PUBLIC_KEY = getPublicKeyString(keyPair);
            PRIVATE_KEY = getPrivateKeyString(keyPair);
            
            return keyPair;
        } catch (Exception e) {
            log.error("生成RSA密钥对失败", e);
            throw new RuntimeException("生成RSA密钥对失败", e);
        }
    }

    /**
     * 获取公钥字符串
     * @param keyPair 密钥对
     * @return Base64编码的公钥字符串
     */
    public static String getPublicKeyString(KeyPair keyPair) {
        PublicKey publicKey = keyPair.getPublic();
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    /**
     * 获取私钥字符串
     * @param keyPair 密钥对
     * @return Base64编码的私钥字符串
     */
    public static String getPrivateKeyString(KeyPair keyPair) {
        PrivateKey privateKey = keyPair.getPrivate();
        return Base64.getEncoder().encodeToString(privateKey.getEncoded());
    }

    /**
     * 从字符串构造公钥
     * @param publicKeyStr Base64编码的公钥字符串
     * @return 公钥对象
     */
    public static PublicKey getPublicKeyFromString(String publicKeyStr) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(publicKeyStr);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
            return keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
            log.error("构造公钥失败", e);
            throw new RuntimeException("构造公钥失败", e);
        }
    }

    /**
     * 从字符串构造私钥
     * @param privateKeyStr Base64编码的私钥字符串
     * @return 私钥对象
     */
    public static PrivateKey getPrivateKeyFromString(String privateKeyStr) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(privateKeyStr);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
            return keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            log.error("构造私钥失败", e);
            throw new RuntimeException("构造私钥失败", e);
        }
    }

    /**
     * RSA加密
     * @param plainText 明文
     * @param publicKey 公钥
     * @return Base64编码的密文
     */
    public static String encrypt(String plainText, PublicKey publicKey) {
        try {
            Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            log.error("RSA加密失败", e);
            throw new RuntimeException("RSA加密失败", e);
        }
    }

    /**
     * RSA加密（使用公钥字符串）
     * @param plainText 明文
     * @param publicKeyStr Base64编码的公钥字符串
     * @return Base64编码的密文
     */
    public static String encrypt(String plainText, String publicKeyStr) {
        PublicKey publicKey = getPublicKeyFromString(publicKeyStr);
        return encrypt(plainText, publicKey);
    }

    /**
     * RSA解密
     * @param cipherText Base64编码的密文
     * @param privateKey 私钥
     * @return 明文
     */
    public static String decrypt(String cipherText, PrivateKey privateKey) {
        try {
            byte[] cipherBytes = Base64.getDecoder().decode(cipherText);
            Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decryptedBytes = cipher.doFinal(cipherBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("RSA解密失败", e);
            throw new RuntimeException("RSA解密失败", e);
        }
    }

    /**
     * RSA解密（使用私钥字符串）
     * @param cipherText Base64编码的密文
     * @param privateKeyStr Base64编码的私钥字符串
     * @return 明文
     */
    public static String decrypt(String cipherText, String privateKeyStr) {
        PrivateKey privateKey = getPrivateKeyFromString(privateKeyStr);
        return decrypt(cipherText, privateKey);
    }

    /**
     * RSA解密（使用默认私钥）
     * @param cipherText Base64编码的密文
     * @return 明文
     */
    public static String decrypt(String cipherText) {
        return decrypt(cipherText, PRIVATE_KEY);
    }

    /**
     * 获取公钥（供前端使用）
     * @return Base64编码的公钥字符串
     */
    public static String getPublicKey() {
        return PUBLIC_KEY;
    }

    /**
     * 获取私钥（供后端解密使用）
     * @return Base64编码的私钥字符串
     */
    public static String getPrivateKey() {
        return PRIVATE_KEY;
    }
} 
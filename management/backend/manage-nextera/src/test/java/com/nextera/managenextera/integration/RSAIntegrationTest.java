package com.nextera.managenextera.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextera.managenextera.dto.HybridEncryptionRequest;
import com.nextera.managenextera.dto.SysRoleDTO;
import com.nextera.managenextera.util.AESUtil;
import com.nextera.managenextera.util.HybridEncryptionUtil;
import com.nextera.managenextera.util.RSAUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RSA加密解密集成测试
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class RSAIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testGetPublicKey() throws Exception {
        // 测试获取公钥接口
        String url = "http://localhost:" + port + "/api/rsa/public-key";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        
        assertEquals(200, response.getStatusCode().value());
        
        // 解析响应JSON
        JsonNode jsonNode = objectMapper.readTree(response.getBody());
        assertEquals(200, jsonNode.get("code").asInt());
        assertNotNull(jsonNode.get("data"));
        assertFalse(jsonNode.get("data").asText().isEmpty());
        
        System.out.println("获取公钥成功: " + jsonNode.get("data").asText());
    }

    @Test
    public void testRSAEncryptionDecryption() throws Exception {
        // 测试RSA加密解密功能
        SysRoleDTO roleDTO = new SysRoleDTO();
        roleDTO.setId(1L);
        roleDTO.setRoleName("测试角色加密");
        roleDTO.setRoleCode("TEST_ROLE_ENCRYPTED");
        roleDTO.setDescription("RSA加密测试角色");
        roleDTO.setStatus(1);
        roleDTO.setPermissionIds(Arrays.asList(1L, 2L, 3L));

        String jsonData = objectMapper.writeValueAsString(roleDTO);
        System.out.println("原始JSON数据: " + jsonData);

        // RSA加密
        String encryptedData = RSAUtil.encrypt(jsonData, RSAUtil.getPublicKey());
        System.out.println("加密后数据: " + encryptedData);
        System.out.println("加密后数据长度: " + encryptedData.length());

        // 准备请求头
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Encrypted", "true");
        headers.setContentType(MediaType.TEXT_PLAIN);

        // 创建请求实体
        HttpEntity<String> requestEntity = new HttpEntity<>(encryptedData, headers);

        // 发送加密请求
        String url = "http://localhost:" + port + "/api/system/role";
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, requestEntity, String.class);

        System.out.println("响应状态码: " + response.getStatusCode());
        System.out.println("响应内容: " + response.getBody());

        // 验证响应（可能会因为权限问题返回403，但至少不应该是解密错误）
        assertTrue(response.getStatusCode().value() == 200 || response.getStatusCode().value() == 403,
                "状态码应该是200或403，实际是: " + response.getStatusCode());
    }

    @Test
    public void testUpdateRoleWithoutEncryption() throws Exception {
        // 准备测试数据
        SysRoleDTO roleDTO = new SysRoleDTO();
        roleDTO.setId(2L);
        roleDTO.setRoleName("测试角色明文");
        roleDTO.setRoleCode("TEST_ROLE_PLAIN");
        roleDTO.setDescription("明文测试角色");
        roleDTO.setStatus(1);
        roleDTO.setPermissionIds(Arrays.asList(4L, 5L, 6L));

        String jsonData = objectMapper.writeValueAsString(roleDTO);

        // 准备请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 创建请求实体
        HttpEntity<String> requestEntity = new HttpEntity<>(jsonData, headers);

        // 发送明文请求（不设置X-Encrypted头）
        String url = "http://localhost:" + port + "/system/role";
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, requestEntity, String.class);

        System.out.println("明文请求响应状态码: " + response.getStatusCode());
        System.out.println("明文请求响应内容: " + response.getBody());

        // 验证响应（可能会因为权限问题返回403，但至少不应该是解析错误）
        assertTrue(response.getStatusCode().value() == 200 || response.getStatusCode().value() == 403,
                "状态码应该是200或403，实际是: " + response.getStatusCode());
    }

    @Test
    public void testUpdateRoleWithInvalidEncryption() throws Exception {
        // 发送无效的加密数据
        String invalidEncryptedData = "invalid_encrypted_data_string";

        // 准备请求头
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Encrypted", "true");
        headers.setContentType(MediaType.TEXT_PLAIN);

        // 创建请求实体
        HttpEntity<String> requestEntity = new HttpEntity<>(invalidEncryptedData, headers);

        // 发送无效加密数据的请求
        String url = "http://localhost:" + port + "/system/role";
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, requestEntity, String.class);

        System.out.println("无效加密数据响应状态码: " + response.getStatusCode());
        System.out.println("无效加密数据响应内容: " + response.getBody());

        // 验证返回400错误
        assertEquals(400, response.getStatusCode().value());
        
        // 解析响应JSON
        JsonNode jsonNode = objectMapper.readTree(response.getBody());
        assertEquals(400, jsonNode.get("code").asInt());
        assertEquals("数据解密失败", jsonNode.get("message").asText());
    }

    @Test
    public void testRSABasicEncryptDecrypt() {
        // 测试基本的RSA加密解密
        String plainText = "Hello RSA Test!";
        
        // 加密
        String encrypted = RSAUtil.encrypt(plainText, RSAUtil.getPublicKey());
        assertNotNull(encrypted);
        assertNotEquals(plainText, encrypted);
        
        // 解密
        String decrypted = RSAUtil.decrypt(encrypted);
        assertEquals(plainText, decrypted);
        
        System.out.println("RSA基本加密解密测试通过");
    }

    @Test
    public void testAESKeyEncryptDecrypt() {
        // 测试AES密钥的RSA加密解密
        String aesKey = AESUtil.generateKey();
        
        // 使用RSA加密AES密钥
        String encryptedKey = RSAUtil.encrypt(aesKey, RSAUtil.getPublicKey());
        assertNotNull(encryptedKey);
        
        // 使用RSA解密AES密钥
        String decryptedKey = RSAUtil.decrypt(encryptedKey);
        assertEquals(aesKey, decryptedKey);
        
        System.out.println("AES密钥RSA加密解密测试通过");
    }

    @Test
    public void testHybridEncryptionFlow() throws Exception {
        // 创建测试数据
        SysRoleDTO roleDTO = new SysRoleDTO();
        roleDTO.setId(1L);
        roleDTO.setRoleCode("TEST_ROLE");
        roleDTO.setRoleName("测试角色");
        roleDTO.setDescription("这是一个测试角色");
        roleDTO.setStatus(1);
        
        String jsonData = objectMapper.writeValueAsString(roleDTO);
        
        // 执行混合加密
        String encryptedJson = HybridEncryptionUtil.hybridEncrypt(jsonData, RSAUtil.getPublicKey());
        assertNotNull(encryptedJson);
        
        // 执行混合解密
        String decryptedJson = HybridEncryptionUtil.hybridDecrypt(encryptedJson, RSAUtil.getPrivateKey());
        assertEquals(jsonData, decryptedJson);
        
        // 验证解密后的数据可以正确反序列化
        SysRoleDTO decryptedRole = objectMapper.readValue(decryptedJson, SysRoleDTO.class);
        assertEquals(roleDTO.getId(), decryptedRole.getId());
        assertEquals(roleDTO.getRoleCode(), decryptedRole.getRoleCode());
        assertEquals(roleDTO.getRoleName(), decryptedRole.getRoleName());
        assertEquals(roleDTO.getDescription(), decryptedRole.getDescription());
        assertEquals(roleDTO.getStatus(), decryptedRole.getStatus());
        
        System.out.println("混合加密解密流程测试通过");
    }

    @Test
    public void testPublicKeyFormat() {
        // 测试公钥格式
        String publicKey = RSAUtil.getPublicKey();
        assertNotNull(publicKey);
        assertTrue(publicKey.length() > 0);
        
        // 验证是Base64格式
        try {
            java.util.Base64.getDecoder().decode(publicKey);
        } catch (IllegalArgumentException e) {
            fail("公钥不是有效的Base64格式");
        }
        
        System.out.println("公钥格式验证通过");
        System.out.println("公钥长度: " + publicKey.length());
    }

    @Test
    public void testLargeDataEncryption() throws Exception {
        // 测试大数据加密
        StringBuilder largeData = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            largeData.append("这是测试数据第").append(i).append("行，用于测试大数据的混合加密功能。");
        }
        
        String originalData = largeData.toString();
        
        // 执行混合加密
        String encryptedJson = HybridEncryptionUtil.hybridEncrypt(originalData, RSAUtil.getPublicKey());
        assertNotNull(encryptedJson);
        
        // 执行混合解密
        String decryptedData = HybridEncryptionUtil.hybridDecrypt(encryptedJson, RSAUtil.getPrivateKey());
        assertEquals(originalData, decryptedData);
        
        System.out.println("大数据混合加密解密测试通过");
        System.out.println("原始数据长度: " + originalData.length());
        System.out.println("加密后JSON长度: " + encryptedJson.length());
    }

    @Test
    public void testInvalidDecryption() {
        // 测试无效解密
        String invalidCipherText = "invalid_cipher_text";
        
        assertThrows(RuntimeException.class, () -> {
            RSAUtil.decrypt(invalidCipherText);
        });
        
        System.out.println("无效解密异常处理测试通过");
    }
} 
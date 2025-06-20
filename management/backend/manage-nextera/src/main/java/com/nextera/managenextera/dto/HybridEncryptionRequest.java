package com.nextera.managenextera.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 混合加密请求DTO
 * 包含RSA加密的AES密钥和AES加密的业务数据
 */
public class HybridEncryptionRequest {
    
    /**
     * RSA加密的AES密钥（Base64编码）
     */
    @JsonProperty("encryptedKey")
    private String encryptedKey;
    
    /**
     * AES加密的业务数据（Base64编码）
     */
    @JsonProperty("encryptedData")
    private String encryptedData;
    
    public HybridEncryptionRequest() {}
    
    public HybridEncryptionRequest(String encryptedKey, String encryptedData) {
        this.encryptedKey = encryptedKey;
        this.encryptedData = encryptedData;
    }
    
    public String getEncryptedKey() {
        return encryptedKey;
    }
    
    public void setEncryptedKey(String encryptedKey) {
        this.encryptedKey = encryptedKey;
    }
    
    public String getEncryptedData() {
        return encryptedData;
    }
    
    public void setEncryptedData(String encryptedData) {
        this.encryptedData = encryptedData;
    }
    
    @Override
    public String toString() {
        return "HybridEncryptionRequest{" +
                "encryptedKey='" + (encryptedKey != null ? "[ENCRYPTED]" : "null") + '\'' +
                ", encryptedData='" + (encryptedData != null ? "[ENCRYPTED]" : "null") + '\'' +
                '}';
    }
} 
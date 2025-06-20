<template>
  <div class="rsa-test">
    <h2>RSA加密测试</h2>
    
    <el-card class="test-card">
      <h3>获取公钥测试</h3>
      <el-button @click="testGetPublicKey" type="primary">获取RSA公钥</el-button>
      <div v-if="publicKey" class="result">
        <h4>公钥：</h4>
        <pre>{{ publicKey }}</pre>
      </div>
    </el-card>

    <el-card class="test-card">
      <h3>RSA加密测试</h3>
      <el-input v-model="testData" placeholder="输入要加密的测试数据" />
      <el-button @click="testRSAEncrypt" type="primary">RSA加密测试</el-button>
      <div v-if="rsaResult" class="result">
        <h4>加密结果：</h4>
        <pre>{{ rsaResult }}</pre>
      </div>
    </el-card>

    <el-card class="test-card">
      <h3>混合加密测试</h3>
      <el-button @click="testHybridEncrypt" type="primary">混合加密测试</el-button>
      <div v-if="hybridResult" class="result">
        <h4>混合加密结果：</h4>
        <pre>{{ JSON.stringify(hybridResult, null, 2) }}</pre>
      </div>
    </el-card>

    <div v-if="error" class="error">
      <h4>错误信息：</h4>
      <pre>{{ error }}</pre>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import request from '@/api/request'
import hybridEncryption, { type HybridEncryptionData } from '@/utils/hybridEncryption'
import JSEncrypt from 'jsencrypt'

const publicKey = ref('')
const testData = ref('Hello RSA Test!')
const rsaResult = ref('')
const hybridResult = ref<HybridEncryptionData | null>(null)
const error = ref('')

const testGetPublicKey = async () => {
  try {
    error.value = ''
    const response = await request.get('/rsa/public-key')
    console.log('公钥API响应:', response)
    
    const rawKey = response.data.publicKey
    publicKey.value = formatPublicKeyToPEM(rawKey)
    
    console.log('格式化后的公钥:', publicKey.value)
  } catch (err) {
    error.value = `获取公钥失败: ${err}`
    console.error('获取公钥失败:', err)
  }
}

const formatPublicKeyToPEM = (base64Key: string): string => {
  const pemHeader = '-----BEGIN PUBLIC KEY-----'
  const pemFooter = '-----END PUBLIC KEY-----'
  
  // 每64个字符换行
  const formattedKey = base64Key.match(/.{1,64}/g)?.join('\n') || base64Key
  
  return `${pemHeader}\n${formattedKey}\n${pemFooter}`
}

const testRSAEncrypt = async () => {
  try {
    error.value = ''
    
    if (!publicKey.value) {
      await testGetPublicKey()
    }
    
    const jsencrypt = new JSEncrypt()
    jsencrypt.setPublicKey(publicKey.value)
    
    const encrypted = jsencrypt.encrypt(testData.value)
    if (!encrypted) {
      throw new Error('RSA加密失败')
    }
    
    rsaResult.value = encrypted
    console.log('RSA加密成功:', encrypted)
  } catch (err) {
    error.value = `RSA加密失败: ${err}`
    console.error('RSA加密失败:', err)
  }
}

const testHybridEncrypt = async () => {
  try {
    error.value = ''
    
    const testObj = {
      id: 1,
      name: '测试角色',
      description: '这是一个测试角色'
    }
    
    const result = await hybridEncryption.hybridEncrypt(testObj)
    hybridResult.value = result
    console.log('混合加密成功:', result)
  } catch (err) {
    error.value = `混合加密失败: ${err}`
    console.error('混合加密失败:', err)
  }
}
</script>

<style scoped>
.rsa-test {
  padding: 20px;
}

.test-card {
  margin-bottom: 20px;
}

.result {
  margin-top: 10px;
  padding: 10px;
  background-color: #f5f5f5;
  border-radius: 4px;
}

.error {
  margin-top: 20px;
  padding: 10px;
  background-color: #fef0f0;
  border: 1px solid #fde2e2;
  border-radius: 4px;
  color: #f56565;
}

pre {
  white-space: pre-wrap;
  word-break: break-all;
}

.el-button {
  margin: 10px 0;
}
</style> 
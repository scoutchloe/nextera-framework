<template>
  <div class="anti-replay-test">
    <div class="test-header">
      <h1>防重放攻击测试</h1>
      <p>测试不同的防重放配置和攻击场景</p>
    </div>

    <div class="test-section">
      <h2>1. 基础防重放测试</h2>
      <p>验证时间戳和nonce</p>
      <el-button type="primary" @click="testBasicAntiReplay" :loading="loading.basic">
        测试基础防重放
      </el-button>
      <div v-if="results.basic" class="result-box">
        <pre>{{ results.basic }}</pre>
      </div>
    </div>

    <div class="test-section">
      <h2>2. 序列号防重放测试</h2>
      <p>验证时间戳、nonce和序列号</p>
      <el-button type="primary" @click="testSequenceAntiReplay" :loading="loading.sequence">
        测试序列号防重放
      </el-button>
      <div v-if="results.sequence" class="result-box">
        <pre>{{ results.sequence }}</pre>
      </div>
    </div>

    <div class="test-section">
      <h2>3. 签名防重放测试</h2>
      <p>验证时间戳、nonce和签名</p>
      <el-button type="primary" @click="testSignatureAntiReplay" :loading="loading.signature">
        测试签名防重放
      </el-button>
      <div v-if="results.signature" class="result-box">
        <pre>{{ results.signature }}</pre>
      </div>
    </div>

    <div class="test-section">
      <h2>4. 完整防重放测试</h2>
      <p>验证所有参数：时间戳、nonce、序列号和签名</p>
      <el-button type="primary" @click="testFullAntiReplay" :loading="loading.full">
        测试完整防重放
      </el-button>
      <div v-if="results.full" class="result-box">
        <pre>{{ results.full }}</pre>
      </div>
    </div>

    <div class="test-section">
      <h2>5. 重放攻击模拟</h2>
      <p>发送相同的请求，模拟重放攻击</p>
      <el-button type="warning" @click="testReplayAttack" :loading="loading.replay">
        模拟重放攻击
      </el-button>
      <div v-if="results.replay" class="result-box">
        <pre>{{ results.replay }}</pre>
      </div>
    </div>

    <div class="test-section">
      <h2>6. 时间戳过期测试</h2>
      <p>发送过期的时间戳</p>
      <el-button type="warning" @click="testExpiredTimestamp" :loading="loading.expired">
        测试过期时间戳
      </el-button>
      <div v-if="results.expired" class="result-box">
        <pre>{{ results.expired }}</pre>
      </div>
    </div>

    <div class="test-section">
      <h2>7. 无保护接口测试</h2>
      <p>测试没有防重放保护的接口</p>
      <el-button type="success" @click="testNoProtection" :loading="loading.noProtection">
        测试无保护接口
      </el-button>
      <div v-if="results.noProtection" class="result-box">
        <pre>{{ results.noProtection }}</pre>
      </div>
    </div>

    <div class="test-section">
      <h2>用户序列号状态</h2>
      <p>当前用户序列号: {{ currentSequence }}</p>
      <el-button type="info" @click="resetSequence">重置序列号</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import { 
  generateAntiReplayHeaders, 
  getCurrentUserSequence, 
  resetUserSequence,
  type AntiReplayOptions 
} from '@/utils/antiReplay'

// 测试用户ID
const TEST_USER_ID = 'test-user-123'
const SECRET_KEY = 'nextera-anti-replay-secret-key-2024'

// 响应式数据
const loading = ref({
  basic: false,
  sequence: false,
  signature: false,
  full: false,
  replay: false,
  expired: false,
  noProtection: false
})

const results = ref({
  basic: '',
  sequence: '',
  signature: '',
  full: '',
  replay: '',
  expired: '',
  noProtection: ''
})

const currentSequence = ref(0)

// 最后一次请求的配置（用于重放攻击测试）
let lastRequestConfig: any = null

// 更新当前序列号显示
const updateSequenceDisplay = () => {
  currentSequence.value = getCurrentUserSequence(TEST_USER_ID)
}

// 基础防重放测试
const testBasicAntiReplay = async () => {
  loading.value.basic = true
  try {
    const testData = { message: '基础防重放测试', timestamp: Date.now() }
    const headers = await generateAntiReplayHeaders('POST', '/api/anti-replay/basic', testData)
    
    const response = await axios.post('/api/anti-replay/basic', testData, { 
      headers: headers as any 
    })
    results.value.basic = JSON.stringify(response.data, null, 2)
    ElMessage.success('基础防重放测试成功')
  } catch (error: any) {
    results.value.basic = JSON.stringify(error.response?.data || error.message, null, 2)
    ElMessage.error('基础防重放测试失败')
  }
  loading.value.basic = false
}

// 序列号防重放测试
const testSequenceAntiReplay = async () => {
  loading.value.sequence = true
  try {
    const testData = { message: '序列号防重放测试', timestamp: Date.now() }
    const options: AntiReplayOptions = {
      enableSequence: true,
      userId: TEST_USER_ID
    }
    const headers = await generateAntiReplayHeaders('POST', '/api/anti-replay/sequence', testData, options)
    
    const response = await axios.post('/api/anti-replay/sequence', testData, { 
      headers: headers as any 
    })
    results.value.sequence = JSON.stringify(response.data, null, 2)
    updateSequenceDisplay()
    ElMessage.success('序列号防重放测试成功')
  } catch (error: any) {
    results.value.sequence = JSON.stringify(error.response?.data || error.message, null, 2)
    ElMessage.error('序列号防重放测试失败')
  }
  loading.value.sequence = false
}

// 签名防重放测试
const testSignatureAntiReplay = async () => {
  loading.value.signature = true
  try {
    const testData = { message: '签名防重放测试', timestamp: Date.now() }
    const options: AntiReplayOptions = {
      enableSignature: true,
      secretKey: SECRET_KEY
    }
    const headers = await generateAntiReplayHeaders('POST', '/api/anti-replay/signature', testData, options)
    
    const response = await axios.post('/api/anti-replay/signature', testData, { 
      headers: headers as any 
    })
    results.value.signature = JSON.stringify(response.data, null, 2)
    ElMessage.success('签名防重放测试成功')
  } catch (error: any) {
    results.value.signature = JSON.stringify(error.response?.data || error.message, null, 2)
    ElMessage.error('签名防重放测试失败')
  }
  loading.value.signature = false
}

// 完整防重放测试
const testFullAntiReplay = async () => {
  loading.value.full = true
  try {
    const testData = { message: '完整防重放测试', timestamp: Date.now() }
    const options: AntiReplayOptions = {
      enableSequence: true,
      enableSignature: true,
      userId: TEST_USER_ID,
      secretKey: SECRET_KEY
    }
    const headers = await generateAntiReplayHeaders('POST', '/api/anti-replay/full', testData, options)
    
    // 保存请求配置用于重放攻击测试
    lastRequestConfig = {
      url: '/api/anti-replay/full',
      method: 'POST',
      data: testData,
      headers: headers as any
    }
    
    const response = await axios.post('/api/anti-replay/full', testData, { 
      headers: headers as any 
    })
    results.value.full = JSON.stringify(response.data, null, 2)
    updateSequenceDisplay()
    ElMessage.success('完整防重放测试成功')
  } catch (error: any) {
    results.value.full = JSON.stringify(error.response?.data || error.message, null, 2)
    ElMessage.error('完整防重放测试失败')
  }
  loading.value.full = false
}

// 重放攻击测试
const testReplayAttack = async () => {
  if (!lastRequestConfig) {
    ElMessage.warning('请先执行完整防重放测试')
    return
  }
  
  loading.value.replay = true
  try {
    // 使用相同的请求配置再次发送请求
    const response = await axios(lastRequestConfig)
    results.value.replay = JSON.stringify(response.data, null, 2)
    ElMessage.warning('重放攻击成功（这不应该发生）')
  } catch (error: any) {
    results.value.replay = JSON.stringify(error.response?.data || error.message, null, 2)
    ElMessage.success('重放攻击被成功阻止')
  }
  loading.value.replay = false
}

// 过期时间戳测试
const testExpiredTimestamp = async () => {
  loading.value.expired = true
  try {
    const testData = { message: '过期时间戳测试', timestamp: Date.now() }
    
    // 手动构造过期的时间戳（10分钟前）
    const expiredTimestamp = Date.now() - 10 * 60 * 1000
    const headers = {
      'X-Timestamp': expiredTimestamp.toString(),
      'X-Nonce': Math.random().toString(36).substring(2, 15)
    }
    
    const response = await axios.post('/api/anti-replay/basic', testData, { 
      headers: headers as any 
    })
    results.value.expired = JSON.stringify(response.data, null, 2)
    ElMessage.warning('过期时间戳测试通过（这不应该发生）')
  } catch (error: any) {
    results.value.expired = JSON.stringify(error.response?.data || error.message, null, 2)
    ElMessage.success('过期时间戳被成功拒绝')
  }
  loading.value.expired = false
}

// 无保护接口测试
const testNoProtection = async () => {
  loading.value.noProtection = true
  try {
    const testData = { message: '无保护接口测试', timestamp: Date.now() }
    
    const response = await axios.post('/api/anti-replay/no-protection', testData)
    results.value.noProtection = JSON.stringify(response.data, null, 2)
    ElMessage.success('无保护接口测试成功')
  } catch (error: any) {
    results.value.noProtection = JSON.stringify(error.response?.data || error.message, null, 2)
    ElMessage.error('无保护接口测试失败')
  }
  loading.value.noProtection = false
}

// 重置序列号
const resetSequence = () => {
  resetUserSequence(TEST_USER_ID)
  updateSequenceDisplay()
  ElMessage.success('序列号已重置')
}

// 组件挂载时更新序列号显示
onMounted(() => {
  updateSequenceDisplay()
})
</script>

<style scoped>
.anti-replay-test {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.test-header {
  text-align: center;
  margin-bottom: 30px;
}

.test-header h1 {
  color: #409eff;
  margin-bottom: 10px;
}

.test-section {
  margin-bottom: 30px;
  padding: 20px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  background-color: #fafafa;
}

.test-section h2 {
  color: #303133;
  margin-bottom: 10px;
}

.test-section p {
  color: #606266;
  margin-bottom: 15px;
}

.result-box {
  margin-top: 15px;
  padding: 15px;
  background-color: #f5f7fa;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  max-height: 300px;
  overflow-y: auto;
}

.result-box pre {
  margin: 0;
  white-space: pre-wrap;
  word-wrap: break-word;
  font-family: 'Courier New', monospace;
  font-size: 12px;
  color: #606266;
}
</style> 
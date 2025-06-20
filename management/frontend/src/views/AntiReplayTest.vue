<template>
  <div class="anti-replay-test">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>防重放攻击测试</span>
        </div>
      </template>
      
      <div class="test-section">
        <h3>测试说明</h3>
        <p>这个页面用于测试防重放攻击功能，包括：</p>
        <ul>
          <li>基础防重放：使用时间戳 + nonce</li>
          <li>序列号防重放：使用用户序列号</li>
          <li>重放攻击模拟：验证重复请求被拒绝</li>
          <li>时间戳过期：验证过期请求被拒绝</li>
          <li>无保护接口：对比普通接口</li>
        </ul>
      </div>

      <div class="test-buttons">
        <el-button type="primary" @click="testBasicAntiReplay" :loading="loading">
          基础防重放测试
        </el-button>
        <el-button type="success" @click="testSequenceAntiReplay" :loading="loading">
          序列号防重放测试
        </el-button>
        <el-button type="warning" @click="testReplayAttack" :loading="loading">
          重放攻击模拟
        </el-button>
        <el-button type="danger" @click="testExpiredTimestamp" :loading="loading">
          时间戳过期测试
        </el-button>
        <el-button type="info" @click="testNoProtection" :loading="loading">
          无保护接口测试
        </el-button>
        <el-button @click="clearResults">清空结果</el-button>
      </div>

      <div class="test-results">
        <h3>测试结果</h3>
        <div v-for="(result, key) in results" :key="key" class="result-item">
          <h4>{{ getTestName(key) }}</h4>
          <el-alert
            v-if="result"
            :title="result.message"
            :type="result.success ? 'success' : 'error'"
            :description="result.data ? JSON.stringify(result.data, null, 2) : ''"
            show-icon
            :closable="false"
          />
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { generateNonce } from '../utils/antiReplay'
import request from '../api/request'

// 响应式数据
const loading = ref(false)

interface TestResult {
  success: boolean
  message: string
  data?: any
}

const results = ref<Record<string, TestResult | null>>({
  basic: null,
  sequence: null,
  replay: null,
  expired: null,
  noProtection: null
})

// 获取测试名称
const getTestName = (key: string): string => {
  const names: Record<string, string> = {
    basic: '基础防重放测试',
    sequence: '序列号防重放测试',
    replay: '重放攻击模拟',
    expired: '时间戳过期测试',
    noProtection: '无保护接口测试'
  }
  return names[key] || key
}

// 基础防重放测试
const testBasicAntiReplay = async () => {
  try {
    loading.value = true
    const testData = { message: '基础防重放测试' }
    
    // 手动生成防重放头部
    const timestamp = Date.now().toString()
    const nonce = generateNonce()
    
    const headers = {
      'X-Timestamp': timestamp,
      'X-Nonce': nonce
    }
    
    console.log('请求头:', headers)
    
    const response = await request.post('/api/anti-replay/basic', testData, {
      headers
    })
    
    results.value.basic = { success: true, message: '基础防重放测试成功', data: response.data }
    ElMessage.success('基础防重放测试成功')
  } catch (error: any) {
    console.error('基础防重放测试失败:', error)
    results.value.basic = { success: false, message: error.message || '基础防重放测试失败' }
    ElMessage.error('基础防重放测试失败')
  } finally {
    loading.value = false
  }
}

// 序列号防重放测试
const testSequenceAntiReplay = async () => {
  try {
    loading.value = true
    const testData = { message: '序列号防重放测试' }
    
    // 手动生成带序列号的防重放头部
    const timestamp = Date.now().toString()
    const nonce = generateNonce()
    const sequence = Math.floor(Math.random() * 1000) + 1
    
    const headers = {
      'X-Timestamp': timestamp,
      'X-Nonce': nonce,
      'X-Sequence': sequence.toString()
    }
    
    console.log('请求头:', headers)
    
    const response = await request.post('/api/anti-replay/sequence', testData, {
      headers
    })
    
    results.value.sequence = { success: true, message: '序列号防重放测试成功', data: response.data }
    ElMessage.success('序列号防重放测试成功')
  } catch (error: any) {
    console.error('序列号防重放测试失败:', error)
    results.value.sequence = { success: false, message: error.message || '序列号防重放测试失败' }
    ElMessage.error('序列号防重放测试失败')
  } finally {
    loading.value = false
  }
}

// 重放攻击模拟
const testReplayAttack = async () => {
  try {
    loading.value = true
    const testData = { message: '重放攻击测试' }
    
    // 生成防重放头部
    const timestamp = Date.now().toString()
    const nonce = generateNonce()
    
    const headers = {
      'X-Timestamp': timestamp,
      'X-Nonce': nonce
    }
    
    console.log('第一次请求头:', headers)
    
    // 第一次请求
    const response1 = await request.post('/api/anti-replay/basic', testData, {
      headers
    })
    
    console.log('第一次请求成功:', response1.data)
    
    // 使用相同的头部再次请求（模拟重放攻击）
    try {
      const response2 = await request.post('/api/anti-replay/basic', testData, {
        headers
      })
      results.value.replay = { success: false, message: '重放攻击未被阻止，存在安全漏洞！' }
      ElMessage.error('重放攻击未被阻止')
    } catch (replayError: any) {
      results.value.replay = { success: true, message: '重放攻击被成功阻止', data: replayError.message }
      ElMessage.success('重放攻击被成功阻止')
    }
  } catch (error: any) {
    console.error('重放攻击测试失败:', error)
    results.value.replay = { success: false, message: error.message || '重放攻击测试失败' }
    ElMessage.error('重放攻击测试失败')
  } finally {
    loading.value = false
  }
}

// 时间戳过期测试
const testExpiredTimestamp = async () => {
  try {
    loading.value = true
    const testData = { message: '过期时间戳测试' }
    
    // 生成一个过期的时间戳（5分钟前）
    const expiredTimestamp = (Date.now() - 5 * 60 * 1000).toString()
    const nonce = generateNonce()
    
    const headers = {
      'X-Timestamp': expiredTimestamp,
      'X-Nonce': nonce
    }
    
    console.log('过期请求头:', headers)
    
    const response = await request.post('/api/anti-replay/basic', testData, {
      headers
    })
    
    results.value.expired = { success: false, message: '过期时间戳未被拒绝，存在安全漏洞！' }
    ElMessage.error('过期时间戳未被拒绝')
  } catch (error: any) {
    console.error('过期时间戳测试:', error)
    results.value.expired = { success: true, message: '过期时间戳被成功拒绝', data: error.message }
    ElMessage.success('过期时间戳被成功拒绝')
  } finally {
    loading.value = false
  }
}

// 无保护接口测试
const testNoProtection = async () => {
  try {
    loading.value = true
    const testData = { message: '无保护接口测试' }
    
    const response = await request.post('/api/anti-replay/no-protection', testData)
    
    results.value.noProtection = { success: true, message: '无保护接口测试成功', data: response.data }
    ElMessage.success('无保护接口测试成功')
  } catch (error: any) {
    console.error('无保护接口测试失败:', error)
    results.value.noProtection = { success: false, message: error.message || '无保护接口测试失败' }
    ElMessage.error('无保护接口测试失败')
  } finally {
    loading.value = false
  }
}

// 清空结果
const clearResults = () => {
  results.value = {
    basic: null,
    sequence: null,
    replay: null,
    expired: null,
    noProtection: null
  }
  ElMessage.info('测试结果已清空')
}
</script>

<style scoped>
.anti-replay-test {
  padding: 20px;
}

.test-section {
  margin-bottom: 20px;
}

.test-section h3 {
  color: #409eff;
  margin-bottom: 10px;
}

.test-section ul {
  margin-left: 20px;
}

.test-section li {
  margin-bottom: 5px;
}

.test-buttons {
  margin-bottom: 20px;
}

.test-buttons .el-button {
  margin-right: 10px;
  margin-bottom: 10px;
}

.test-results h3 {
  color: #409eff;
  margin-bottom: 15px;
}

.result-item {
  margin-bottom: 15px;
}

.result-item h4 {
  margin-bottom: 8px;
  color: #606266;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style> 
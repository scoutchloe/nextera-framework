<template>
  <div class="hybrid-encryption-test">
    <div class="page-header">
      <h2>混合加密测试</h2>
      <p>测试角色更新接口的RSA+AES混合加密功能</p>
    </div>

    <div class="test-container">
      <!-- 角色更新表单 -->
      <el-card class="test-card">
        <template #header>
          <div class="card-header">
            <span>角色更新测试</span>
          </div>
        </template>

        <el-form :model="roleForm" :rules="rules" ref="roleFormRef" label-width="120px">
          <el-form-item label="角色ID" prop="id">
            <el-input-number v-model="roleForm.id" :min="1" placeholder="请输入角色ID" />
          </el-form-item>
          
          <el-form-item label="角色名称" prop="roleName">
            <el-input v-model="roleForm.roleName" placeholder="请输入角色名称" />
          </el-form-item>
          
          <el-form-item label="角色编码" prop="roleCode">
            <el-input v-model="roleForm.roleCode" placeholder="请输入角色编码" />
          </el-form-item>
          
          <el-form-item label="角色描述" prop="roleDesc">
            <el-input v-model="roleForm.roleDesc" type="textarea" placeholder="请输入角色描述" />
          </el-form-item>
          
          <el-form-item label="状态" prop="status">
            <el-radio-group v-model="roleForm.status">
              <el-radio :label="1">启用</el-radio>
              <el-radio :label="0">禁用</el-radio>
            </el-radio-group>
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" @click="testRoleUpdate" :loading="loading">
              测试角色更新（混合加密）
            </el-button>
            <el-button @click="resetForm">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 加密过程展示 -->
      <el-card class="test-card">
        <template #header>
          <div class="card-header">
            <span>加密过程展示</span>
          </div>
        </template>

        <div class="encryption-steps">
          <el-steps :active="currentStep" direction="vertical">
            <el-step title="生成AES密钥" :description="steps.aesKey" />
            <el-step title="AES加密业务数据" :description="steps.aesEncrypt" />
            <el-step title="RSA加密AES密钥" :description="steps.rsaEncrypt" />
            <el-step title="发送混合加密数据" :description="steps.sendData" />
            <el-step title="服务器解密响应" :description="steps.response" />
          </el-steps>
        </div>
      </el-card>

      <!-- 详细信息展示 -->
      <el-card class="test-card" v-if="encryptionDetails.originalData">
        <template #header>
          <div class="card-header">
            <span>加密详细信息</span>
          </div>
        </template>

        <div class="details-content">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="原始数据">
              <el-input v-model="encryptionDetails.originalData" type="textarea" readonly />
            </el-descriptions-item>
            
            <el-descriptions-item label="AES密钥">
              <el-input v-model="encryptionDetails.aesKey" readonly />
            </el-descriptions-item>
            
            <el-descriptions-item label="AES加密后数据">
              <el-input v-model="encryptionDetails.encryptedData" type="textarea" readonly />
            </el-descriptions-item>
            
            <el-descriptions-item label="RSA加密后密钥">
              <el-input v-model="encryptionDetails.encryptedKey" type="textarea" readonly />
            </el-descriptions-item>
            
            <el-descriptions-item label="最终发送数据">
              <el-input v-model="encryptionDetails.finalData" type="textarea" readonly />
            </el-descriptions-item>
          </el-descriptions>
        </div>
      </el-card>

      <!-- 响应结果 -->
      <el-card class="test-card" v-if="responseData">
        <template #header>
          <div class="card-header">
            <span>响应结果</span>
          </div>
        </template>

        <div class="response-content">
          <el-alert
            :title="responseData.success ? '请求成功' : '请求失败'"
            :type="responseData.success ? 'success' : 'error'"
            :description="responseData.message"
            show-icon
          />
          
          <div class="response-details" v-if="responseData.details">
            <h4>响应详情：</h4>
            <pre>{{ JSON.stringify(responseData.details, null, 2) }}</pre>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { roleApi } from '@/api/system'
import hybridEncryption from '@/utils/hybridEncryption'

// 响应式数据
const loading = ref(false)
const currentStep = ref(0)
const roleFormRef = ref()

// 角色表单数据
const roleForm = reactive({
  id: 1,
  roleName: '测试角色',
  roleCode: 'test_role',
  roleDesc: '这是一个测试角色，用于验证混合加密功能',
  status: 1
})

// 表单验证规则
const rules = {
  id: [
    { required: true, message: '请输入角色ID', trigger: 'blur' }
  ],
  roleName: [
    { required: true, message: '请输入角色名称', trigger: 'blur' }
  ],
  roleCode: [
    { required: true, message: '请输入角色编码', trigger: 'blur' }
  ]
}

// 加密步骤信息
const steps = reactive({
  aesKey: '等待开始...',
  aesEncrypt: '等待开始...',
  rsaEncrypt: '等待开始...',
  sendData: '等待开始...',
  response: '等待开始...'
})

// 加密详细信息
const encryptionDetails = reactive({
  originalData: '',
  aesKey: '',
  encryptedData: '',
  encryptedKey: '',
  finalData: ''
})

// 响应数据
const responseData = ref<any>(null)

// 测试角色更新
const testRoleUpdate = async () => {
  try {
    // 表单验证
    await roleFormRef.value.validate()
    
    loading.value = true
    currentStep.value = 0
    responseData.value = null
    
    // 重置步骤信息
    Object.keys(steps).forEach(key => {
      steps[key] = '等待开始...'
    })
    
    // 清空详细信息
    Object.keys(encryptionDetails).forEach(key => {
      encryptionDetails[key] = ''
    })

    ElMessage.info('开始混合加密测试...')

    // 第1步：准备原始数据
    const originalData = JSON.stringify(roleForm)
    encryptionDetails.originalData = originalData
    steps.aesKey = '正在生成AES密钥...'
    currentStep.value = 1
    await sleep(500)

    // 第2步：执行混合加密
    steps.aesKey = '✓ AES密钥生成成功'
    steps.aesEncrypt = '正在使用AES加密业务数据...'
    currentStep.value = 2
    await sleep(500)

    const encryptedResult = await hybridEncryption.hybridEncrypt(roleForm)
    
    // 更新详细信息
    encryptionDetails.encryptedData = encryptedResult.encryptedData
    encryptionDetails.encryptedKey = encryptedResult.encryptedKey
    encryptionDetails.finalData = JSON.stringify(encryptedResult, null, 2)

    steps.aesEncrypt = '✓ AES加密业务数据成功'
    steps.rsaEncrypt = '✓ RSA加密AES密钥成功'
    currentStep.value = 3
    await sleep(500)

    // 第3步：发送请求
    steps.sendData = '正在发送混合加密数据到服务器...'
    currentStep.value = 4
    await sleep(500)

    const response = await roleApi.updateRole(roleForm.id, roleForm)
    
    steps.sendData = '✓ 数据发送成功'
    steps.response = '✓ 服务器解密并处理成功'
    currentStep.value = 5

    // 设置响应数据
    responseData.value = {
      success: response.code === 200,
      message: response.message || '角色更新成功',
      details: response
    }

    ElMessage.success('混合加密测试完成！')

  } catch (error) {
    console.error('混合加密测试失败:', error)
    
    steps.response = '✗ 测试失败: ' + (error.message || '未知错误')
    
    responseData.value = {
      success: false,
      message: '混合加密测试失败',
      details: {
        error: error.message || '未知错误',
        stack: error.stack
      }
    }
    
    ElMessage.error('混合加密测试失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 重置表单
const resetForm = () => {
  roleFormRef.value?.resetFields()
  currentStep.value = 0
  responseData.value = null
  
  // 重置步骤信息
  Object.keys(steps).forEach(key => {
    steps[key] = '等待开始...'
  })
  
  // 清空详细信息
  Object.keys(encryptionDetails).forEach(key => {
    encryptionDetails[key] = ''
  })
}

// 辅助函数：延迟
const sleep = (ms: number) => new Promise(resolve => setTimeout(resolve, ms))
</script>

<style lang="scss" scoped>
.hybrid-encryption-test {
  .page-header {
    margin-bottom: 24px;
    padding-bottom: 16px;
    border-bottom: 1px solid var(--border-color);
    
    h2 {
      margin: 0 0 8px 0;
      color: var(--text-primary);
      font-size: 24px;
      font-weight: 600;
    }
    
    p {
      margin: 0;
      color: var(--text-secondary);
      font-size: 14px;
    }
  }

  .test-container {
    display: flex;
    flex-direction: column;
    gap: 20px;
  }

  .test-card {
    .card-header {
      font-weight: 600;
      color: var(--text-primary);
    }
  }

  .encryption-steps {
    .el-steps {
      max-width: 100%;
    }
  }

  .details-content {
    .el-textarea {
      :deep(.el-textarea__inner) {
        font-family: 'Courier New', monospace;
        font-size: 12px;
      }
    }
    
    .el-input {
      :deep(.el-input__inner) {
        font-family: 'Courier New', monospace;
        font-size: 12px;
      }
    }
  }

  .response-content {
    .response-details {
      margin-top: 16px;
      
      h4 {
        margin: 0 0 8px 0;
        color: var(--text-primary);
      }
      
      pre {
        background: var(--bg-secondary);
        padding: 12px;
        border-radius: 4px;
        font-size: 12px;
        overflow-x: auto;
        white-space: pre-wrap;
        word-wrap: break-word;
      }
    }
  }
}
</style> 
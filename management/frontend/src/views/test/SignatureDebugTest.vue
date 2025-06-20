<template>
  <div class="signature-debug-test">
    <div class="page-header">
      <h2>签名调试测试</h2>
      <p>比较前后端签名生成过程，帮助排查签名不匹配问题</p>
    </div>

    <div class="test-container">
      <el-card class="form-card">
        <template #header>
          <span>测试数据</span>
        </template>

        <el-form :model="testData" label-width="120px">
          <el-form-item label="角色名称">
            <el-input v-model="testData.roleName" placeholder="请输入角色名称" />
          </el-form-item>
          <el-form-item label="角色编码">
            <el-input v-model="testData.roleCode" placeholder="请输入角色编码" />
          </el-form-item>
          <el-form-item label="描述">
            <el-input v-model="testData.description" placeholder="请输入描述（可为空）" />
          </el-form-item>
          <el-form-item label="状态">
            <el-radio-group v-model="testData.status">
              <el-radio :label="1">启用</el-radio>
              <el-radio :label="0">禁用</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="权限ID">
            <el-input v-model="permissionIdsInput" placeholder="输入权限ID，用逗号分隔" @input="updatePermissionIds" />
          </el-form-item>
        </el-form>

        <div class="test-actions">
          <el-button type="primary" @click="generateSignature">生成签名</el-button>
          <el-button @click="resetData">重置</el-button>
        </div>
      </el-card>

      <el-card class="result-card" v-if="signatureResult">
        <template #header>
          <span>签名结果</span>
        </template>

        <div class="result-content">
          <div class="result-item">
            <label>时间戳:</label>
            <code>{{ signatureResult.timestamp }}</code>
          </div>

          <div class="result-item">
            <label>签名参数:</label>
            <pre class="code-block">{{ JSON.stringify(signatureResult.signParams, null, 2) }}</pre>
          </div>

          <div class="result-item">
            <label>签名字符串:</label>
            <code class="code-block">{{ signatureResult.signString }}</code>
          </div>

          <div class="result-item">
            <label>生成的签名:</label>
            <code class="signature-value">{{ signatureResult.signature }}</code>
          </div>

          <div class="result-item">
            <label>请求头:</label>
            <pre class="code-block">{{ JSON.stringify(signatureResult.headers, null, 2) }}</pre>
          </div>
        </div>
      </el-card>
    </div>

    <div class="instructions">
      <el-card>
        <template #header>
          <span>使用说明</span>
        </template>
        <ol>
          <li>填写测试数据（特别注意空字符串的处理）</li>
          <li>点击"生成签名"查看前端签名生成过程</li>
          <li>复制相同的测试数据到实际角色创建接口</li>
          <li>对比前后端生成的签名字符串和最终签名</li>
          <li>确保前后端处理空值的方式一致</li>
        </ol>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { SignatureUtil } from '@/utils/signature'
import CryptoJS from 'crypto-js'

// 测试数据
const testData = reactive({
  roleName: 'OOO',
  roleCode: 'OOO', 
  description: '',
  status: 1,
  permissionIds: [] as number[]
})

// 权限ID输入
const permissionIdsInput = ref('')

// 签名结果
const signatureResult = ref<{
  timestamp: number
  signParams: any
  signString: string
  signature: string
  headers: any
} | null>(null)

// 更新权限ID
const updatePermissionIds = () => {
  if (permissionIdsInput.value.trim()) {
    testData.permissionIds = permissionIdsInput.value
      .split(',')
      .map(id => parseInt(id.trim()))
      .filter(id => !isNaN(id))
  } else {
    testData.permissionIds = []
  }
}

// 生成签名
const generateSignature = () => {
  const timestamp = Date.now()
  
  // 模拟前端的签名参数提取逻辑
  const signParams: Record<string, any> = {}
  
  if (testData.roleCode !== null && testData.roleCode !== undefined) signParams.roleCode = testData.roleCode
  if (testData.roleName !== null && testData.roleName !== undefined) signParams.roleName = testData.roleName
  if (testData.status !== null && testData.status !== undefined) signParams.status = testData.status
  if (testData.description !== null && testData.description !== undefined) signParams.description = testData.description
  
  if (testData.permissionIds && testData.permissionIds.length > 0) {
    signParams.permissionIds = testData.permissionIds.sort().join(',')
  }

  // 手动生成签名字符串以便调试
  const sortedParams = { ...signParams, timestamp }
  const queryString = Object.keys(sortedParams)
    .map(key => `${key}=${(sortedParams as any)[key]}`)
    .join('&')

  const signature = CryptoJS.MD5(queryString).toString().toUpperCase()

  signatureResult.value = {
    timestamp,
    signParams,
    signString: queryString,
    signature,
    headers: {
      'X-Signature': signature,
      'X-Timestamp': timestamp.toString()
    }
  }

  console.log('调试签名生成:')
  console.log('参数:', signParams)
  console.log('签名字符串:', queryString)
  console.log('最终签名:', signature)
}

// 重置数据
const resetData = () => {
  Object.assign(testData, {
    roleName: '',
    roleCode: '',
    description: '',
    status: 1,
    permissionIds: []
  })
  permissionIdsInput.value = ''
  signatureResult.value = null
}
</script>

<style lang="scss" scoped>
.signature-debug-test {
  padding: 20px;

  .page-header {
    margin-bottom: 20px;
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
    gap: 20px;
    margin-bottom: 20px;

    .form-card,
    .result-card {
      flex: 1;
    }

    .test-actions {
      display: flex;
      gap: 12px;
      justify-content: center;
      margin-top: 20px;
      padding-top: 20px;
      border-top: 1px solid var(--border-color);
    }

    .result-content {
      .result-item {
        margin-bottom: 16px;

        label {
          display: block;
          font-weight: 600;
          color: var(--text-primary);
          margin-bottom: 4px;
        }

        .code-block {
          background: #f5f7fa;
          padding: 12px;
          border-radius: 4px;
          font-family: 'Consolas', 'Monaco', monospace;
          font-size: 12px;
          white-space: pre-wrap;
          word-break: break-all;
        }

        .signature-value {
          background: #e8f4fd;
          color: #1890ff;
          padding: 8px 12px;
          border-radius: 4px;
          font-family: 'Consolas', 'Monaco', monospace;
          font-size: 14px;
          font-weight: bold;
          display: block;
          word-break: break-all;
        }
      }
    }
  }

  .instructions {
    ol {
      margin: 0;
      padding-left: 20px;

      li {
        margin-bottom: 8px;
        line-height: 1.5;
      }
    }
  }
}

@media (max-width: 768px) {
  .test-container {
    flex-direction: column;
  }
}
</style> 
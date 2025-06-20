<template>
  <div class="role-signature-test">
    <div class="page-header">
      <h2>角色新增签名测试</h2>
      <p>测试角色新增接口的签名验证功能</p>
    </div>

    <div class="test-container">
      <el-card class="test-form-card">
        <template #header>
          <div class="card-header">
            <span>角色信息</span>
            <el-tag :type="testResult?.success ? 'success' : 'danger'" v-if="testResult">
              {{ testResult.success ? '成功' : '失败' }}
            </el-tag>
          </div>
        </template>

        <el-form
          ref="formRef"
          :model="formData"
          :rules="formRules"
          label-width="120px"
        >
          <el-form-item label="角色名称" prop="roleName">
            <el-input v-model="formData.roleName" placeholder="请输入角色名称" />
          </el-form-item>
          <el-form-item label="角色编码" prop="roleCode">
            <el-input v-model="formData.roleCode" placeholder="请输入角色编码" />
          </el-form-item>
          <el-form-item label="描述" prop="description">
            <el-input
              v-model="formData.description"
              type="textarea"
              :rows="3"
              placeholder="请输入角色描述"
            />
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-radio-group v-model="formData.status">
              <el-radio :label="1">启用</el-radio>
              <el-radio :label="0">禁用</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="权限ID">
            <el-input
              v-model="permissionIdsInput"
              placeholder="请输入权限ID，用逗号分隔（可选）"
              @input="handlePermissionIdsChange"
            />
          </el-form-item>
        </el-form>

        <div class="test-actions">
          <el-button type="primary" @click="testCreateRole" :loading="testing">
            测试创建角色
          </el-button>
          <el-button @click="resetForm">重置表单</el-button>
        </div>
      </el-card>

      <el-card class="test-result-card" v-if="testResult">
        <template #header>
          <span>测试结果</span>
        </template>

        <div class="result-content">
          <div class="result-item">
            <label>请求状态:</label>
            <el-tag :type="testResult.success ? 'success' : 'danger'">
              {{ testResult.success ? '成功' : '失败' }}
            </el-tag>
          </div>

          <div class="result-item" v-if="testResult.signature">
            <label>生成的签名:</label>
            <code class="signature-code">{{ testResult.signature }}</code>
          </div>

          <div class="result-item" v-if="testResult.timestamp">
            <label>时间戳:</label>
            <span>{{ testResult.timestamp }}</span>
          </div>

          <div class="result-item" v-if="testResult.signParams">
            <label>签名参数:</label>
            <pre class="params-code">{{ JSON.stringify(testResult.signParams, null, 2) }}</pre>
          </div>

          <div class="result-item">
            <label>响应消息:</label>
            <span>{{ testResult.message }}</span>
          </div>

          <div class="result-item" v-if="testResult.error">
            <label>错误详情:</label>
            <pre class="error-code">{{ testResult.error }}</pre>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { roleApi } from '@/api/system'

// 表单数据
const formData = reactive({
  roleName: '测试角色',
  roleCode: 'TEST_ROLE',
  description: '这是一个测试角色',
  status: 1,
  permissionIds: [] as number[]
})

// 权限ID输入
const permissionIdsInput = ref('')

// 表单规则
const formRules = {
  roleName: [
    { required: true, message: '请输入角色名称', trigger: 'blur' }
  ],
  roleCode: [
    { required: true, message: '请输入角色编码', trigger: 'blur' }
  ]
}

// 表单引用
const formRef = ref()

// 测试状态
const testing = ref(false)

// 测试结果
const testResult = ref<{
  success: boolean
  message: string
  signature?: string
  timestamp?: number
  signParams?: any
  error?: string
} | null>(null)

// 处理权限ID输入变化
const handlePermissionIdsChange = () => {
  if (permissionIdsInput.value.trim()) {
    formData.permissionIds = permissionIdsInput.value
      .split(',')
      .map(id => parseInt(id.trim()))
      .filter(id => !isNaN(id))
  } else {
    formData.permissionIds = []
  }
}

// 测试创建角色
const testCreateRole = async () => {
  const valid = await formRef.value?.validate()
  if (!valid) return

  testing.value = true
  testResult.value = null

  try {
    console.log('开始测试角色创建，数据:', formData)
    
    // 准备测试数据
    const testData = {
      roleName: formData.roleName,
      roleCode: formData.roleCode,
      description: formData.description,
      status: formData.status,
      permissionIds: formData.permissionIds.length > 0 ? formData.permissionIds : undefined
    }

    // 移除undefined字段
    Object.keys(testData).forEach(key => {
      if (testData[key] === undefined) {
        delete testData[key]
      }
    })

    console.log('处理后的测试数据:', testData)

    // 调用API（会自动生成签名）
    const response = await roleApi.createRole(testData)
    
    console.log('API响应:', response)

    testResult.value = {
      success: response.code === 200,
      message: response.message || '创建成功',
      signature: 'API调用中已生成（查看浏览器开发者工具网络标签）',
      timestamp: Date.now(),
      signParams: testData
    }

    if (response.code === 200) {
      ElMessage.success('角色创建成功！签名验证通过')
    } else {
      ElMessage.error(`角色创建失败: ${response.message}`)
    }

  } catch (error: any) {
    console.error('测试角色创建失败:', error)
    
    testResult.value = {
      success: false,
      message: error.response?.data?.message || error.message || '未知错误',
      error: JSON.stringify(error.response?.data || error.message, null, 2)
    }

    ElMessage.error('测试失败: ' + (error.response?.data?.message || error.message))
  } finally {
    testing.value = false
  }
}

// 重置表单
const resetForm = () => {
  formRef.value?.resetFields()
  testResult.value = null
  permissionIdsInput.value = ''
  formData.permissionIds = []
}
</script>

<style lang="scss" scoped>
.role-signature-test {
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
    align-items: flex-start;

    .test-form-card,
    .test-result-card {
      flex: 1;
    }

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
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
        display: flex;
        flex-direction: column;
        gap: 8px;

        label {
          font-weight: 600;
          color: var(--text-primary);
        }

        .signature-code,
        .params-code,
        .error-code {
          background: #f5f7fa;
          padding: 12px;
          border-radius: 4px;
          font-family: 'Consolas', 'Monaco', monospace;
          font-size: 12px;
          word-break: break-all;
          white-space: pre-wrap;
        }

        .signature-code {
          border-left: 4px solid #409eff;
        }

        .error-code {
          border-left: 4px solid #f56c6c;
          background: #fef0f0;
        }
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
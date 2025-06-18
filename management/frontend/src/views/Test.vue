<template>
  <div class="test-page">
    <el-card class="mb-4">
      <template #header>
        <div class="card-header">
          <span>🧪 API 数据测试页面</span>
        </div>
      </template>
      
      <div class="test-buttons">
        <el-button @click="testAdminApi" type="primary" :loading="loading.admin">
          测试管理员API
        </el-button>
        <el-button @click="testRoleApi" type="success" :loading="loading.role">
          测试角色API
        </el-button>
        <el-button @click="testPermissionApi" type="info" :loading="loading.permission">
          测试权限API
        </el-button>
        <el-button @click="testLogApi" type="warning" :loading="loading.log">
          测试日志API
        </el-button>
        <el-button @click="clearResults" type="danger">
          清空结果
        </el-button>
      </div>
    </el-card>

    <!-- 测试结果显示 -->
    <el-card v-if="results.length > 0">
      <template #header>
        <div class="card-header">
          <span>📊 测试结果</span>
        </div>
      </template>
      
      <div class="test-results">
        <div v-for="(result, index) in results" :key="index" class="result-item">
          <el-tag :type="result.success ? 'success' : 'danger'" class="mb-2">
            {{ result.api }} - {{ result.success ? '成功' : '失败' }}
          </el-tag>
          
          <div class="result-details">
            <h4>原始响应:</h4>
            <pre>{{ JSON.stringify(result.response, null, 2) }}</pre>
            
            <h4>数据处理结果:</h4>
            <pre>{{ JSON.stringify(result.processedData, null, 2) }}</pre>
            
            <h4>数据类型分析:</h4>
            <ul>
              <li>响应类型: {{ typeof result.response }}</li>
              <li>数据部分类型: {{ typeof result.response?.data }}</li>
              <li>是否包含records: {{ result.response?.data?.records ? '是' : '否' }}</li>
              <li>是否包含list: {{ result.response?.data?.list ? '是' : '否' }}</li>
              <li>数据是否为数组: {{ Array.isArray(result.response?.data) ? '是' : '否' }}</li>
              <li>最终数据长度: {{ result.processedData?.length || 0 }}</li>
            </ul>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 数据表格测试 -->
    <el-card v-if="tableData.length > 0" class="mt-4">
      <template #header>
        <div class="card-header">
          <span>📋 数据表格测试</span>
        </div>
      </template>
      
      <el-table :data="tableData" border style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="realName" label="真实姓名" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'

// 模拟API调用
const mockAdminApi = {
  getAdminList: () => Promise.resolve({
    code: 200,
    data: {
      records: [
        {
          id: 1,
          username: 'admin',
          realName: '超级管理员',
          email: 'admin@nextera.com',
          phone: '13800138000',
          status: 1,
          createTime: '2024-01-01 10:00:00'
        },
        {
          id: 2,
          username: 'manager',
          realName: '张三',
          email: 'zhangsan@nextera.com',
          phone: '13800138001',
          status: 1,
          createTime: '2024-01-01 09:30:00'
        }
      ],
      total: 2
    },
    message: '获取成功'
  })
}

const loading = reactive({
  admin: false,
  role: false,
  permission: false,
  log: false
})

const results = ref<any[]>([])
const tableData = ref<any[]>([])

// 数据处理函数
const processApiData = (response: any) => {
  console.log('处理API响应:', response)
  
  if (!response || (!response.code && !response.success)) {
    return []
  }
  
  const data = response.data
  if (!data) return []
  
  // 尝试不同的数据结构
  if (data.records && Array.isArray(data.records)) {
    return data.records
  } else if (data.list && Array.isArray(data.list)) {
    return data.list
  } else if (Array.isArray(data)) {
    return data
  }
  
  return []
}

const testAdminApi = async () => {
  loading.admin = true
  try {
    const response = await mockAdminApi.getAdminList()
    const processedData = processApiData(response)
    
    results.value.push({
      api: '管理员API',
      success: true,
      response,
      processedData
    })
    
    // 设置表格数据进行测试
    tableData.value = processedData
    
    ElMessage.success(`管理员API测试成功，获取到${processedData.length}条数据`)
  } catch (error) {
    console.error('管理员API测试失败:', error)
    results.value.push({
      api: '管理员API',
      success: false,
      response: error,
      processedData: []
    })
    ElMessage.error('管理员API测试失败')
  } finally {
    loading.admin = false
  }
}

const testRoleApi = async () => {
  loading.role = true
  try {
    // 模拟角色API响应
    const response = {
      code: 200,
      data: [
        { id: 1, roleName: '超级管理员', roleCode: 'SUPER_ADMIN', status: 1 },
        { id: 2, roleName: '系统管理员', roleCode: 'ADMIN', status: 1 }
      ]
    }
    
    const processedData = processApiData(response)
    
    results.value.push({
      api: '角色API',
      success: true,
      response,
      processedData
    })
    
    ElMessage.success(`角色API测试成功，获取到${processedData.length}条数据`)
  } catch (error) {
    console.error('角色API测试失败:', error)
    ElMessage.error('角色API测试失败')
  } finally {
    loading.role = false
  }
}

const testPermissionApi = async () => {
  loading.permission = true
  try {
    // 模拟权限API响应
    const response = {
      success: true,
      result: {
        list: [
          { id: 1, permissionName: '系统管理', type: 'menu', status: 1 },
          { id: 2, permissionName: '用户管理', type: 'menu', status: 1 }
        ],
        total: 2
      }
    }
    
    const processedData = processApiData(response)
    
    results.value.push({
      api: '权限API',
      success: true,
      response,
      processedData
    })
    
    ElMessage.success(`权限API测试成功，获取到${processedData.length}条数据`)
  } catch (error) {
    console.error('权限API测试失败:', error)
    ElMessage.error('权限API测试失败')
  } finally {
    loading.permission = false
  }
}

const testLogApi = async () => {
  loading.log = true
  try {
    // 模拟日志API响应
    const response = {
      status: 200,
      data: {
        content: [
          { id: 1, username: 'admin', operation: '登录', module: '系统', status: '成功' },
          { id: 2, username: 'admin', operation: '查看', module: '用户管理', status: '成功' }
        ],
        totalElements: 2
      }
    }
    
    const processedData = processApiData(response)
    
    results.value.push({
      api: '日志API',
      success: true,
      response,
      processedData
    })
    
    ElMessage.success(`日志API测试成功，获取到${processedData.length}条数据`)
  } catch (error) {
    console.error('日志API测试失败:', error)
    ElMessage.error('日志API测试失败')
  } finally {
    loading.log = false
  }
}

const clearResults = () => {
  results.value = []
  tableData.value = []
  ElMessage.info('测试结果已清空')
}
</script>

<style lang="scss" scoped>
.test-page {
  padding: 20px;
  
  .test-buttons {
    display: flex;
    gap: 10px;
    flex-wrap: wrap;
  }
  
  .test-results {
    .result-item {
      margin-bottom: 20px;
      padding: 15px;
      border: 1px solid #eee;
      border-radius: 6px;
      
      .result-details {
        margin-top: 10px;
        
        h4 {
          margin: 10px 0 5px 0;
          color: #333;
        }
        
        pre {
          background: #f5f5f5;
          padding: 10px;
          border-radius: 4px;
          overflow-x: auto;
          font-size: 12px;
          max-height: 200px;
          overflow-y: auto;
        }
        
        ul {
          margin: 5px 0;
          padding-left: 20px;
          
          li {
            margin: 2px 0;
            font-size: 13px;
          }
        }
      }
    }
  }
}

.mb-4 {
  margin-bottom: 16px;
}

.mt-4 {
  margin-top: 16px;
}

.mb-2 {
  margin-bottom: 8px;
}
</style> 
<template>
  <div class="data-debug">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>🔍 数据调试工具</span>
        </div>
      </template>

      <div class="debug-section">
        <h3>测试操作日志数据显示</h3>
        <el-button @click="testLogData" type="primary" :loading="loading">
          测试操作日志数据
        </el-button>
      </div>

      <!-- 原始数据展示 -->
      <div v-if="rawData" class="debug-section">
        <h4>📋 原始响应数据</h4>
        <el-collapse v-model="activeNames">
          <el-collapse-item title="原始JSON数据" name="1">
            <pre class="json-display">{{ JSON.stringify(rawData, null, 2) }}</pre>
          </el-collapse-item>
        </el-collapse>
      </div>

      <!-- 处理后数据展示 -->
      <div v-if="processedData.length > 0" class="debug-section">
        <h4>🔄 处理后的数据 ({{ processedData.length }}条)</h4>
        <el-table :data="processedData.slice(0, 5)" border style="width: 100%">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="username" label="用户名" width="120" />
          <el-table-column prop="operation" label="操作类型" width="120" />
          <el-table-column prop="module" label="模块" width="120" />
          <el-table-column prop="description" label="描述" show-overflow-tooltip />
          <el-table-column prop="status" label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="row.status === 'success' ? 'success' : 'danger'">
                {{ row.status }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
        <div v-if="processedData.length > 5" class="mt-2">
          <el-text type="info">只显示前5条数据</el-text>
        </div>
      </div>

      <!-- 字段映射信息 -->
      <div v-if="fieldMapping" class="debug-section">
        <h4>🗺️ 字段映射信息</h4>
        <el-table :data="fieldMapping" border style="width: 100%">
          <el-table-column prop="frontend" label="前端字段" />
          <el-table-column prop="backend" label="后端字段" />
          <el-table-column prop="value" label="示例值" show-overflow-tooltip />
        </el-table>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const rawData = ref<any>(null)
const processedData = ref<any[]>([])
const fieldMapping = ref<any[]>([])
const activeNames = ref(['1'])

// 模拟后端返回的真实数据结构
const mockLogData = {
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 501,
        "adminId": 1,
        "adminUsername": "admin",
        "module": "操作日志",
        "operationType": "QUERY",
        "operationDesc": "查询操作日志",
        "requestMethod": "GET",
        "requestUrl": "/api/operation-log/page",
        "requestParams": "[1, 10, null, null, null, null, null]",
        "responseResult": "{\"code\":200,\"message\":\"操作成功\"}",
        "status": 1,
        "operationIp": "本地IP",
        "operationLocation": "本地",
        "browser": "Google Chrome",
        "os": "Windows 10",
        "executionTime": 318,
        "operationTime": "2025-06-18T22:47:00",
        "createTime": "2025-06-18T22:47:00"
      },
      {
        "id": 500,
        "adminId": 1,
        "adminUsername": "admin",
        "module": "操作日志",
        "operationType": "QUERY",
        "operationDesc": "查询操作日志",
        "requestMethod": "GET",
        "requestUrl": "/api/operation-log/page",
        "requestParams": "[1, 10, null, null, null, null, null]",
        "responseResult": "{\"code\":200,\"message\":\"操作成功\"}",
        "status": 1,
        "operationIp": "本地IP",
        "operationLocation": "本地",
        "browser": "Google Chrome",
        "os": "Windows 10",
        "executionTime": 11,
        "operationTime": "2025-06-18T22:42:40",
        "createTime": "2025-06-18T22:42:40"
      }
    ],
    "total": 17,
    "size": 10,
    "current": 1,
    "pages": 2
  }
}

const testLogData = async () => {
  loading.value = true
  try {
    // 模拟API调用延迟
    await new Promise(resolve => setTimeout(resolve, 500))
    
    // 使用真实的后端数据结构
    rawData.value = mockLogData
    
    // 数据处理逻辑
    const response = mockLogData
    let dataList = []
    let total = 0
    
    if (response.data) {
      if (response.data.records && Array.isArray(response.data.records)) {
        dataList = response.data.records
        total = response.data.total || response.data.size || response.data.records.length
      }
    }
    
    console.log('提取的原始数据列表:', dataList)
    console.log('数据总数:', total)
    
    // 数据字段转换
    const transformedData = dataList.map((item: any) => {
      const transformed = {
        id: item.id,
        username: item.adminUsername || item.username,
        operation: item.operationType || item.operation,
        module: item.module,
        description: item.operationDesc || item.description,
        method: item.requestMethod || item.method,
        url: item.requestUrl || item.url,
        ip: item.operationIp || item.ip,
        location: item.operationLocation || item.location,
        userAgent: (item.browser && item.os) ? `${item.browser} / ${item.os}` : item.userAgent,
        status: item.status === 1 ? 'success' : 'failed',
        costTime: item.executionTime || item.costTime,
        createTime: item.operationTime || item.createTime,
        params: item.requestParams || item.params,
        result: item.responseResult || item.result,
        exception: item.exception || null
      }
      
      console.log('单个数据转换:', {
        原始: item,
        转换后: transformed
      })
      
      return transformed
    })
    
    console.log('字段转换后的完整数据:', transformedData)
    processedData.value = transformedData
    
    // 生成字段映射信息
    if (dataList.length > 0) {
      const firstItem = dataList[0]
      fieldMapping.value = [
        { frontend: 'username', backend: 'adminUsername', value: firstItem.adminUsername },
        { frontend: 'operation', backend: 'operationType', value: firstItem.operationType },
        { frontend: 'description', backend: 'operationDesc', value: firstItem.operationDesc },
        { frontend: 'method', backend: 'requestMethod', value: firstItem.requestMethod },
        { frontend: 'url', backend: 'requestUrl', value: firstItem.requestUrl },
        { frontend: 'ip', backend: 'operationIp', value: firstItem.operationIp },
        { frontend: 'location', backend: 'operationLocation', value: firstItem.operationLocation },
        { frontend: 'userAgent', backend: 'browser + os', value: `${firstItem.browser} / ${firstItem.os}` },
        { frontend: 'status', backend: 'status (1=success)', value: firstItem.status },
        { frontend: 'costTime', backend: 'executionTime', value: firstItem.executionTime },
        { frontend: 'createTime', backend: 'operationTime', value: firstItem.operationTime },
        { frontend: 'params', backend: 'requestParams', value: firstItem.requestParams },
        { frontend: 'result', backend: 'responseResult', value: firstItem.responseResult?.substring(0, 50) + '...' }
      ]
    }
    
    ElMessage.success(`数据处理完成！共${transformedData.length}条记录`)
  } catch (error) {
    console.error('测试失败:', error)
    ElMessage.error('测试失败')
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.data-debug {
  padding: 20px;
  
  .debug-section {
    margin-bottom: 20px;
    
    h3, h4 {
      margin-bottom: 10px;
      color: #333;
    }
  }
  
  .json-display {
    background: #f5f5f5;
    padding: 15px;
    border-radius: 4px;
    font-size: 12px;
    max-height: 400px;
    overflow-y: auto;
    white-space: pre-wrap;
    word-break: break-all;
  }
  
  .mt-2 {
    margin-top: 8px;
  }
}
</style> 
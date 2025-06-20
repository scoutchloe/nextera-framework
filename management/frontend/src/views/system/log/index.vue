<template>
  <div class="log-management">
    <div class="page-header">
      <div class="header-title">
        <h2>操作日志</h2>
        <p>查看系统操作记录，监控用户行为</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" :icon="Download" @click="handleExport">
          导出日志
        </el-button>
        <el-button type="danger" :icon="Delete" @click="handleClear">
          清空日志
        </el-button>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="操作人员">
          <el-input
            v-model="searchForm.username"
            placeholder="请输入操作人员"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="操作类型">
          <el-select v-model="searchForm.operation" placeholder="请选择操作类型" clearable>
            <el-option label="登录" value="login" />
            <el-option label="登出" value="logout" />
            <el-option label="新增" value="create" />
            <el-option label="修改" value="update" />
            <el-option label="删除" value="delete" />
            <el-option label="查询" value="query" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作模块">
          <el-select v-model="searchForm.module" placeholder="请选择操作模块" clearable>
            <el-option label="用户管理" value="user" />
            <el-option label="角色管理" value="role" />
            <el-option label="权限管理" value="permission" />
            <el-option label="文章管理" value="article" />
            <el-option label="系统设置" value="system" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作时间">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">
            搜索
          </el-button>
          <el-button :icon="Refresh" @click="handleReset">
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 数据表格 -->
    <div class="table-container">
      <el-table
        v-loading="loading"
        :data="tableData"
        style="width: 100%"
        row-key="id"
        stripe
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="日志ID" width="100" />
        <el-table-column prop="username" label="操作人员" width="120" />
        <el-table-column prop="operation" label="操作类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getOperationTagType(row.operation)">
              {{ getOperationText(row.operation) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="module" label="操作模块" width="120" align="center">
          <template #default="{ row }">
            <el-tag type="info">
              {{ getModuleText(row.module) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="操作描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="method" label="请求方式" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="getMethodTagType(row.method)">
              {{ row.method }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="url" label="请求URL" min-width="180" show-overflow-tooltip />
        <el-table-column prop="ip" label="IP地址" width="140" />
        <el-table-column prop="location" label="操作地点" width="120" show-overflow-tooltip />
        <el-table-column prop="userAgent" label="浏览器" width="100" show-overflow-tooltip>
          <template #default="{ row }">
            <el-tooltip :content="row.userAgent" placement="top">
              <span>{{ getBrowserName(row.userAgent) }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="操作状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'success' ? 'success' : 'danger'">
              {{ row.status === 'success' ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="costTime" label="耗时(ms)" width="100" align="center" />
        <el-table-column prop="createTime" label="操作时间" width="180" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              link
              :icon="View"
              @click="handleView(row)"
            >
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          :current-page="pagination.current"
          :page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailVisible"
      title="操作日志详情"
      width="800px"
    >
      <div class="log-detail" v-if="currentLog">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="日志ID">{{ currentLog.id }}</el-descriptions-item>
          <el-descriptions-item label="操作人员">{{ currentLog.username }}</el-descriptions-item>
          <el-descriptions-item label="操作类型">
            <el-tag :type="getOperationTagType(currentLog.operation)">
              {{ getOperationText(currentLog.operation) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="操作模块">
            <el-tag type="info">{{ getModuleText(currentLog.module) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="操作描述" :span="2">{{ currentLog.description }}</el-descriptions-item>
          <el-descriptions-item label="请求方式">
            <el-tag size="small" :type="getMethodTagType(currentLog.method)">
              {{ currentLog.method }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="请求URL">{{ currentLog.url }}</el-descriptions-item>
          <el-descriptions-item label="IP地址">{{ currentLog.ip }}</el-descriptions-item>
          <el-descriptions-item label="操作地点">{{ currentLog.location }}</el-descriptions-item>
          <el-descriptions-item label="操作状态">
            <el-tag :type="currentLog.status === 'success' ? 'success' : 'danger'">
              {{ currentLog.status === 'success' ? '成功' : '失败' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="耗时">{{ currentLog.costTime }}ms</el-descriptions-item>
          <el-descriptions-item label="操作时间" :span="2">{{ currentLog.createTime }}</el-descriptions-item>
        </el-descriptions>

        <!-- 请求参数 -->
        <div class="detail-section" v-if="currentLog.params">
          <h4>请求参数</h4>
          <el-input
            v-model="currentLog.params"
            type="textarea"
            :rows="6"
            readonly
            class="params-textarea"
          />
        </div>

        <!-- 响应结果 -->
        <div class="detail-section" v-if="currentLog.result">
          <h4>响应结果</h4>
          <el-input
            v-model="currentLog.result"
            type="textarea"
            :rows="6"
            readonly
            class="result-textarea"
          />
        </div>

        <!-- 异常信息 -->
        <div class="detail-section" v-if="currentLog.exception">
          <h4>异常信息</h4>
          <el-input
            v-model="currentLog.exception"
            type="textarea"
            :rows="8"
            readonly
            class="exception-textarea"
          />
        </div>

        <!-- 浏览器信息 -->
        <div class="detail-section">
          <h4>浏览器信息</h4>
          <p class="user-agent">{{ currentLog.userAgent }}</p>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="detailVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Download, Delete, View } from '@element-plus/icons-vue'
import { logApi } from '@/api/system'

// 响应式数据
const loading = ref(false)
const detailVisible = ref(false)
const currentLog = ref<any>(null)

// 搜索表单
const searchForm = reactive({
  username: '',
  operation: '',
  module: '',
  dateRange: []
})

// 分页数据
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0
})

// 表格数据 - 初始化为空数组，等待API数据
const tableData = ref<any[]>([])

const selectedRows = ref<any[]>([])

// 获取操作类型标签类型
const getOperationTagType = (operation: string) => {
  const typeMap: Record<string, string> = {
    login: 'success',
    logout: 'info',
    create: 'primary',
    update: 'warning',
    delete: 'danger',
    query: 'info'
  }
  return typeMap[operation] || 'info'
}

// 获取操作类型文本
const getOperationText = (operation: string) => {
  const textMap: Record<string, string> = {
    login: '登录',
    logout: '登出',
    create: '新增',
    update: '修改',
    delete: '删除',
    query: '查询'
  }
  return textMap[operation] || operation
}

// 获取模块文本
const getModuleText = (module: string) => {
  const textMap: Record<string, string> = {
    user: '用户管理',
    role: '角色管理',
    permission: '权限管理',
    article: '文章管理',
    system: '系统设置'
  }
  return textMap[module] || module
}

// 获取请求方式标签类型
const getMethodTagType = (method: string) => {
  const typeMap: Record<string, string> = {
    GET: 'success',
    POST: 'primary',
    PUT: 'warning',
    DELETE: 'danger',
    PATCH: 'info'
  }
  return typeMap[method] || 'info'
}

// 获取浏览器名称
const getBrowserName = (userAgent: string) => {
  if (userAgent.includes('Chrome')) return 'Chrome'
  if (userAgent.includes('Firefox')) return 'Firefox'
  if (userAgent.includes('Safari')) return 'Safari'
  if (userAgent.includes('Edge')) return 'Edge'
  return 'Other'
}

// 方法
const handleSearch = () => {
  pagination.current = 1
  loadTableData()
}

const handleReset = () => {
  Object.assign(searchForm, {
    username: '',
    operation: '',
    module: '',
    dateRange: []
  })
  handleSearch()
}

const loadTableData = async () => {
  loading.value = true
  
  try {
    const params = {
      page: pagination.current,
      pageSize: pagination.pageSize,
      username: searchForm.username || undefined,
      operation: searchForm.operation || undefined,
      module: searchForm.module || undefined,
      startDate: searchForm.dateRange?.[0] || undefined,
      endDate: searchForm.dateRange?.[1] || undefined
    }
    
    console.log('加载操作日志，参数:', params)
    const response = await logApi.getLogList(params)
    console.log('操作日志响应:', response)
    console.log('响应数据结构:', response.data)
    
    if (response.code === 200 || response.success) {
      // 处理不同的响应数据结构
      let dataList = []
      let total = 0
      
      if (response.data) {
        // 检查是否是分页结构
        if (response.data.records && Array.isArray(response.data.records)) {
          dataList = response.data.records
          total = response.data.total || response.data.size || response.data.records.length
        } else if (response.data.list && Array.isArray(response.data.list)) {
          dataList = response.data.list
          total = response.data.total || response.data.size || response.data.list.length
        } else if (Array.isArray(response.data)) {
          dataList = response.data
          total = response.data.length
        } else {
          console.warn('未识别的数据结构:', response.data)
          dataList = []
          total = 0
        }
      }
      
      console.log('处理后的日志列表:', dataList)
      console.log('日志总数:', total)
      console.log('分页信息:', {
        total: response.data?.total,
        size: response.data?.size,
        current: response.data?.current,
        pages: response.data?.pages
      })
      
      // 数据字段转换 - 将后端字段映射到前端期望的字段
      const transformedData = dataList.map((item: any) => ({
        id: item.id,
        username: item.adminUsername || item.username,  // 后端字段为adminUsername
        operation: item.operationType || item.operation,  // 后端字段为operationType
        module: item.module,
        description: item.operationDesc || item.description,  // 后端字段为operationDesc
        method: item.requestMethod || item.method,  // 后端字段为requestMethod
        url: item.requestUrl || item.url,  // 后端字段为requestUrl
        ip: item.operationIp || item.ip,  // 后端字段为operationIp
        location: item.operationLocation || item.location,  // 后端字段为operationLocation
        userAgent: item.browser + ' / ' + item.os || item.userAgent,  // 后端有browser和os字段
        status: item.status === 1 ? 'success' : 'failed',  // 后端status为数字
        costTime: item.executionTime || item.costTime,  // 后端字段为executionTime
        createTime: item.operationTime || item.createTime,  // 后端字段为operationTime
        params: item.requestParams || item.params,  // 后端字段为requestParams
        result: item.responseResult || item.result,  // 后端字段为responseResult
        exception: item.exception || null,
        // 保留原始数据
        ...item
      }))
      
      console.log('字段转换后的数据:', transformedData)
      
      // 强制设置表格数据
      tableData.value = [...transformedData]  // 使用转换后的数据
      pagination.total = total
      
      // 确保Vue能检测到数据变化
      nextTick(() => {
        console.log('表格数据已更新:', tableData.value)
        console.log('表格数据长度:', tableData.value.length)
      })
      
      if (dataList.length > 0) {
        ElMessage.success(`操作日志加载成功，共${total}条数据`)
      } else {
        ElMessage.info('暂无操作日志数据')
      }
    } else {
      ElMessage.error(response.message || '获取操作日志失败')
      tableData.value = []
      pagination.total = 0
    }
  } catch (error) {
    console.error('获取操作日志失败:', error)
    ElMessage.error('获取操作日志失败，请检查网络连接')
    // 保持现有模拟数据作为fallback
    pagination.total = tableData.value.length
  } finally {
    loading.value = false
  }
}

const handleView = (row: any) => {
  currentLog.value = row
  detailVisible.value = true
}

const handleExport = async () => {
  try {
    const params = {
      username: searchForm.username || undefined,
      operation: searchForm.operation || undefined,
      module: searchForm.module || undefined,
      startDate: searchForm.dateRange?.[0] || undefined,
      endDate: searchForm.dateRange?.[1] || undefined
    }
    
    console.log('导出操作日志，参数:', params)
    ElMessage.info('正在导出操作日志...')
    
    await logApi.exportLogs(params)
    ElMessage.success('操作日志导出成功')
  } catch (error) {
    console.error('导出操作日志失败:', error)
    ElMessage.error('导出失败，请检查网络连接')
  }
}

const handleClear = () => {
  ElMessageBox.confirm(
    '确定要清空所有日志吗？此操作不可恢复！',
    '清空确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      console.log('清空操作日志')
      const response = await logApi.clearLogs()
      console.log('清空日志响应:', response)
      
      if (response.code === 200) {
        ElMessage.success('日志清空成功')
        loadTableData()
      } else {
        ElMessage.error(response.message || '清空失败')
      }
    } catch (error) {
      console.error('清空日志失败:', error)
      ElMessage.error('清空失败，请检查网络连接')
    }
  }).catch(() => {
    // 取消清空
  })
}

const handleSelectionChange = (selection: any[]) => {
  selectedRows.value = selection
}

const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  loadTableData()
}

const handleCurrentChange = (current: number) => {
  pagination.current = current
  loadTableData()
}

// 组件挂载时加载数据
onMounted(() => {
  loadTableData()
})
</script>

<style lang="scss" scoped>
.log-management {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 24px;
    padding-bottom: 16px;
    border-bottom: 1px solid var(--border-color);
    
    .header-title {
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
    
    .header-actions {
      display: flex;
      gap: 12px;
    }
  }

  .search-bar {
    background: var(--bg-primary);
    padding: 20px;
    border-radius: var(--radius-md);
    margin-bottom: 16px;
    border: 1px solid var(--border-color);
    
    .search-form {
      margin: 0;
      
      .el-form-item {
        margin-bottom: 16px;
      }
    }
  }

  .table-container {
    background: var(--bg-primary);
    border-radius: var(--radius-md);
    border: 1px solid var(--border-color);
    overflow: hidden;
    
    .el-table {
      border: none;
    }
    
    .pagination-container {
      padding: 16px 20px;
      border-top: 1px solid var(--border-color);
      display: flex;
      justify-content: flex-end;
    }
  }

  .log-detail {
    .detail-section {
      margin-top: 24px;
      
      h4 {
        margin: 0 0 12px 0;
        color: var(--text-primary);
        font-size: 16px;
        font-weight: 600;
      }
      
      .params-textarea :deep(.el-textarea__inner) {
        background-color: #f8f9fa;
        border-color: #e9ecef;
      }
      
      .result-textarea :deep(.el-textarea__inner) {
        background-color: #f0f9ff;
        border-color: #bae6fd;
      }
      
      .exception-textarea :deep(.el-textarea__inner) {
        background-color: #fef2f2;
        border-color: #fecaca;
        color: #dc2626;
      }
      
      .user-agent {
        padding: 12px;
        background-color: #f8f9fa;
        border: 1px solid #e9ecef;
        border-radius: var(--radius-md);
        color: var(--text-secondary);
        font-size: 14px;
        line-height: 1.5;
        word-break: break-all;
      }
    }
  }
}

// 对话框样式调整
:deep(.el-dialog__body) {
  padding: 20px;
}

:deep(.el-descriptions) {
  .el-descriptions__label {
    font-weight: 600;
    color: var(--text-primary);
  }
}
</style> 
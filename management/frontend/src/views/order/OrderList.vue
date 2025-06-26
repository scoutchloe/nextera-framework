<template>
  <div class="order-list-container">
    <!-- 搜索区域 -->
    <el-card class="search-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">订单搜索</span>
          <el-button 
            type="primary" 
            @click="toggleSearchExpanded"
            size="small"
          >
            {{ searchExpanded ? '收起' : '展开' }}
          </el-button>
        </div>
      </template>

      <el-form 
        ref="searchFormRef"
        :model="searchForm" 
        :inline="true" 
        label-width="80px"
        class="search-form"
      >
        <!-- 基础搜索 -->
        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item label="订单号">
              <el-input 
                v-model="searchForm.orderNo" 
                placeholder="请输入订单号"
                clearable
                @keyup.enter="handleSearch"
              />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="用户名">
              <el-input 
                v-model="searchForm.username" 
                placeholder="请输入用户名"
                clearable
                @keyup.enter="handleSearch"
              />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="订单状态">
              <el-select 
                v-model="searchForm.statusList" 
                placeholder="选择状态"
                multiple
                clearable
                style="width: 100%"
              >
                <el-option
                  v-for="status in statusOptions"
                  :key="status.value"
                  :label="status.label"
                  :value="status.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="">
              <el-button type="primary" @click="handleSearch" :loading="loading">
                搜索
              </el-button>
              <el-button @click="handleReset">重置</el-button>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 高级搜索 -->
        <div v-show="searchExpanded" class="advanced-search">
          <el-row :gutter="20">
            <el-col :span="6">
              <el-form-item label="支付方式">
                <el-select 
                  v-model="searchForm.paymentMethodList" 
                  placeholder="选择支付方式"
                  multiple
                  clearable
                  style="width: 100%"
                >
                  <el-option
                    v-for="method in paymentOptions"
                    :key="method.value"
                    :label="method.label"
                    :value="method.value"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="商品名称">
                <el-input 
                  v-model="searchForm.productName" 
                  placeholder="请输入商品名称"
                  clearable
                  @keyup.enter="handleSearch"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="创建时间">
                <el-date-picker
                  v-model="createdTimeRange"
                  type="datetimerange"
                  range-separator="至"
                  start-placeholder="开始时间"
                  end-placeholder="结束时间"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
          </el-row>
        </div>
      </el-form>
    </el-card>

    <!-- 操作区域 -->
    <el-card class="action-card" shadow="never">
      <div class="action-buttons">
        <el-button type="primary" @click="handleRefresh" :loading="loading">
          刷新
        </el-button>
        <el-button 
          type="success" 
          @click="handleExport" 
          :loading="exporting"
          :disabled="!hasData"
        >
          导出Excel
        </el-button>
        <el-button 
          type="warning" 
          @click="handleBatchSyncToES" 
          :loading="batchSyncing"
          :disabled="!selectedOrders.length"
        >
          <el-icon><Refresh /></el-icon>
          批量同步ES ({{ selectedOrders.length }})
        </el-button>
      </div>
      <div class="action-info">
        <span>共 {{ pagination.total }} 条记录</span>
        <span v-if="selectedOrders.length > 0">，已选择 {{ selectedOrders.length }} 条</span>
      </div>
    </el-card>

    <!-- 数据表格 -->
    <el-card class="table-card" shadow="never">
      <el-table
        ref="tableRef"
        v-loading="loading"
        :data="orderList"
        stripe
        style="width: 100%"
        @selection-change="handleSelectionChange"
        @sort-change="handleSortChange"
      >
        <el-table-column type="selection" width="55" fixed="left" />
        <el-table-column prop="orderNo" label="订单号" width="180" fixed="left">
          <template #default="{ row }">
            <el-button 
              type="text" 
              @click="showOrderDetail(row)"
              class="order-no-link"
            >
              {{ row.orderNo }}
            </el-button>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="totalAmount" label="订单金额" width="120" sortable="custom">
          <template #default="{ row }">
            <span class="amount">¥{{ (row.totalAmount || 0).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="订单状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusColor(row.status)">
              {{ getStatusDescription(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="paymentMethod" label="支付方式" width="100">
          <template #default="{ row }">
            {{ getPaymentMethodDescription(row.paymentMethod) }}
          </template>
        </el-table-column>
        <el-table-column prop="createdTime" label="创建时间" width="160" sortable="custom" />
        <el-table-column prop="updatedTime" label="更新时间" width="160" sortable="custom" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button type="text" size="small" @click="showOrderDetail(row)">
                详情
              </el-button>
              <el-button 
                type="text" 
                size="small" 
                @click="showStatusHistory(row)"
              >
                状态历史
              </el-button>
              <el-button 
                type="text" 
                size="small" 
                @click="handleSyncToES(row)"
                :loading="row.syncing"
              >
                同步ES
              </el-button>
            </div>
            
            <!-- 状态更新区域 -->
            <div class="status-update-area" v-if="getNextStatus(row.status)">
              <div class="update-text">更新状态</div>
              <div 
                class="next-status" 
                @click="handleStatusUpdate(row)"
                :class="{ 'updating': row.updating }"
              >
                <el-icon v-if="row.updating" class="loading-icon">
                  <Loading />
                </el-icon>
                <span v-else>→ {{ getNextStatusDescription(row.status) }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          :current-page="pagination.current"
          :page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 订单详情抽屉 -->
    <el-drawer
      v-model="orderDetailVisible"
      title="订单详情"
      direction="rtl"
      size="60%"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
    >
      <div v-if="selectedOrder" v-loading="detailLoading" class="order-detail-content">
        <!-- 订单基本信息 -->
        <div class="detail-section">
          <h3 class="section-title">订单基本信息</h3>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="订单号">
              <span class="order-no">{{ selectedOrder.orderNo }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="用户名">{{ selectedOrder.username || '-' }}</el-descriptions-item>
            <el-descriptions-item label="订单金额">
              <span class="amount">¥{{ (selectedOrder.totalAmount || 0).toFixed(2) }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="订单状态">
              <el-tag :type="getStatusColor(selectedOrder.status)">
                {{ getStatusDescription(selectedOrder.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="支付方式">{{ getPaymentMethodDescription(selectedOrder.paymentMethod) }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ selectedOrder.createdTime }}</el-descriptions-item>
            <el-descriptions-item label="更新时间">{{ selectedOrder.updatedTime }}</el-descriptions-item>
            <el-descriptions-item label="备注">{{ selectedOrder.remark || '无' }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 订单明细 -->
        <div class="detail-section">
          <h3 class="section-title">订单明细</h3>
          <el-table :data="selectedOrder.orderItems || []" border style="width: 100%">
            <el-table-column prop="productName" label="商品名称" />
            <el-table-column prop="productPrice" label="单价" width="100">
              <template #default="{ row }">
                ¥{{ (row.productPrice || 0).toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column prop="quantity" label="数量" width="60" />
            <el-table-column prop="totalPrice" label="小计" width="100">
              <template #default="{ row }">
                <span class="amount">¥{{ (row.totalPrice || 0).toFixed(2) }}</span>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- 状态流转历史 -->
        <div class="detail-section">
          <h3 class="section-title">状态流转历史</h3>
          <el-timeline v-if="selectedOrder.statusHistory && selectedOrder.statusHistory.length">
            <el-timeline-item
              v-for="(history, index) in selectedOrder.statusHistory"
              :key="index"
              :timestamp="history.createdTime"
              placement="top"
              :type="getTimelineType(history.status)"
            >
              <div class="status-history-item">
                <div class="status-info">
                  <el-tag :type="getStatusColor(history.status)" size="small">
                    {{ getStatusDescription(history.status) }}
                  </el-tag>
                  <span class="operator" v-if="history.operator">
                    操作人：{{ history.operator }}
                  </span>
                </div>
                <div class="status-remark" v-if="history.remark">
                  备注：{{ history.remark }}
                </div>
              </div>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-else description="暂无状态流转记录" :image-size="60" />
        </div>

        <!-- 操作按钮 -->
        <div class="detail-actions">
          <div class="action-row">
            <el-button @click="orderDetailVisible = false">关闭</el-button>
            <el-button type="primary" @click="refreshOrderDetail" :loading="detailLoading">刷新</el-button>
            <el-button 
              type="success" 
              @click="handleSyncToES(selectedOrder)"
              :loading="selectedOrder.syncing"
            >
              同步ES
            </el-button>
          </div>
          
          <!-- 状态更新区域 -->
          <div class="drawer-status-update" v-if="getNextStatus(selectedOrder.status)">
            <div class="update-label">更新状态</div>
            <div 
              class="next-status-item" 
              @click="handleStatusUpdate(selectedOrder)"
              :class="{ 'updating': selectedOrder.updating }"
            >
              <el-icon v-if="selectedOrder.updating" class="loading-icon">
                <Loading />
              </el-icon>
              <span v-else>→ {{ getNextStatusDescription(selectedOrder.status) }}</span>
            </div>
          </div>
        </div>
      </div>
    </el-drawer>

    <!-- 状态历史对话框 -->
    <el-dialog
      v-model="statusHistoryVisible"
      title="订单状态流转历史"
      width="60%"
      :close-on-click-modal="false"
    >
      <div v-loading="historyLoading">
        <div v-if="currentOrderNo" class="order-info">
          <el-tag type="info">订单号：{{ currentOrderNo }}</el-tag>
        </div>
        
        <el-timeline v-if="statusHistoryList.length" style="margin-top: 20px;">
          <el-timeline-item
            v-for="(history, index) in statusHistoryList"
            :key="index"
            :timestamp="history.createdTime"
            placement="top"
            :type="getTimelineType(history.status)"
          >
            <div class="status-history-item">
              <div class="status-info">
                <el-tag :type="getStatusColor(history.status)" size="small">
                  {{ getStatusDescription(history.status) }}
                </el-tag>
                <span class="operator" v-if="history.operator">
                  操作人：{{ history.operator }}
                </span>
              </div>
              <div class="status-remark" v-if="history.remark">
                备注：{{ history.remark }}
              </div>
            </div>
          </el-timeline-item>
        </el-timeline>
        
        <el-empty v-else description="暂无状态流转记录" :image-size="80" />
      </div>
      
      <template #footer>
        <el-button @click="statusHistoryVisible = false">关闭</el-button>
        <el-button type="primary" @click="refreshStatusHistory" :loading="historyLoading">刷新</el-button>
      </template>
    </el-dialog>


  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, Loading } from '@element-plus/icons-vue'
// import { orderApi } from '@/api/orderApi'

// 获取认证头部的辅助函数
const getAuthHeaders = () => {
  const token = localStorage.getItem('token')
  return {
    'Content-Type': 'application/json',
    'Authorization': token ? `Bearer ${token}` : ''
  }
}

// 临时定义orderApi以修复构建错误
const orderApi = {
  getOrderPage: (params: any) => 
    fetch('/api/order/page', { 
      method: 'POST', 
      headers: getAuthHeaders(), 
      body: JSON.stringify(params) 
    }).then(r => r.json()),
    
  getOrderDetail: (id: number) => 
    fetch(`/api/order/${id}`, { 
      headers: getAuthHeaders() 
    }).then(r => r.json()),
    
  updateOrderStatus: (id: number, status: number) => 
    fetch(`/api/order/${id}/status?status=${status}`, { 
      method: 'PUT', 
      headers: getAuthHeaders() 
    }).then(r => r.json()),
    
  syncOrderToES: (id: number) => 
    fetch(`/api/order/${id}/sync`, { 
      method: 'POST', 
      headers: getAuthHeaders() 
    }).then(r => r.json()),
    
  batchSyncOrdersToES: (orderIds: number[]) => 
    fetch(`/api/order/batch/sync?orderIds=${orderIds.join(',')}`, { 
      method: 'POST', 
      headers: getAuthHeaders() 
    }).then(r => r.json()),
    
  exportOrders: (params: any) => {
    const now = new Date()
    const timestamp = now.getFullYear() + String(now.getMonth() + 1).padStart(2, '0') + String(now.getDate()).padStart(2, '0') + '_' + String(now.getHours()).padStart(2, '0') + String(now.getMinutes()).padStart(2, '0') + String(now.getSeconds()).padStart(2, '0')
    const filename = `订单导出_${timestamp}.xlsx`
    return fetch('/api/order/export', { 
      method: 'POST', 
      headers: getAuthHeaders(), 
      body: JSON.stringify(params) 
    })
      .then(response => response.blob())
      .then(blob => {
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = filename
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        window.URL.revokeObjectURL(url)
      })
  }
}

// 类型定义
interface OrderSearchForm {
  orderNo: string
  username: string
  statusList: number[]
  paymentMethodList: number[]
  productName: string
  createdTimeStart?: string
  createdTimeEnd?: string
  current: number
  size: number
  sortField: string
  sortOrder: string
}

interface StatusHistory {
  id: number
  status: number
  operator?: string
  remark?: string
  createdTime: string
}

interface OrderItem {
  id: number
  orderNo: string
  username?: string
  totalAmount: number
  status: number
  paymentMethod: number
  createdTime: string
  updatedTime: string
  remark?: string
  orderItems?: any[]
  statusHistory?: StatusHistory[]
  syncing?: boolean
  updating?: boolean
  [key: string]: any
}

// 响应式数据
const loading = ref(false)
const exporting = ref(false)
const detailLoading = ref(false)
const searchExpanded = ref(false)
const orderDetailVisible = ref(false)
const selectedOrder = ref<OrderItem | null>(null)
const batchSyncing = ref(false)
const statusHistoryVisible = ref(false)
const historyLoading = ref(false)
const statusHistoryList = ref<StatusHistory[]>([])
const currentOrderNo = ref('')
const currentOrderId = ref<number | null>(null)

// 组件引用
const searchFormRef = ref()
const tableRef = ref()

// 搜索表单
const searchForm = reactive<OrderSearchForm>({
  orderNo: '',
  username: '',
  statusList: [],
  paymentMethodList: [],
  productName: '',
  current: 1,
  size: 20,
  sortField: 'createdTime',
  sortOrder: 'desc'
})

// 时间范围
const createdTimeRange = ref<[string, string]>()

// 分页信息
const pagination = reactive({
  current: 1,
  size: 20,
  total: 0,
  pages: 0
})

// 订单列表和选中项
const orderList = ref<OrderItem[]>([])
const selectedOrders = ref<OrderItem[]>([])

// 状态和支付方式选项
const statusOptions = [
  { value: 1, label: '待支付' },
  { value: 2, label: '已支付' },
  { value: 3, label: '已发货' },
  { value: 4, label: '已完成' },
  { value: 5, label: '已取消' }
]

const paymentOptions = [
  { value: 1, label: '微信支付' },
  { value: 2, label: '支付宝' },
  { value: 3, label: '银行卡' }
]

// 计算属性
const hasData = computed(() => orderList.value.length > 0)

// 监听时间范围变化
watch(createdTimeRange, (newVal) => {
  if (newVal && newVal.length === 2) {
    searchForm.createdTimeStart = newVal[0]
    searchForm.createdTimeEnd = newVal[1]
  } else {
    searchForm.createdTimeStart = undefined
    searchForm.createdTimeEnd = undefined
  }
})

// 工具函数
const getStatusDescription = (status: number): string => {
  const statusMap: Record<number, string> = {
    1: '待支付',
    2: '已支付',
    3: '已发货',
    4: '已完成',
    5: '已取消'
  }
  return statusMap[status] || '未知状态'
}

const getStatusColor = (status: number): string => {
  const colorMap: Record<number, string> = {
    1: 'warning',
    2: 'primary',
    3: 'info',
    4: 'success',
    5: 'danger'
  }
  return colorMap[status] || 'default'
}

const getPaymentMethodDescription = (method: number): string => {
  const methodMap: Record<number, string> = {
    1: '微信支付',
    2: '支付宝',
    3: '银行卡'
  }
  return methodMap[method] || '未知方式'
}

const getNextStatus = (currentStatus: number): number | null => {
  const nextStatusMap: Record<number, number> = {
    1: 2, // 待支付 -> 已支付
    2: 3, // 已支付 -> 已发货
    3: 4  // 已发货 -> 已完成
  }
  return nextStatusMap[currentStatus] || null
}

const getNextStatusDescription = (currentStatus: number): string => {
  const nextStatus = getNextStatus(currentStatus)
  return nextStatus ? getStatusDescription(nextStatus) : ''
}

const handleStatusUpdate = (order: OrderItem) => {
  const nextStatus = getNextStatus(order.status)
  if (nextStatus) {
    updateOrderStatus(order, nextStatus)
  }
}

const getTimelineType = (status: number): string => {
  const typeMap: Record<number, string> = {
    1: 'warning',  // 待支付
    2: 'primary',  // 已支付
    3: 'info',     // 已发货
    4: 'success',  // 已完成
    5: 'danger'    // 已取消
  }
  return typeMap[status] || 'default'
}

const getAvailableStatuses = (currentStatus: number) => {
  // 返回除当前状态外的所有可选状态
  return statusOptions.filter(status => status.value !== currentStatus)
}

// 方法
const toggleSearchExpanded = () => {
  searchExpanded.value = !searchExpanded.value
}

const loadOrderList = async () => {
  try {
    loading.value = true
    
    const params: any = {
      ...searchForm,
      current: pagination.current,
      size: pagination.size
    }
    
    console.log('发送订单查询请求:', params)
    const response: any = await orderApi.getOrderPage(params)
    console.log('订单查询响应:', response)
    
    if (response.code === 200) {
      const pageResult = response.data
      orderList.value = (pageResult.records || []) as OrderItem[]
      pagination.total = pageResult.total || 0
      pagination.pages = pageResult.pages || 0
      console.log('订单列表加载成功:', pageResult)
    } else {
      ElMessage.error(response.message || '获取订单列表失败')
    }
  } catch (error) {
    console.error('加载订单列表失败:', error)
    ElMessage.error('网络错误，请重试')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadOrderList()
}

const handleReset = () => {
  searchFormRef.value?.resetFields()
  Object.assign(searchForm, {
    orderNo: '',
    username: '',
    statusList: [],
    paymentMethodList: [],
    productName: '',
    createdTimeStart: undefined,
    createdTimeEnd: undefined,
    current: 1,
    size: 20,
    sortField: 'createdTime',
    sortOrder: 'desc'
  })
  createdTimeRange.value = undefined
  pagination.current = 1
  loadOrderList()
}

const handleRefresh = () => {
  loadOrderList()
}

const handleExport = async () => {
  try {
    exporting.value = true
    console.log('开始导出订单:', searchForm)
    
    await orderApi.exportOrders(searchForm as any)
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败，请重试')
  } finally {
    exporting.value = false
  }
}

const handleSelectionChange = (selection: OrderItem[]) => {
  selectedOrders.value = selection
}

const handleSortChange = ({ prop, order }: { prop: string; order: string | null }) => {
  if (order) {
    searchForm.sortField = prop
    searchForm.sortOrder = order === 'ascending' ? 'asc' : 'desc'
  } else {
    searchForm.sortField = 'createdTime'
    searchForm.sortOrder = 'desc'
  }
  loadOrderList()
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.current = 1
  loadOrderList()
}

const handleCurrentChange = (current: number) => {
  pagination.current = current
  loadOrderList()
}

// 生成模拟状态流转历史
const generateMockStatusHistory = (currentStatus: number): StatusHistory[] => {
  const history: StatusHistory[] = []
  const baseTime = new Date()
  
  // 根据当前状态生成历史记录
  for (let status = 1; status <= currentStatus; status++) {
    const time = new Date(baseTime.getTime() - (currentStatus - status + 1) * 24 * 60 * 60 * 1000)
    history.push({
      id: status,
      status: status,
      operator: status === 1 ? '系统' : `操作员${status}`,
      remark: getStatusHistoryRemark(status),
      createdTime: time.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      })
    })
  }
  
  return history.reverse() // 最新的在前面
}

const getStatusHistoryRemark = (status: number): string => {
  const remarks: Record<number, string> = {
    1: '订单创建，等待用户支付',
    2: '用户完成支付，订单进入处理流程',
    3: '商品已发货，物流信息已更新',
    4: '用户确认收货，订单完成',
    5: '订单已取消'
  }
  return remarks[status] || ''
}

const showOrderDetail = async (order: OrderItem) => {
  try {
    detailLoading.value = true
    console.log('获取订单详情:', order.id)
    
    const response: any = await orderApi.getOrderDetail(order.id)
    
    if (response.code === 200) {
      const orderDetail = response.data
      
      // 添加模拟状态流转历史
      orderDetail.statusHistory = generateMockStatusHistory(orderDetail.status)
      
      selectedOrder.value = orderDetail
      orderDetailVisible.value = true
    } else {
      ElMessage.error(response.message || '获取订单详情失败')
    }
  } catch (error) {
    console.error('获取订单详情失败:', error)
    ElMessage.error('网络错误，请重试')
  } finally {
    detailLoading.value = false
  }
}

const refreshOrderDetail = async () => {
  if (selectedOrder.value) {
    await showOrderDetail(selectedOrder.value)
  }
}

const updateOrderStatus = async (order: OrderItem, newStatus: number) => {
  try {
    // 设置单个订单的更新状态
    order.updating = true
    console.log('更新订单状态:', { orderId: order.id, newStatus, currentStatus: order.status })
    
    const response: any = await orderApi.updateOrderStatus(order.id, newStatus)
    
    if (response.code === 200) {
      ElMessage.success(`状态更新成功：${getStatusDescription(order.status)} → ${getStatusDescription(newStatus)}`)
      // 更新本地订单状态，避免重新加载整个列表
      order.status = newStatus
      loadOrderList()
      if (selectedOrder.value && selectedOrder.value.id === order.id) {
        await refreshOrderDetail()
      }
    } else {
      ElMessage.error(response.message || '状态更新失败')
    }
  } catch (error) {
    console.error('更新状态失败:', error)
    ElMessage.error('网络错误，请重试')
  } finally {
    order.updating = false
  }
}

const handleSyncToES = async (order: OrderItem) => {
  try {
    // 设置单个订单的同步状态
    order.syncing = true
    console.log('同步订单到ES:', order.id)
    
    const response: any = await orderApi.syncOrderToES(order.id)
    
    if (response.code === 200) {
      ElMessage.success('同步ES成功')
    } else {
      ElMessage.error(response.message || '同步ES失败')
    }
  } catch (error) {
    console.error('同步ES失败:', error)
    ElMessage.error('网络错误，请重试')
  } finally {
    order.syncing = false
  }
}

const handleBatchSyncToES = async () => {
  if (!selectedOrders.value.length) {
    ElMessage.warning('请选择要同步的订单')
    return
  }
  
  try {
    batchSyncing.value = true
    const orderIds = selectedOrders.value.map(order => order.id)
    console.log('批量同步订单到ES:', orderIds)
    
    const response: any = await orderApi.batchSyncOrdersToES(orderIds)
    
    if (response.code === 200) {
      ElMessage.success(`批量同步ES成功，共同步 ${orderIds.length} 条订单`)
      // 清空选择
      tableRef.value?.clearSelection()
    } else {
      ElMessage.error(response.message || '批量同步ES失败')
    }
  } catch (error) {
    console.error('批量同步ES失败:', error)
    ElMessage.error('网络错误，请重试')
  } finally {
    batchSyncing.value = false
  }
}

const showStatusHistory = async (order: OrderItem) => {
  try {
    historyLoading.value = true
    currentOrderId.value = order.id
    currentOrderNo.value = order.orderNo
    console.log('获取订单状态历史:', order.id)
    
    // 使用模拟数据，因为API中没有状态历史接口
    const response: any = { code: 200, data: generateMockStatusHistory(order.status || 1) }
    
    if (response.code === 200) {
      statusHistoryList.value = response.data || []
      statusHistoryVisible.value = true
    } else {
      ElMessage.error(response.message || '获取状态历史失败')
    }
  } catch (error) {
    console.error('获取状态历史失败:', error)
    ElMessage.error('网络错误，请重试')
  } finally {
    historyLoading.value = false
  }
}

const refreshStatusHistory = async () => {
  if (currentOrderId.value) {
    const order = { id: currentOrderId.value, orderNo: currentOrderNo.value } as OrderItem
    await showStatusHistory(order)
  }
}

// 生命周期
onMounted(() => {
  loadOrderList()
})
</script>

<style scoped lang="scss">
.order-list-container {
  .search-card, .action-card, .table-card {
    margin-bottom: 16px;
    
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      .card-title {
        font-weight: 600;
        font-size: 16px;
      }
    }
  }
  
  .search-form {
    .advanced-search {
      border-top: 1px solid #f0f0f0;
      padding-top: 20px;
      margin-top: 10px;
    }
  }
  
  .action-buttons {
    display: flex;
    gap: 12px;
    flex-wrap: wrap;
  }
  
  .action-info {
    margin-top: 12px;
    color: #666;
    font-size: 14px;
  }
  
  .order-no-link {
    font-family: 'Courier New', monospace;
    font-weight: 600;
  }
  
  .amount {
    color: #f56c6c;
    font-weight: 600;
  }
  
  .pagination-container {
    display: flex;
    justify-content: center;
    margin-top: 20px;
  }
  
  .status-history-item {
    .status-info {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 4px;
      
      .operator {
        color: #666;
        font-size: 12px;
      }
    }
    
    .status-remark {
      color: #999;
      font-size: 12px;
      line-height: 1.4;
    }
  }
}

// 订单详情抽屉样式
.order-detail-content {
  padding: 0 4px;
  height: 100%;
  display: flex;
  flex-direction: column;
  
  .detail-section {
    margin-bottom: 24px;
    
    .section-title {
      margin: 0 0 16px 0;
      font-size: 16px;
      font-weight: 600;
      color: #303133;
      border-bottom: 2px solid #409eff;
      padding-bottom: 8px;
    }
  }
  
  .order-no {
    font-family: 'Courier New', monospace;
    font-weight: 600;
    color: #409eff;
  }
  
    .detail-actions {
    margin-top: auto;
    padding: 16px 0;
    border-top: 1px solid #ebeef5;
    
    .action-row {
      display: flex;
      gap: 12px;
      flex-wrap: wrap;
      margin-bottom: 12px;
      
      .el-button {
        flex: 1;
        min-width: 80px;
      }
    }
    
    .drawer-status-update {
      margin-top: 8px;
      padding: 8px 0;
      border-top: 1px solid #f0f0f0;
      
      .update-label {
        font-size: 12px;
        color: #909399;
        margin-bottom: 4px;
      }
      
      .next-status-item {
        display: inline-block;
        padding: 4px 8px;
        background: #f0f9ff;
        border: 1px solid #409eff;
        border-radius: 4px;
        color: #409eff;
        font-size: 12px;
        cursor: pointer;
        transition: all 0.3s;
        
        &:hover {
          background: #409eff;
          color: white;
        }
        
        &.updating {
          background: #f5f7fa;
          color: #909399;
          cursor: not-allowed;
          
          .loading-icon {
            animation: rotate 1s linear infinite;
          }
        }
      }
    }
  }
}

// 表格操作列样式
.action-buttons {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
  margin-bottom: 4px;
  
  .el-button {
    margin: 0;
  }
}

.status-update-area {
  margin-top: 4px;
  
  .update-text {
    font-size: 11px;
    color: #909399;
    margin-bottom: 2px;
  }
  
  .next-status {
    display: inline-block;
    padding: 2px 6px;
    background: #f0f9ff;
    border: 1px solid #409eff;
    border-radius: 3px;
    color: #409eff;
    font-size: 11px;
    cursor: pointer;
    transition: all 0.3s;
    
    &:hover {
      background: #409eff;
      color: white;
    }
    
    &.updating {
      background: #f5f7fa;
      color: #909399;
      cursor: not-allowed;
      
      .loading-icon {
        animation: rotate 1s linear infinite;
        font-size: 10px;
      }
    }
  }
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style> 
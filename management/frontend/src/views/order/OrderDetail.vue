<template>
  <el-drawer
    v-model="visible"
    title="订单详情"
    :size="800"
    direction="rtl"
    class="order-detail-drawer"
    @closed="handleClose"
  >
    <div v-loading="loading" class="order-detail-container">
      <div v-if="orderData" class="order-detail-content">
        <!-- 订单基本信息 -->
        <el-card class="info-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span class="card-title">订单基本信息</span>
              <div class="order-status">
                <el-tag 
                  :type="orderUtils.getStatusColor(orderData.status)" 
                  size="large"
                >
                  {{ orderUtils.getStatusDescription(orderData.status) }}
                </el-tag>
              </div>
            </div>
          </template>

          <el-row :gutter="20">
            <el-col :span="12">
              <div class="info-item">
                <label>订单号：</label>
                <span class="order-no">{{ orderData.orderNo }}</span>
                <el-button 
                  type="text" 
                  size="small" 
                  @click="copyToClipboard(orderData.orderNo)"
                  class="copy-btn"
                >
                  复制
                </el-button>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="info-item">
                <label>用户名：</label>
                <span>{{ orderData.username || '-' }}</span>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="info-item">
                <label>订单金额：</label>
                <span class="amount">¥{{ orderData.totalAmount.toFixed(2) }}</span>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="info-item">
                <label>支付方式：</label>
                <span>{{ orderUtils.getPaymentMethodDescription(orderData.paymentMethod) }}</span>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="info-item">
                <label>创建时间：</label>
                <span>{{ orderData.createdTime }}</span>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="info-item">
                <label>更新时间：</label>
                <span>{{ orderData.updatedTime }}</span>
              </div>
            </el-col>
            <el-col :span="24">
              <div class="info-item">
                <label>备注：</label>
                <span>{{ orderData.remark || '无' }}</span>
              </div>
            </el-col>
          </el-row>
        </el-card>

        <!-- 订单明细信息 -->
        <el-card class="items-card" shadow="never">
          <template #header>
            <span class="card-title">订单明细</span>
          </template>

          <el-table 
            :data="orderData.orderItems" 
            style="width: 100%"
            stripe
          >
            <el-table-column prop="productName" label="商品名称" />
            <el-table-column prop="productPrice" label="单价" width="120">
              <template #default="{ row }">
                ¥{{ row.productPrice.toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column prop="quantity" label="数量" width="80" />
            <el-table-column prop="totalPrice" label="小计" width="120">
              <template #default="{ row }">
                <span class="amount">¥{{ row.totalPrice.toFixed(2) }}</span>
              </template>
            </el-table-column>
          </el-table>

          <div class="order-summary">
            <div class="summary-item">
              <span class="label">商品总数：</span>
              <span class="value">{{ getTotalQuantity() }} 件</span>
            </div>
            <div class="summary-item">
              <span class="label">订单总额：</span>
              <span class="value amount">¥{{ orderData.totalAmount.toFixed(2) }}</span>
            </div>
          </div>
        </el-card>

        <!-- 状态操作 -->
        <el-card class="actions-card" shadow="never">
          <template #header>
            <span class="card-title">状态操作</span>
          </template>

          <div class="status-actions">
            <el-button-group>
                           <el-button
               v-for="status in getAvailableStatuses"
               :key="status.value"
               :type="getStatusButtonType(status.value)"
               @click="updateOrderStatus(status.value)"
               :disabled="statusUpdating"
             >
                {{ status.label }}
              </el-button>
            </el-button-group>
          </div>

          <div class="other-actions">
            <el-button 
              type="info" 
              :icon="Refresh"
              @click="syncToES"
              :loading="syncing"
            >
              同步到ES
            </el-button>
            <el-button 
              type="primary" 
              :icon="RefreshLeft"
              @click="refreshOrderDetail"
              :loading="loading"
            >
              刷新数据
            </el-button>
          </div>
        </el-card>

        <!-- 状态流转历史 -->
        <el-card class="history-card" shadow="never">
          <template #header>
            <span class="card-title">状态流转历史</span>
          </template>

          <el-timeline>
            <el-timeline-item
              v-for="(item, index) in statusHistory"
              :key="index"
              :timestamp="item.timestamp"
              :type="getTimelineType(item.status)"
            >
              <div class="timeline-content">
                <div class="status-change">
                  订单状态变更为：
                  <el-tag :type="orderUtils.getStatusColor(item.status)" size="small">
                    {{ orderUtils.getStatusDescription(item.status) }}
                  </el-tag>
                </div>
                <div class="operator" v-if="item.operator">
                  操作人：{{ item.operator }}
                </div>
                <div class="remark" v-if="item.remark">
                  备注：{{ item.remark }}
                </div>
              </div>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </div>

      <!-- 空数据状态 -->
      <el-empty v-else description="暂无订单数据" />
    </div>
  </el-drawer>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, RefreshLeft } from '@element-plus/icons-vue'
import { 
  orderApi, 
  type OrderManageDTO,
  OrderStatusOptions,
  orderUtils
} from '../../api/orderApi'
import type { ApiResponse } from '../../types'

// Props
interface Props {
  visible: boolean
  orderId?: number
}

const props = withDefaults(defineProps<Props>(), {
  visible: false
})

// Emits
const emit = defineEmits<{
  'update:visible': [value: boolean]
  'refresh': []
}>()

// 响应式数据
const loading = ref(false)
const statusUpdating = ref(false)
const syncing = ref(false)
const orderData = ref<OrderManageDTO>()

// 模拟状态历史数据
const statusHistory = ref([
  {
    status: 1,
    timestamp: '2024-01-01 10:00:00',
    operator: '系统',
    remark: '订单创建'
  },
  {
    status: 2,
    timestamp: '2024-01-01 10:30:00',
    operator: '用户',
    remark: '支付完成'
  }
])

// 计算属性
const visible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value)
})

const getAvailableStatuses = computed(() => {
  if (!orderData.value) return []
  return OrderStatusOptions.filter(option => 
    orderUtils.canUpdateStatus(orderData.value!.status, option.value)
  )
})

// 方法
const loadOrderDetail = async () => {
  if (!props.orderId) return

  try {
    loading.value = true
    const response = await orderApi.getOrderDetail(props.orderId)
    console.log('订单详情响应:', response)
    // response本身就是ApiResponse<OrderManageDTO>类型，所以response.data是OrderManageDTO
    orderData.value = (response as any).data
  } catch (error) {
    console.error('获取订单详情失败:', error)
    ElMessage.error('获取订单详情失败')
  } finally {
    loading.value = false
  }
}

const refreshOrderDetail = () => {
  loadOrderDetail()
}

const getTotalQuantity = () => {
  if (!orderData.value?.orderItems) return 0
  return orderData.value.orderItems.reduce((total, item) => total + item.quantity, 0)
}

const getStatusButtonType = (status: number) => {
  const typeMap: Record<number, string> = {
    1: 'warning', // 待支付
    2: 'primary', // 已支付
    3: 'info',    // 已发货
    4: 'success', // 已完成
    5: 'danger'   // 已取消
  }
  return typeMap[status] || 'default'
}

const getTimelineType = (status: number) => {
  const typeMap: Record<number, string> = {
    1: 'warning',
    2: 'primary',
    3: 'info',
    4: 'success',
    5: 'danger'
  }
  return typeMap[status] || 'primary'
}

const updateOrderStatus = async (status: number) => {
  if (!orderData.value) return

  try {
    await ElMessageBox.confirm(
      `确定要将订单状态更新为 ${orderUtils.getStatusDescription(status)} 吗？`,
      '确认操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    statusUpdating.value = true
    const response = await orderApi.updateOrderStatus(orderData.value.id, status)
    // 响应拦截器已经处理了错误情况，能到这里说明成功
    ElMessage.success('状态更新成功')
    // 添加到状态历史
    statusHistory.value.push({
      status,
      timestamp: new Date().toLocaleString(),
      operator: '管理员',
      remark: '手动更新'
    })
    // 重新加载订单数据
    await loadOrderDetail()
    // 通知父组件刷新
    emit('refresh')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('更新订单状态失败:', error)
      ElMessage.error('状态更新失败')
    }
  } finally {
    statusUpdating.value = false
  }
}

const syncToES = async () => {
  if (!orderData.value) return

  try {
    syncing.value = true
    const response = await orderApi.syncOrderToES(orderData.value.id)
    // 响应拦截器已经处理了错误情况，能到这里说明成功
    ElMessage.success('同步ES成功')
  } catch (error) {
    console.error('同步ES失败:', error)
    ElMessage.error('同步ES失败')
  } finally {
    syncing.value = false
  }
}

const copyToClipboard = async (text: string) => {
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success('复制成功')
  } catch (error) {
    console.error('复制失败:', error)
    ElMessage.error('复制失败')
  }
}

const handleClose = () => {
  orderData.value = undefined
}

// 监听器
watch(() => props.visible, (newVal) => {
  if (newVal && props.orderId) {
    loadOrderDetail()
  }
})

watch(() => props.orderId, (newVal) => {
  if (newVal && props.visible) {
    loadOrderDetail()
  }
})
</script>

<style scoped lang="scss">
.order-detail-drawer {
  :deep(.el-drawer__header) {
    padding: 20px 20px 0 20px;
    margin-bottom: 0;
  }

  :deep(.el-drawer__body) {
    padding: 20px;
  }
}

.order-detail-container {
  height: 100%;

  .order-detail-content {
    .info-card,
    .items-card,
    .actions-card,
    .history-card {
      margin-bottom: 20px;

      &:last-child {
        margin-bottom: 0;
      }
    }

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .card-title {
        font-weight: 600;
        font-size: 16px;
      }
    }

    .card-title {
      font-weight: 600;
      font-size: 16px;
    }

    .info-item {
      display: flex;
      align-items: center;
      margin-bottom: 16px;
      min-height: 24px;

      label {
        min-width: 80px;
        color: #606266;
        font-weight: 500;
      }

      .order-no {
        font-family: 'Courier New', monospace;
        font-weight: 600;
        color: #409EFF;
      }

      .copy-btn {
        margin-left: 8px;
        padding: 0;
        font-size: 12px;
      }

      .amount {
        color: #F56C6C;
        font-weight: 600;
        font-size: 16px;
      }
    }

    .order-summary {
      margin-top: 16px;
      padding: 16px;
      background: #f5f7fa;
      border-radius: 4px;

      .summary-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 8px;

        &:last-child {
          margin-bottom: 0;
        }

        .label {
          color: #606266;
        }

        .value {
          font-weight: 600;

          &.amount {
            color: #F56C6C;
            font-size: 18px;
          }
        }
      }
    }

    .status-actions {
      margin-bottom: 20px;
    }

    .other-actions {
      display: flex;
      gap: 12px;
    }

    .timeline-content {
      .status-change {
        font-weight: 500;
        margin-bottom: 4px;
      }

      .operator,
      .remark {
        font-size: 12px;
        color: #909399;
        margin-bottom: 2px;
      }
    }
  }
}

.amount {
  color: #F56C6C;
  font-weight: 600;
}
</style>

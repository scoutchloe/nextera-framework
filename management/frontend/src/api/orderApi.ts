import request from './request'
import type { ApiResponse } from '@/types'

// 订单搜索条件接口
export interface OrderSearchDTO {
  orderNo?: string
  userId?: number
  username?: string
  statusList?: number[]
  paymentMethodList?: number[]
  minAmount?: number
  maxAmount?: number
  createdTimeStart?: string
  createdTimeEnd?: string
  updatedTimeStart?: string
  updatedTimeEnd?: string
  productName?: string
  productId?: number
  current?: number
  size?: number
  sortField?: string
  sortOrder?: string
}

// 订单明细接口
export interface OrderItemDTO {
  id: number
  orderId: number
  productId: number
  productName: string
  productPrice: number
  quantity: number
  totalPrice: number
  createdTime: string
}

// 订单管理DTO接口
export interface OrderManageDTO {
  id: number
  orderNo: string
  userId: number
  username?: string
  totalAmount: number
  status: number
  statusDescription?: string
  paymentMethod: number
  paymentMethodDescription?: string
  remark?: string
  createdTime: string
  updatedTime: string
  orderItems?: OrderItemDTO[]
}

// 分页结果接口
export interface PageResult<T> {
  records: T[]
  total: number
  current: number
  size: number
  pages: number
}

// 订单管理相关API
export const orderApi = {
  // 分页查询订单
  getOrderPage: (searchDTO: OrderSearchDTO) => 
    request.post<ApiResponse<PageResult<OrderManageDTO>>>('/order/page', searchDTO),

  // 根据ID获取订单详情
  getOrderDetail: (id: number) => 
    request.get<ApiResponse<OrderManageDTO>>(`/order/${id}`),

  // 根据订单号获取订单详情
  getOrderDetailByOrderNo: (orderNo: string) => 
    request.get<ApiResponse<OrderManageDTO>>(`/order/orderNo/${orderNo}`),

  // 更新订单状态
  updateOrderStatus: (id: number, status: number) => 
    request.put<ApiResponse<boolean>>(`/order/${id}/status?status=${status}`),

  // 批量更新订单状态
  batchUpdateOrderStatus: (orderIds: number[], status: number) => 
    request.put<ApiResponse<boolean>>(`/order/batch/status?orderIds=${orderIds.join(',')}&status=${status}`),

  // 高级搜索订单
  advancedSearch: (searchDTO: OrderSearchDTO) => 
    request.post<ApiResponse<OrderManageDTO[]>>('/order/search', searchDTO),

  // 导出订单数据
  exportOrders: (searchDTO: OrderSearchDTO) => {
    // 生成文件名
    const now = new Date()
    const timestamp = now.getFullYear() + 
      String(now.getMonth() + 1).padStart(2, '0') + 
      String(now.getDate()).padStart(2, '0') + '_' +
      String(now.getHours()).padStart(2, '0') + 
      String(now.getMinutes()).padStart(2, '0') + 
      String(now.getSeconds()).padStart(2, '0')
    const filename = `订单导出_${timestamp}.xlsx`
    
    return request.download('/order/export', filename, {
      method: 'POST',
      data: searchDTO
    })
  },

  // 获取订单状态分布
  getOrderStatusDistribution: () => 
    request.get<ApiResponse<OrderManageDTO[]>>('/order/status/distribution'),

  // 同步订单到ES
  syncOrderToES: (id: number) => 
    request.post<ApiResponse<boolean>>(`/order/${id}/sync`),

  // 批量同步订单到ES
  batchSyncOrdersToES: (orderIds: number[]) => 
    request.post<ApiResponse<boolean>>(`/order/batch/sync?orderIds=${orderIds.join(',')}`)
}

// 订单状态枚举
export const OrderStatus = {
  PENDING_PAYMENT: 1,
  PAID: 2,
  SHIPPED: 3,
  COMPLETED: 4,
  CANCELLED: 5
} as const

// 订单状态描述映射
export const OrderStatusMap = {
  [OrderStatus.PENDING_PAYMENT]: '待支付',
  [OrderStatus.PAID]: '已支付',
  [OrderStatus.SHIPPED]: '已发货',
  [OrderStatus.COMPLETED]: '已完成',
  [OrderStatus.CANCELLED]: '已取消'
}

// 支付方式枚举
export const PaymentMethod = {
  WECHAT: 1,
  ALIPAY: 2,
  BANK_CARD: 3
} as const

// 支付方式描述映射
export const PaymentMethodMap = {
  [PaymentMethod.WECHAT]: '微信支付',
  [PaymentMethod.ALIPAY]: '支付宝',
  [PaymentMethod.BANK_CARD]: '银行卡'
}

// 订单状态选项（用于下拉框）
export const OrderStatusOptions = Object.entries(OrderStatusMap).map(([value, label]) => ({
  value: Number(value),
  label
}))

// 支付方式选项（用于下拉框）
export const PaymentMethodOptions = Object.entries(PaymentMethodMap).map(([value, label]) => ({
  value: Number(value),
  label
}))

// 工具函数
export const orderUtils = {
  // 获取订单状态描述
  getStatusDescription: (status: number): string => {
    return OrderStatusMap[status as keyof typeof OrderStatusMap] || '未知状态'
  },

  // 获取支付方式描述
  getPaymentMethodDescription: (method: number): string => {
    return PaymentMethodMap[method as keyof typeof PaymentMethodMap] || '未知方式'
  },

  // 判断订单是否可以更新状态
  canUpdateStatus: (currentStatus: number, targetStatus: number): boolean => {
    // 已取消和已完成的订单不能再更新状态
    if (currentStatus === OrderStatus.CANCELLED || currentStatus === OrderStatus.COMPLETED) {
      return false
    }
    
    // 状态流转规则
    const allowedTransitions: Record<number, number[]> = {
      [OrderStatus.PENDING_PAYMENT]: [OrderStatus.PAID, OrderStatus.CANCELLED],
      [OrderStatus.PAID]: [OrderStatus.SHIPPED, OrderStatus.CANCELLED],
      [OrderStatus.SHIPPED]: [OrderStatus.COMPLETED]
    }
    
    return allowedTransitions[currentStatus]?.includes(targetStatus) || false
  },

  // 获取订单状态对应的颜色
  getStatusColor: (status: number): string => {
    const colorMap: Record<number, string> = {
      [OrderStatus.PENDING_PAYMENT]: 'warning',
      [OrderStatus.PAID]: 'primary',
      [OrderStatus.SHIPPED]: 'info',
      [OrderStatus.COMPLETED]: 'success',
      [OrderStatus.CANCELLED]: 'danger'
    }
    
    return colorMap[status] || 'default'
  },

  // 格式化金额
  formatAmount: (amount: number): string => {
    return `¥${amount.toFixed(2)}`
  },

  // 格式化订单号
  formatOrderNo: (orderNo: string): string => {
    // 订单号脱敏，只显示前4位和后4位
    if (orderNo.length > 8) {
      return `${orderNo.substring(0, 4)}****${orderNo.substring(orderNo.length - 4)}`
    }
    return orderNo
  }
} 
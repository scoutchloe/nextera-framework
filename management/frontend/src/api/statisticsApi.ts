import request from './request'
import type { ApiResponse } from '@/types'

// 概览统计接口
export interface OverviewStatistics {
  todayOrderCount: number
  todayOrderAmount: number
  monthOrderCount: number
  monthOrderAmount: number
  totalOrderCount: number
  totalOrderAmount: number
  pendingOrderCount: number
  paidOrderCount: number
  completedOrderCount: number
  cancelledOrderCount: number
  statisticsTime: string
}

// 实时统计接口
export interface RealtimeStatistics {
  lastHourOrderCount: number
  last24HourOrderCount: number
  last7DayOrderCount: number
  last30DayOrderCount: number
  lastHourOrderAmount: number
  last24HourOrderAmount: number
  last7DayOrderAmount: number
  last30DayOrderAmount: number
  statisticsTime: string
}

// 趋势统计接口
export interface TrendStatistics {
  date: string
  orderCount: number
  orderAmount: number
  newUserCount: number
}

// 状态分布统计接口
export interface StatusDistribution {
  status: number
  statusDescription: string
  count: number
  percentage: number
}

// 商品销售排行接口
export interface ProductSalesRanking {
  productId: number
  productName: string
  salesCount: number
  salesAmount: number
  rank: number
}

// 用户订单分析接口
export interface UserOrderAnalysis {
  userId: number
  username: string
  orderCount: number
  totalAmount: number
  avgOrderAmount: number
  lastOrderTime?: string
}

// 订单统计相关API
export const statisticsApi = {
  // 获取概览统计（Dashboard用）
  getOverviewStatistics: () => 
    request.get<ApiResponse<OverviewStatistics>>('/statistics/overview'),

  // 获取实时统计
  getRealtimeStatistics: () => 
    request.get<ApiResponse<RealtimeStatistics>>('/statistics/realtime'),

  // 获取订单趋势
  getOrderTrend: (startDate: string, endDate: string) => 
    request.get<ApiResponse<TrendStatistics[]>>('/statistics/trend', {
      params: { startDate, endDate }
    }),

  // 获取订单状态分布
  getOrderStatusDistribution: (startDate: string, endDate: string) => 
    request.get<ApiResponse<StatusDistribution[]>>('/statistics/status', {
      params: { startDate, endDate }
    }),

  // 获取商品销售排行榜
  getProductSalesRanking: (startDate: string, endDate: string, limit: number = 10) => 
    request.get<ApiResponse<ProductSalesRanking[]>>('/statistics/products', {
      params: { startDate, endDate, limit }
    }),

  // 获取用户订单分析
  getUserOrderAnalysis: (startDate: string, endDate: string, limit: number = 10) => 
    request.get<ApiResponse<UserOrderAnalysis[]>>('/statistics/users', {
      params: { startDate, endDate, limit }
    }),

  // 获取订单金额分布
  getOrderAmountDistribution: (startDate: string, endDate: string) => 
    request.get<ApiResponse<Record<string, number>>>('/statistics/amount', {
      params: { startDate, endDate }
    }),

  // 刷新统计缓存
  refreshStatisticsCache: () => 
    request.post<ApiResponse<boolean>>('/statistics/cache/refresh')
}

// 图表配置工具函数
export const chartUtils = {
  // 生成趋势图表配置
  generateTrendChartOption: (data: TrendStatistics[]) => {
    return {
      title: {
        text: '订单趋势分析',
        left: 'center'
      },
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'cross'
        }
      },
      legend: {
        data: ['订单数量', '订单金额'],
        top: 30
      },
      xAxis: {
        type: 'category',
        data: data.map(item => item.date)
      },
      yAxis: [
        {
          type: 'value',
          name: '订单数量',
          position: 'left'
        },
        {
          type: 'value',
          name: '订单金额',
          position: 'right'
        }
      ],
      series: [
        {
          name: '订单数量',
          type: 'line',
          data: data.map(item => item.orderCount),
          smooth: true
        },
        {
          name: '订单金额',
          type: 'bar',
          yAxisIndex: 1,
          data: data.map(item => item.orderAmount)
        }
      ]
    }
  },

  // 生成状态分布饼图配置
  generateStatusPieChartOption: (data: StatusDistribution[]) => {
    return {
      title: {
        text: '订单状态分布',
        left: 'center'
      },
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b}: {c} ({d}%)'
      },
      legend: {
        orient: 'vertical',
        left: 'left',
        data: data.map(item => item.statusDescription)
      },
      series: [
        {
          name: '订单状态',
          type: 'pie',
          radius: '50%',
          data: data.map(item => ({
            value: item.count,
            name: item.statusDescription
          })),
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          }
        }
      ]
    }
  },

  // 生成销售排行柱状图配置
  generateSalesRankingChartOption: (data: ProductSalesRanking[]) => {
    return {
      title: {
        text: '商品销售排行榜',
        left: 'center'
      },
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'shadow'
        }
      },
      xAxis: {
        type: 'category',
        data: data.map(item => item.productName),
        axisLabel: {
          rotate: 45,
          interval: 0
        }
      },
      yAxis: [
        {
          type: 'value',
          name: '销售数量'
        },
        {
          type: 'value',
          name: '销售金额'
        }
      ],
      series: [
        {
          name: '销售数量',
          type: 'bar',
          data: data.map(item => item.salesCount)
        },
        {
          name: '销售金额',
          type: 'line',
          yAxisIndex: 1,
          data: data.map(item => item.salesAmount)
        }
      ]
    }
  }
}

// 统计数据格式化工具
export const statisticsUtils = {
  // 格式化数量
  formatCount: (count: number): string => {
    if (count >= 10000) {
      return `${(count / 10000).toFixed(1)}万`
    } else if (count >= 1000) {
      return `${(count / 1000).toFixed(1)}k`
    }
    return count.toString()
  },

  // 格式化金额
  formatAmount: (amount: number): string => {
    if (amount >= 100000000) {
      return `¥${(amount / 100000000).toFixed(2)}亿`
    } else if (amount >= 10000) {
      return `¥${(amount / 10000).toFixed(2)}万`
    }
    return `¥${amount.toFixed(2)}`
  },

  // 格式化百分比
  formatPercentage: (value: number): string => {
    return `${value.toFixed(1)}%`
  },

  // 计算增长率
  calculateGrowthRate: (current: number, previous: number): number => {
    if (previous === 0) return current > 0 ? 100 : 0
    return ((current - previous) / previous) * 100
  },

  // 获取增长率颜色
  getGrowthRateColor: (rate: number): string => {
    if (rate > 0) return 'success'
    if (rate < 0) return 'danger'
    return 'info'
  },

  // 获取增长率图标
  getGrowthRateIcon: (rate: number): string => {
    if (rate > 0) return 'arrow-up'
    if (rate < 0) return 'arrow-down'
    return 'minus'
  }
} 
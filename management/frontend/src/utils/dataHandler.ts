/**
 * 数据处理工具函数
 * 用于处理系统管理模块的API响应数据
 */

/**
 * 处理API响应数据，统一数据格式
 * @param response API响应对象
 * @param defaultData 默认数据（fallback用）
 * @returns 处理后的数据对象
 */
export function processApiResponse(response: any, defaultData = []) {
  console.log('=== 数据处理开始 ===')
  console.log('原始响应:', response)
  
  // 检查响应是否成功
  const isSuccess = response.code === 200 || 
                   response.code === '200' || 
                   response.success === true ||
                   response.status === 200 ||
                   response.status === '200'
  
  console.log('响应是否成功:', isSuccess)
  
  if (!isSuccess) {
    console.log('响应失败，使用默认数据')
    return {
      success: false,
      data: defaultData,
      total: Array.isArray(defaultData) ? defaultData.length : 0,
      message: response.message || response.msg || '请求失败'
    }
  }
  
  // 处理响应数据
  let dataList = []
  let total = 0
  
  const data = response.data || response.result || response
  console.log('提取的数据部分:', data)
  
  if (!data) {
    console.log('无数据部分，使用默认数据')
    return {
      success: true,
      data: defaultData,
      total: Array.isArray(defaultData) ? defaultData.length : 0,
      message: '数据为空'
    }
  }
  
  // 处理分页数据结构
  if (data.records && Array.isArray(data.records)) {
    console.log('检测到分页结构（records）')
    dataList = data.records
    total = data.total || data.size || data.records.length
  } else if (data.list && Array.isArray(data.list)) {
    console.log('检测到分页结构（list）')
    dataList = data.list
    total = data.total || data.size || data.list.length
  } else if (data.content && Array.isArray(data.content)) {
    console.log('检测到分页结构（content）')
    dataList = data.content
    total = data.totalElements || data.total || data.content.length
  } else if (Array.isArray(data)) {
    console.log('检测到数组结构')
    dataList = data
    total = data.length
  } else {
    console.log('未识别的数据结构，使用默认数据')
    console.warn('未识别的数据结构:', data)
    dataList = defaultData
    total = Array.isArray(defaultData) ? defaultData.length : 0
  }
  
  console.log('处理后的数据列表:', dataList)
  console.log('数据总数:', total)
  console.log('=== 数据处理结束 ===')
  
  return {
    success: true,
    data: dataList,
    total: total,
    message: '数据加载成功'
  }
}

/**
 * 处理表格数据展示
 * @param data 数据列表
 * @param total 总数
 * @param itemName 项目名称（用于消息提示）
 */
export function handleTableData(data: any[], total: number, itemName = '数据') {
  if (data.length > 0) {
    console.log(`${itemName}列表加载成功:`, data)
    return {
      showMessage: true,
      messageType: 'success',
      message: `${itemName}列表加载成功，共${total}条数据`
    }
  } else {
    console.log(`暂无${itemName}数据`)
    return {
      showMessage: true,
      messageType: 'info',
      message: `暂无${itemName}数据`
    }
  }
}

/**
 * 调试日志输出
 * @param label 标签
 * @param data 数据
 */
export function debugLog(label: string, data: any) {
  console.group(`🔍 ${label}`)
  console.log('数据类型:', typeof data)
  console.log('是否为数组:', Array.isArray(data))
  console.log('数据内容:', data)
  if (Array.isArray(data)) {
    console.log('数组长度:', data.length)
    if (data.length > 0) {
      console.log('第一个元素:', data[0])
    }
  }
  console.groupEnd()
} 
// 权限测试工具
import axios from 'axios'

export interface PermissionTestResult {
  success: boolean
  message: string
  data?: any
}

/**
 * 获取所有权限编码
 */
export async function getAllPermissions(): Promise<PermissionTestResult> {
  try {
    const response = await axios.get('/api/system/permission/tree')
    if (response.data?.code === 200) {
      return {
        success: true,
        message: '获取权限成功',
        data: response.data.data
      }
    } else {
      return {
        success: false,
        message: response.data?.message || '获取权限失败'
      }
    }
  } catch (error: any) {
    return {
      success: false,
      message: error.response?.data?.message || error.message || '网络错误'
    }
  }
}

/**
 * 获取当前用户权限
 */
export async function getCurrentUserPermissions(): Promise<PermissionTestResult> {
  try {
    const response = await axios.get('/api/auth/permissions')
    if (response.data?.code === 200) {
      return {
        success: true,
        message: '获取用户权限成功',
        data: response.data.data
      }
    } else {
      return {
        success: false,
        message: response.data?.message || '获取用户权限失败'
      }
    }
  } catch (error: any) {
    return {
      success: false,
      message: error.response?.data?.message || error.message || '网络错误'
    }
  }
}

/**
 * 检查ContentAdmin权限状态
 */
export async function checkContentAdminPermissions(): Promise<PermissionTestResult> {
  try {
    const response = await axios.get('/api/debug/check-content-admin')
    if (response.data?.code === 200) {
      return {
        success: true,
        message: '检查ContentAdmin权限完成',
        data: response.data.data
      }
    } else {
      return {
        success: false,
        message: response.data?.message || '检查ContentAdmin权限失败'
      }
    }
  } catch (error: any) {
    return {
      success: false,
      message: error.response?.data?.message || error.message || '网络错误'
    }
  }
}

/**
 * 修复ContentAdmin权限
 */
export async function fixContentAdminPermissions(): Promise<PermissionTestResult> {
  try {
    const response = await axios.post('/api/debug/fix-content-admin')
    if (response.data?.code === 200) {
      return {
        success: true,
        message: '权限修复成功',
        data: response.data.data
      }
    } else {
      return {
        success: false,
        message: response.data?.message || '权限修复失败'
      }
    }
  } catch (error: any) {
    return {
      success: false,
      message: error.response?.data?.message || error.message || '网络错误'
    }
  }
}

/**
 * 扁平化权限树
 */
export function flattenPermissions(permissions: any[]): any[] {
  const result: any[] = []
  
  function flatten(items: any[]) {
    items.forEach(item => {
      result.push(item)
      if (item.children && item.children.length > 0) {
        flatten(item.children)
      }
    })
  }
  
  flatten(permissions)
  return result
}

/**
 * 生成权限配置建议
 */
export function generatePermissionSuggestion(userPermissions: string[]): string {
  const config = {
    dashboard: userPermissions.filter(p => p.includes('dashboard')),
    system: userPermissions.filter(p => p.startsWith('system:')),
    user: userPermissions.filter(p => p.startsWith('user:')),
    content: userPermissions.filter(p => p.startsWith('content:')),
    order: userPermissions.filter(p => p.startsWith('order:'))
  }
  
  return JSON.stringify(config, null, 2)
} 
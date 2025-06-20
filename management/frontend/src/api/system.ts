import request from './request'
import type { Admin, SysRole, SysUser, MenuItem } from '@/types'

// 系统管理相关API
export const systemApi = {
  // 管理员相关
  getAdminList: (params?: any) => 
    request.get('/system/admin/page', { params }),

  getAdminById: (id: number) => 
    request.get(`/system/admin/${id}`),

  createAdmin: (data: Partial<Admin>) => 
    request.post('/system/admin', data),

  updateAdmin: (id: number, data: Partial<Admin>) => 
    request.put(`/system/admin/${id}`, data),

  deleteAdmin: (id: number) => 
    request.delete(`/system/admin/${id}`),

  // 重置管理员密码
  resetAdminPassword: (id: number, newPassword: string) => 
    request.put(`/system/admin/${id}/reset-password`, { newPassword }),

  // 角色相关
  getRoleList: (params?: any) => 
    request.get('/system/role/page', { params }),

  getRoleById: (id: number) => 
    request.get(`/system/role/${id}`),

  createRole: (data: Partial<SysRole>) => 
    request.post('/system/role', data),

  updateRole: (id: number, data: Partial<SysRole>) => 
    request.put(`/system/role/${id}`, data),

  deleteRole: (id: number) => 
    request.delete(`/system/role/${id}`),

  // 权限相关
  getPermissionTree: () => 
    request.get('/system/permission/tree'),

  getPermissionList: (params?: any) => 
    request.get('/system/permission/page', { params }),

  // 角色权限分配
  assignRolePermissions: (roleId: number, permissionIds: number[]) => 
    request.put(`/system/role/${roleId}/permissions`, { permissionIds }),

  getRolePermissions: (roleId: number) => 
    request.get(`/system/role/${roleId}/permissions`),

  // 管理员角色分配
  assignAdminRoles: (adminId: number, roleIds: number[]) => 
    request.put(`/system/admin/${adminId}/roles`, { roleIds }),

  getAdminRoles: (adminId: number) => 
    request.get(`/system/admin/${adminId}/roles`),

  // 用户相关
  getUserList: (params?: any) => 
    request.get('/system/user/page', { params }),

  getUserById: (id: number) => 
    request.get(`/system/user/${id}`),

  createUser: (data: Partial<SysUser>) => 
    request.post('/system/user', data),

  updateUser: (id: number, data: Partial<SysUser>) => 
    request.put(`/system/user/${id}`, data),

  deleteUser: (id: number) => 
    request.delete(`/system/user/${id}`),

  // 获取菜单数据（基于权限动态生成）
  getMenuList: () => 
    request.get('/auth/permissions')
}

// 文件上传下载API
export const fileApi = {
  // 上传头像
  uploadAvatar: (file: File) => {
    const formData = new FormData()
    formData.append('file', file)
    return request.post('/file/avatar/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  // 获取头像下载链接
  getAvatarUrl: (year: string, month: string, day: string, fileName: string) => {
    return `/api/file/avatar/download/${year}/${month}/${day}/${fileName}`
  },

  // 获取头像信息
  getAvatarInfo: (year: string, month: string, day: string, fileName: string) => 
    request.get(`/file/avatar/info/${year}/${month}/${day}/${fileName}`),

  // 删除头像
  deleteAvatar: (year: string, month: string, day: string, fileName: string) => 
    request.delete(`/file/avatar/delete/${year}/${month}/${day}/${fileName}`)
}

// 管理员相关API（兼容旧代码）
export const adminApi = systemApi

// 用户相关API（兼容旧代码）
export const userApi = systemApi

// 角色相关API
export const roleApi = {
  // 获取角色列表
  getRoleList: (params: any) => 
    request.get('/system/role/page', { params }),

  // 获取所有角色（不分页）
  getAllRoles: () => 
    request.get('/system/role/list'),

  // 获取角色详情
  getRoleDetail: (id: number) => 
    request.get(`/system/role/${id}`),

  // 创建角色
  createRole: (data: any) => 
    request.post('/system/role', data),

  // 更新角色
  updateRole: (id: number, data: any) => 
    request.put('/system/role', { ...data, id }),

  // 删除角色
  deleteRole: (id: number) => 
    request.delete(`/system/role/${id}`),

  // 批量删除角色
  batchDeleteRole: (ids: number[]) => 
    request.post('/system/role/batch-delete', { ids }),

  // 获取角色权限 - 注意：后端可能没有此接口，需要通过角色详情获取
  getRolePermissions: (id: number) => 
    request.get(`/system/role/${id}`).then(res => {
      // 从角色详情中提取权限信息
      if (res.code === 200 && res.data && res.data.permissions) {
        return { ...res, data: res.data.permissions }
      }
      return { ...res, data: [] }
    }),

  // 分配角色权限
  assignRolePermissions: (id: number, permissionIds: number[]) => 
    request.post('/system/role/assign-permissions', { roleId: id, permissionIds }),

  // 获取角色用户
  getRoleUsers: (id: number) => 
    request.get(`/system/role/${id}/users`)
}

// 权限相关API
export const permissionApi = {
  // 获取权限列表（分页）
  getPermissionList: (params: any) => 
    request.get('/system/permission/page', { params }),

  // 获取权限树 - 主要用于树形展示
  getPermissionTree: () => 
    request.get('/system/permission/tree'),

  // 获取权限详情
  getPermissionDetail: (id: number) => 
    request.get(`/system/permission/${id}`),

  // 创建权限
  createPermission: (data: any) => 
    request.post('/system/permission', data),

  // 更新权限 - 注意后端接口设计，ID包含在请求体中
  updatePermission: (id: number, data: any) => 
    request.put('/system/permission', { ...data, id }),

  // 删除权限
  deletePermission: (id: number) => 
    request.delete(`/system/permission/${id}`),

  // 批量删除权限（如果后端支持）
  batchDeletePermission: (ids: number[]) => 
    request.post('/system/permission/batch-delete', { ids }),

  // 获取菜单权限列表
  getMenuPermissions: () => 
    request.get('/system/permission/menu'),

  // 获取按钮权限列表
  getButtonPermissions: (parentId?: number) => 
    request.get('/system/permission/button', { params: { parentId } }),

  // 修改权限状态
  updatePermissionStatus: (id: number, status: number) => 
    request.put('/system/permission', { id, status }),

  // 获取父级权限选项（用于下拉选择）
  getParentPermissionOptions: () => 
    request.get('/system/permission/tree').then(res => {
      if (res.code === 200) {
        // 过滤出菜单类型的权限作为父级选项
        const buildOptions = (data: any[]): any[] => {
          return data.reduce((acc, item) => {
            if (item.permissionType === 'menu') {
              acc.push({
                value: item.id,
                label: item.permissionName,
                children: item.children ? buildOptions(item.children) : []
              })
            }
            return acc
          }, [] as any[])
        }
        
        return {
          ...res,
          data: [
            { value: null, label: '顶级权限' },
            ...buildOptions(res.data || [])
          ]
        }
      }
      return res
    })
}

// 操作日志相关API
export const logApi = {
  // 获取操作日志列表
  getLogList: (params: any) => 
    request.get('/operation-log/page', { params }),

  // 获取操作日志详情
  getLogDetail: (id: number) => 
    request.get(`/operation-log/${id}`),

  // 导出操作日志
  exportLogs: (params: any) => 
    request.download('/operation-log/export', 'operation_logs.xlsx', { params }),

  // 清空操作日志
  clearLogs: (days: number = 30) => 
    request.delete(`/operation-log/clean/${days}`),

  // 批量删除操作日志
  batchDeleteLogs: (ids: number[]) => 
    request.post('/operation-log/batch-delete', { ids }),

  // 获取操作统计
  getLogStatistics: (params: any) => 
    request.get('/operation-log/statistics', { params })
}

// 系统配置相关API
export const configApi = {
  // 获取系统配置
  getSystemConfig: () => 
    request.get('/system/config'),

  // 更新系统配置
  updateSystemConfig: (data: any) => 
    request.put('/system/config', data),

  // 获取邮件配置
  getEmailConfig: () => 
    request.get('/system/config/email'),

  // 更新邮件配置
  updateEmailConfig: (data: any) => 
    request.put('/system/config/email', data),

  // 测试邮件配置
  testEmailConfig: (data: any) => 
    request.post('/system/config/email/test', data),

  // 获取存储配置
  getStorageConfig: () => 
    request.get('/system/config/storage'),

  // 更新存储配置
  updateStorageConfig: (data: any) => 
    request.put('/system/config/storage', data)
}

// 系统监控相关API
export const monitorApi = {
  // 获取系统信息
  getSystemInfo: () => 
    request.get('/system/monitor/info'),

  // 获取JVM信息
  getJvmInfo: () => 
    request.get('/system/monitor/jvm'),

  // 获取在线用户
  getOnlineUsers: () => 
    request.get('/system/monitor/online'),

  // 踢出用户
  forceLogout: (userId: number) => 
    request.post(`/system/monitor/force-logout/${userId}`),

  // 获取系统缓存
  getCacheInfo: () => 
    request.get('/system/monitor/cache'),

  // 清空系统缓存
  clearCache: () => 
    request.delete('/system/monitor/cache')
}

// 文章管理相关API
export const articleApi = {
  // 获取文章列表
  getArticleList: (params: any) => 
    request.get('/article/page', { params }),

  // 获取文章详情
  getArticleDetail: (id: number) => 
    request.get(`/article/${id}`),

  // 创建文章
  createArticle: (data: any) => 
    request.post('/article', data),

  // 更新文章
  updateArticle: (data: any) => 
    request.put('/article', data),

  // 删除文章
  deleteArticle: (id: number) => 
    request.delete(`/article/${id}`),

  // 批量删除文章
  batchDeleteArticle: (ids: number[]) => 
    request.delete('/article/batch', { data: ids }),

  // 发布文章
  publishArticle: (id: number) => 
    request.put(`/article/${id}/publish`),

  // 下架文章
  unpublishArticle: (id: number) => 
    request.put(`/article/${id}/unpublish`),

  // 设置文章置顶
  setArticleTop: (id: number, isTop: number) => 
    request.put(`/article/${id}/top?isTop=${isTop}`),

  // 设置文章推荐
  setArticleRecommend: (id: number, isRecommend: number) => 
    request.put(`/article/${id}/recommend?isRecommend=${isRecommend}`)
}

// 文章分类管理API
export const categoryApi = {
  // 获取分类列表
  getCategoryList: (params: any) => 
    request.get('/article/category/page', { params }),

  // 获取分类树
  getCategoryTree: () => 
    request.get('/article/category/tree'),

  // 获取启用的分类
  getEnabledCategories: () => 
    request.get('/article/category/enabled'),

  // 获取分类详情
  getCategoryDetail: (id: number) => 
    request.get(`/article/category/${id}`),

  // 创建分类
  createCategory: (data: any) => 
    request.post('/article/category', data),

  // 更新分类
  updateCategory: (data: any) => 
    request.put('/article/category', data),

  // 删除分类
  deleteCategory: (id: number) => 
    request.delete(`/article/category/${id}`),

  // 批量删除分类
  batchDeleteCategory: (ids: number[]) => 
    request.delete('/article/category/batch', { data: ids }),

  // 更新分类状态
  updateCategoryStatus: (id: number, status: number) => 
    request.put(`/article/category/${id}/status?status=${status}`)
}

// 文章标签管理API
export const tagApi = {
  // 获取标签列表
  getTagList: (params: any) => 
    request.get('/article/tag/page', { params }),

  // 获取所有标签（不分页）
  getAllTags: () => 
    request.get('/article/tag/list'),

  // 获取标签详情
  getTagDetail: (id: number) => 
    request.get(`/article/tag/${id}`),

  // 创建标签
  createTag: (data: any) => 
    request.post('/article/tag', data),

  // 更新标签
  updateTag: (id: number, data: any) => 
    request.put(`/article/tag/${id}`, data),

  // 删除标签
  deleteTag: (id: number) => 
    request.delete(`/article/tag/${id}`)
}

// 导出所有API
export default {
  adminApi,
  roleApi,
  permissionApi,
  logApi,
  configApi,
  monitorApi,
  articleApi,
  categoryApi,
  tagApi
} 
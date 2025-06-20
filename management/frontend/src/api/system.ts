import request from './request'

// 管理员相关API
export const adminApi = {
  // 获取管理员列表
  getAdminList: (params: any) => 
    request.get('/admin-user/page', { params }),

  // 获取管理员详情
  getAdminDetail: (id: number) => 
    request.get(`/admin-user/${id}`),

  // 创建管理员
  createAdmin: (data: any) => 
    request.post('/admin/create', data),

  // 更新管理员
  updateAdmin: (id: number, data: any) => 
    request.put(`/admin/${id}`, data),

  // 删除管理员
  deleteAdmin: (id: number) => 
    request.delete(`/admin/${id}`),

  // 批量删除管理员
  batchDeleteAdmin: (ids: number[]) => 
    request.post('/admin/batch-delete', { ids }),

  // 修改管理员状态
  updateAdminStatus: (id: number, status: number) => 
    request.put(`/admin/${id}/status`, { status }),

  // 重置管理员密码
  resetAdminPassword: (id: number, password: string) => 
    request.put(`/admin/${id}/password`, { password }),

  // 获取管理员角色
  getAdminRoles: (id: number) => 
    request.get(`/admin/${id}/roles`),

  // 分配管理员角色
  assignAdminRoles: (id: number, roleIds: number[]) => 
    request.put(`/admin/${id}/roles`, { roleIds })
}

// 角色相关API
export const roleApi = {
  // 获取角色列表
  getRoleList: (params: any) => 
    request.get('/system/role/page', { params }),

  // 获取所有角色（不分页）
  getAllRoles: () => 
    request.get('/role/all'),

  // 获取角色详情
  getRoleDetail: (id: number) => 
    request.get(`/role/${id}`),

  // 创建角色
  createRole: (data: any) => 
    request.post('/role/create', data),

  // 更新角色
  updateRole: (id: number, data: any) => 
    request.put(`/role/${id}`, data),

  // 删除角色
  deleteRole: (id: number) => 
    request.delete(`/role/${id}`),

  // 批量删除角色
  batchDeleteRole: (ids: number[]) => 
    request.post('/role/batch-delete', { ids }),

  // 获取角色权限
  getRolePermissions: (id: number) => 
    request.get(`/role/${id}/permissions`),

  // 分配角色权限
  assignRolePermissions: (id: number, permissionIds: number[]) => 
    request.put(`/role/${id}/permissions`, { permissionIds }),

  // 获取角色用户
  getRoleUsers: (id: number) => 
    request.get(`/role/${id}/users`)
}

// 权限相关API
export const permissionApi = {
  // 获取权限列表
  getPermissionList: (params: any) => 
    request.get('/system/permission/page', { params }),

  // 获取权限树
  getPermissionTree: () => 
    request.get('/system/permission/tree'),

  // 获取权限详情
  getPermissionDetail: (id: number) => 
    request.get(`/system/permission/${id}`),

  // 创建权限
  createPermission: (data: any) => 
    request.post('/system/permission/create', data),

  // 更新权限
  updatePermission: (id: number, data: any) => 
    request.put(`/system/permission/${id}`, data),

  // 删除权限
  deletePermission: (id: number) => 
    request.delete(`/system/permission/${id}`),

  // 批量删除权限
  batchDeletePermission: (ids: number[]) => 
    request.post('/system/permission/batch-delete', { ids }),

  // 获取菜单权限
  getMenuPermissions: () => 
    request.get('/permission/menu'),

  // 获取按钮权限
  getButtonPermissions: (parentId?: number) => 
    request.get('/permission/button', { params: { parentId } })
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
    request.download('/log/export', 'operation_logs.xlsx', { params }),

  // 清空操作日志
  clearLogs: (beforeDate?: string) => 
    request.post('/operation-log/clean', { beforeDate }),

  // 批量删除操作日志
  batchDeleteLogs: (ids: number[]) => 
    request.post('/log/batch-delete', { ids }),

  // 获取操作统计
  getLogStatistics: (params: any) => 
    request.get('/log/statistics', { params })
}

// 系统配置相关API
export const configApi = {
  // 获取系统配置
  getSystemConfig: () => 
    request.get('/config/system'),

  // 更新系统配置
  updateSystemConfig: (data: any) => 
    request.put('/config/system', data),

  // 获取邮件配置
  getEmailConfig: () => 
    request.get('/config/email'),

  // 更新邮件配置
  updateEmailConfig: (data: any) => 
    request.put('/config/email', data),

  // 测试邮件配置
  testEmailConfig: (data: any) => 
    request.post('/config/email/test', data),

  // 获取存储配置
  getStorageConfig: () => 
    request.get('/config/storage'),

  // 更新存储配置
  updateStorageConfig: (data: any) => 
    request.put('/config/storage', data)
}

// 系统监控相关API
export const monitorApi = {
  // 获取系统信息
  getSystemInfo: () => 
    request.get('/monitor/system'),

  // 获取JVM信息
  getJvmInfo: () => 
    request.get('/monitor/jvm'),

  // 获取在线用户
  getOnlineUsers: () => 
    request.get('/monitor/online'),

  // 踢出用户
  kickoutUser: (token: string) => 
    request.post('/monitor/kickout', { token }),

  // 获取缓存信息
  getCacheInfo: () => 
    request.get('/monitor/cache'),

  // 清空缓存
  clearCache: (cacheNames?: string[]) => 
    request.post('/monitor/cache/clear', { cacheNames })
}

// 导出所有API
export default {
  adminApi,
  roleApi,
  permissionApi,
  logApi,
  configApi,
  monitorApi
} 
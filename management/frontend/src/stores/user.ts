import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Admin, SysUser, LoginRequest, LoginResponse, MenuItem } from '@/types'

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref<string>('')
  const refreshToken = ref<string>('')
  const user = ref<Admin | SysUser | null>(null)
  const permissions = ref<string[]>([])
  const roles = ref<string[]>([])
  const menuList = ref<MenuItem[]>([])

  // 计算属性
  const isLoggedIn = computed(() => !!token.value)
  const userName = computed(() => user.value?.username || '')
  const userAvatar = computed(() => user.value?.avatar || '')
  const userInfo = computed(() => user.value)
  const isAdmin = computed(() => {
    if (!user.value) return false
    return 'role' in user.value ? user.value.role === 1 : false
  })

  // 从localStorage恢复数据
  const initStore = () => {
    const storedToken = localStorage.getItem('token')
    const storedUser = localStorage.getItem('user')
    const storedPermissions = localStorage.getItem('permissions')
    const storedRoles = localStorage.getItem('roles')
    const storedMenuList = localStorage.getItem('menuList')

    if (storedToken) {
      token.value = storedToken
    }
    if (storedUser) {
      try {
        user.value = JSON.parse(storedUser)
      } catch (error) {
        console.error('解析用户信息失败:', error)
      }
    }
    if (storedPermissions) {
      try {
        permissions.value = JSON.parse(storedPermissions)
      } catch (error) {
        console.error('解析权限信息失败:', error)
      }
    }
    if (storedRoles) {
      try {
        roles.value = JSON.parse(storedRoles)
      } catch (error) {
        console.error('解析角色信息失败:', error)
      }
    }
    if (storedMenuList) {
      try {
        menuList.value = JSON.parse(storedMenuList)
      } catch (error) {
        console.error('解析菜单信息失败:', error)
      }
    }
  }

  // 登录
  const login = async (loginData: LoginRequest): Promise<void> => {
    try {
      console.log('UserStore: 开始登录流程，参数:', loginData)
      
      // 调用登录API
      const { authApi } = await import('@/api/auth')
      console.log('UserStore: 开始调用登录API')
      
      const response = await authApi.login(loginData)
      console.log('UserStore: 登录API响应:', response)
      
      // 设置登录信息
      setLoginInfo(response.data)
      console.log('UserStore: 登录信息设置完成')
      
      // 获取用户权限
      try {
        console.log('UserStore: 开始获取用户权限')
        const permissionsResponse = await authApi.getUserPermissions()
        console.log('UserStore: 权限API响应:', permissionsResponse)
        
        if (permissionsResponse.data && Array.isArray(permissionsResponse.data)) {
          const permissionCodes = permissionsResponse.data.map((p: any) => p.permissionCode).filter(Boolean)
          permissions.value = permissionCodes
          localStorage.setItem('permissions', JSON.stringify(permissionCodes))
          console.log('UserStore: 权限设置完成:', permissionCodes)
        }
      } catch (permError) {
        console.warn('获取权限失败:', permError)
        // 使用默认权限
        permissions.value = ['system:user:list', 'system:role:list', 'system:permission:list']
        console.log('UserStore: 使用默认权限')
      }
    } catch (error) {
      console.error('UserStore: 登录失败:', error)
      console.error('UserStore: 错误详情:', error)
      throw new Error(error instanceof Error ? error.message : '登录失败，请检查用户名和密码')
    }
  }

  // 设置登录信息
  const setLoginInfo = (loginResponse: LoginResponse) => {
    token.value = loginResponse.token
    
    // 处理用户信息，可能是Admin或AdminInfo格式
    if (loginResponse.user) {
      user.value = loginResponse.user
    } else if ((loginResponse as any).adminInfo) {
      // 处理后台返回的adminInfo格式
      const adminInfo = (loginResponse as any).adminInfo
      user.value = {
        id: adminInfo.id,
        username: adminInfo.username,
        realName: adminInfo.realName,
        email: adminInfo.email,
        phone: adminInfo.phone,
        avatar: adminInfo.avatar,
        status: adminInfo.status,
        role: adminInfo.role,
        createTime: new Date().toISOString(),
        updateTime: new Date().toISOString()
      } as Admin
    }
    
    permissions.value = loginResponse.permissions || []
    roles.value = loginResponse.roles || []
    
    if (loginResponse.refreshToken) {
      refreshToken.value = loginResponse.refreshToken
    }

    // 保存到localStorage
    localStorage.setItem('token', token.value)
    localStorage.setItem('user', JSON.stringify(user.value))
    localStorage.setItem('permissions', JSON.stringify(permissions.value))
    localStorage.setItem('roles', JSON.stringify(roles.value))
    
    if (refreshToken.value) {
      localStorage.setItem('refreshToken', refreshToken.value)
    }
  }

  // 退出登录
  const logout = () => {
    token.value = ''
    refreshToken.value = ''
    user.value = null
    permissions.value = []
    roles.value = []
    menuList.value = []

    // 清除localStorage
    localStorage.removeItem('token')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('user')
    localStorage.removeItem('permissions')
    localStorage.removeItem('roles')
    localStorage.removeItem('menuList')
  }

  // 更新用户信息
  const updateUser = (userData: Partial<Admin | SysUser>) => {
    if (user.value) {
      user.value = { ...user.value, ...userData }
      localStorage.setItem('user', JSON.stringify(user.value))
    }
  }

  // 设置菜单列表
  const setMenuList = (menus: MenuItem[]) => {
    menuList.value = menus
    localStorage.setItem('menuList', JSON.stringify(menus))
  }

  // 检查权限
  const hasPermission = (permission: string): boolean => {
    // admin账户拥有所有权限
    if (isAdmin.value) {
      return true
    }
    return permissions.value.includes(permission)
  }

  // 检查多个权限（or关系）
  const hasAnyPermission = (permissionList: string[]): boolean => {
    // admin账户拥有所有权限
    if (isAdmin.value) {
      return true
    }
    return permissionList.some(permission => permissions.value.includes(permission))
  }

  // 检查多个权限（and关系）
  const hasAllPermissions = (permissionList: string[]): boolean => {
    // admin账户拥有所有权限
    if (isAdmin.value) {
      return true
    }
    return permissionList.every(permission => permissions.value.includes(permission))
  }

  // 检查角色
  const hasRole = (role: string): boolean => {
    // admin账户拥有所有角色权限
    if (isAdmin.value) {
      return true
    }
    return roles.value.includes(role)
  }

  // 检查多个角色（or关系）
  const hasAnyRole = (roleList: string[]): boolean => {
    // admin账户拥有所有角色权限
    if (isAdmin.value) {
      return true
    }
    return roleList.some(role => roles.value.includes(role))
  }

  // 获取用户信息
  const fetchUserInfo = async () => {
    try {
      const { authApi } = await import('@/api/auth')
      const response = await authApi.getUserInfo()
      user.value = response.data
      localStorage.setItem('user', JSON.stringify(user.value))
    } catch (error) {
      console.error('获取用户信息失败:', error)
      throw error
    }
  }

  // 刷新token
  const refreshAccessToken = async (): Promise<boolean> => {
    try {
      if (!refreshToken.value) {
        throw new Error('没有刷新token')
      }

      // 这里应该调用刷新token的API
      // const response = await authApi.refreshToken(refreshToken.value)
      
      // 模拟刷新token响应
      const newToken = 'refreshed-token-' + Date.now()
      token.value = newToken
      localStorage.setItem('token', newToken)
      
      return true
    } catch (error) {
      console.error('刷新token失败:', error)
      logout()
      return false
    }
  }

  return {
    // 状态
    token,
    refreshToken,
    user,
    permissions,
    roles,
    menuList,
    
    // 计算属性
    isLoggedIn,
    userName,
    userAvatar,
    userInfo,
    isAdmin,
    
    // 方法
    initStore,
    login,
    logout,
    setLoginInfo,
    updateUser,
    setMenuList,
    fetchUserInfo,
    hasPermission,
    hasAnyPermission,
    hasAllPermissions,
    hasRole,
    hasAnyRole,
    refreshAccessToken
  }
}) 
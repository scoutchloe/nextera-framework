import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Admin, SysUser, LoginRequest, LoginResponse, MenuItem } from '@/types'
import { generateMenuFromPermissions } from '@/utils/menuGenerator'

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref<string>('')
  const refreshToken = ref<string>('')
  const user = ref<Admin | SysUser | null>(null)
  const permissions = ref<string[]>([])
  const roles = ref<string[]>([])
  const menuList = ref<MenuItem[]>([])
  const permissionTree = ref<any[]>([]) // 后端返回的权限树数据

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
    const storedPermissionTree = localStorage.getItem('permissionTree')

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
    if (storedPermissionTree) {
      try {
        permissionTree.value = JSON.parse(storedPermissionTree)
      } catch (error) {
        console.error('解析权限树失败:', error)
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
      
      // 获取用户权限并生成菜单
      await loadUserPermissionsAndMenu()
      
    } catch (error) {
      console.error('UserStore: 登录失败:', error)
      console.error('UserStore: 错误详情:', error)
      throw new Error(error instanceof Error ? error.message : '登录失败，请检查用户名和密码')
    }
  }

  // 加载用户权限和菜单
  const loadUserPermissionsAndMenu = async () => {
    try {
      console.log('UserStore: 开始获取用户权限')
      const { authApi } = await import('@/api/auth')
      const permissionsResponse = await authApi.getUserPermissions()
      console.log('UserStore: 权限API响应:', permissionsResponse)
      
      if (permissionsResponse.data && Array.isArray(permissionsResponse.data)) {
        // 保存权限树数据
        permissionTree.value = permissionsResponse.data
        localStorage.setItem('permissionTree', JSON.stringify(permissionTree.value))
        
        // 提取权限编码
        const permissionCodes = extractPermissionCodes(permissionsResponse.data)
        permissions.value = permissionCodes
        localStorage.setItem('permissions', JSON.stringify(permissionCodes))
        console.log('UserStore: 权限设置完成:', permissionCodes)
        
        // 生成菜单
        const generatedMenus = generateMenuFromPermissions(permissionsResponse.data)
        menuList.value = generatedMenus
        localStorage.setItem('menuList', JSON.stringify(generatedMenus))
        console.log('UserStore: 菜单生成完成:', generatedMenus)
      }
    } catch (permError) {
      console.warn('获取权限失败:', permError)
      // 权限获取失败时不使用默认权限，避免授予未经授权的访问
      permissions.value = []
      menuList.value = []
      permissionTree.value = []
      
      // 清除相关的localStorage
      localStorage.removeItem('permissions')
      localStorage.removeItem('menuList')
      localStorage.removeItem('permissionTree')
      
      console.log('UserStore: 权限获取失败，清空权限和菜单')
      
      // 可以选择抛出错误让用户重新登录
      throw new Error('获取用户权限失败，请重新登录')
    }
  }

  // 提取权限编码的辅助函数
  const extractPermissionCodes = (permissions: any[]): string[] => {
    const codes: string[] = []
    
    function extract(perms: any[]) {
      for (const perm of perms) {
        if (perm.permissionCode) {
          codes.push(perm.permissionCode)
        }
        if (perm.children && Array.isArray(perm.children)) {
          extract(perm.children)
        }
      }
    }
    
    extract(permissions)
    return codes
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
    
    // 注意：权限和角色现在通过单独的API获取，不再从登录响应中获取
    roles.value = loginResponse.roles || []
    
    if (loginResponse.refreshToken) {
      refreshToken.value = loginResponse.refreshToken
    }

    // 保存到localStorage
    localStorage.setItem('token', token.value)
    localStorage.setItem('user', JSON.stringify(user.value))
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
    permissionTree.value = []

    // 清除localStorage
    localStorage.removeItem('token')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('user')
    localStorage.removeItem('permissions')
    localStorage.removeItem('roles')
    localStorage.removeItem('menuList')
    localStorage.removeItem('permissionTree')
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
      console.log(`权限检查: ${permission} - 管理员账户，拥有所有权限`)
      return true
    }
    const result = permissions.value.includes(permission)
    console.log(`权限检查: ${permission} - 结果: ${result}，用户权限:`, permissions.value)
    return result
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
    return roles.value.includes(role)
  }

  // 检查多个角色（or关系）
  const hasAnyRole = (roleList: string[]): boolean => {
    return roleList.some(role => roles.value.includes(role))
  }

  // 获取用户信息
  const fetchUserInfo = async () => {
    try {
      const { authApi } = await import('@/api/auth')
      const response = await authApi.getUserInfo()
      if (response.data) {
        updateUser(response.data)
      }
      return response.data
    } catch (error) {
      console.error('获取用户信息失败:', error)
      throw error
    }
  }

  // 刷新token
  const refreshAccessToken = async (): Promise<boolean> => {
    try {
      if (!refreshToken.value) {
        return false
      }
      
      const { authApi } = await import('@/api/auth')
      const response = await authApi.refreshToken(refreshToken.value)
      
      if (response.data?.token) {
        token.value = response.data.token
        localStorage.setItem('token', token.value)
        return true
      }
      
      return false
    } catch (error) {
      console.error('刷新token失败:', error)
      return false
    }
  }

  // 重新加载权限和菜单
  const reloadPermissionsAndMenu = async () => {
    await loadUserPermissionsAndMenu()
  }

  return {
    // 状态
    token,
    refreshToken,
    user,
    permissions,
    roles,
    menuList,
    permissionTree,
    
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
    hasPermission,
    hasAnyPermission,
    hasAllPermissions,
    hasRole,
    hasAnyRole,
    fetchUserInfo,
    refreshAccessToken,
    loadUserPermissionsAndMenu,
    reloadPermissionsAndMenu
  }
}) 
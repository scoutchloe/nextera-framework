import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import type { RouteRecordRaw } from 'vue-router'

// 获取用户有权限访问的第一个页面
const getDefaultRoute = (userStore: any): string => {
  console.log('getDefaultRoute: 计算默认路由，用户权限:', userStore.permissions)
  
  // 按优先级检查用户权限并返回对应的默认页面
  if (userStore.hasPermission('dashboard:view')) {
    console.log('getDefaultRoute: 用户有dashboard:view权限，返回/dashboard')
    return '/dashboard'
  }
  if (userStore.hasPermission('article:view') || userStore.hasPermission('article:list')) {
    console.log('getDefaultRoute: 用户有article权限，返回/article/list')
    return '/article/list'
  }
  if (userStore.hasPermission('user:view') || userStore.hasPermission('user:list:view')) {
    console.log('getDefaultRoute: 用户有user权限，返回/user/list')
    return '/user/list'
  }
  if (userStore.hasPermission('system:admin:view')) {
    console.log('getDefaultRoute: 用户有system:admin:view权限，返回/system/admin')
    return '/system/admin'
  }
  if (userStore.hasPermission('system:role:list')) {
    console.log('getDefaultRoute: 用户有system:role:list权限，返回/system/role')
    return '/system/role'
  }
  if (userStore.hasPermission('system:permission:view')) {
    console.log('getDefaultRoute: 用户有system:permission:view权限，返回/system/permission')
    return '/system/permission'
  }
  
  console.log('getDefaultRoute: 用户没有任何页面权限，返回权限调试页面')
  // 如果没有任何页面权限，返回权限调试页面
  return '/permission-debug'
}

// 路由配置
const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Root',
    component: () => import('@/layouts/MainLayout.vue'),
    meta: {
      title: '首页',
      requireAuth: true,
      hideInMenu: true
    }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: {
      title: '登录',
      requireAuth: false,
      hideInMenu: true
    }
  },
  {
    path: '/test',
    name: 'Test',
    component: () => import('@/views/Test.vue'),
    meta: {
      title: '连接测试',
      requireAuth: false,
      hideInMenu: true
    }
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    children: [
      {
        path: '/dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: {
          title: '仪表盘',
          icon: 'DataBoard',
          requireAuth: true,
          permissions: ['dashboard:view']
        }
      },
      {
        path: '/system',
        name: 'System',
        redirect: '/system/admin',
        meta: {
          title: '系统管理',
          icon: 'Setting',
          requireAuth: true
        },
        children: [
          {
            path: '/system/admin',
            name: 'SystemAdmin',
            component: () => import('@/views/system/admin/index.vue'),
            meta: {
              title: '管理员管理',
              icon: 'UserFilled',
              requireAuth: true,
              permissions: ['system:admin:view']
            }
          },
          {
            path: '/system/role',
            name: 'SystemRole',
            component: () => import('@/views/system/role/index.vue'),
            meta: {
              title: '角色管理',
              icon: 'Avatar',
              requireAuth: true,
              permissions: ['system:role:list']
            }
          },
          {
            path: '/system/permission',
            name: 'SystemPermission',
            component: () => import('@/views/system/permission/index.vue'),
            meta: {
              title: '权限管理',
              icon: 'Lock',
              requireAuth: true,
              permissions: ['system:permission:view']
            }
          },
          {
            path: '/system/log',
            name: 'SystemLog',
            component: () => import('@/views/system/log/index.vue'),
            meta: {
              title: '操作日志',
              icon: 'Document',
              requireAuth: true,
              permissions: ['system:log:view']
            }
          }
        ]
      },
      {
        path: '/user',
        name: 'User',
        redirect: '/user/list',
        meta: {
          title: '用户管理',
          icon: 'User',
          requireAuth: true
        },
        children: [
          {
            path: '/user/list',
            name: 'UserList',
            component: () => import('@/views/user/list/index.vue'),
            meta: {
              title: '用户列表',
              icon: 'User',
              requireAuth: true,
              permissions: ['user:list:view']
            }
          },
          {
            path: '/user/analysis',
            name: 'UserAnalysis',
            component: () => import('@/views/user/analysis/index.vue'),
            meta: {
              title: '用户分析',
              icon: 'TrendCharts',
              requireAuth: true,
              permissions: ['user:analysis:view']
            }
          }
        ]
      },
      {
        path: '/article',
        name: 'Article',
        redirect: '/article/list',
        meta: {
          title: '文章管理',
          icon: 'EditPen',
          requireAuth: true
        },
        children: [
          {
            path: '/article/list',
            name: 'ArticleList',
            component: () => import('@/views/article/list/index.vue'),
            meta: {
              title: '文章列表',
              icon: 'Document',
              requireAuth: true,
              permissions: ['article:list']
            }
          },
          {
            path: '/article/category',
            name: 'ArticleCategory',
            component: () => import('@/views/article/category/index.vue'),
            meta: {
              title: '分类管理',
              icon: 'Collection',
              requireAuth: true,
              permissions: ['article:category:list']
            }
          },
          {
            path: '/article/tag',
            name: 'ArticleTag',
            component: () => import('@/views/article/tag/index.vue'),
            meta: {
              title: '标签管理',
              icon: 'PriceTag',
              requireAuth: true,
              permissions: ['article:tag:list']
            }
          }
        ]
      }
    ]
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/views/profile/index.vue'),
    meta: {
      title: '个人中心',
      requireAuth: true,
      hideInMenu: true
    }
  },
  {
    path: '/permission-debug',
    name: 'PermissionDebug',
    component: () => import('@/views/PermissionDebug.vue'),
    meta: {
      title: '权限调试',
      requireAuth: true,
      hideInMenu: true
    }
  },
  {
    path: '/settings',
    name: 'Settings',
    component: () => import('@/views/settings/index.vue'),
    meta: {
      title: '系统设置',
      requireAuth: true,
      hideInMenu: true
    }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: {
      title: '页面不存在',
      hideInMenu: true
    }
  }
]

// 创建路由实例
const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ left: 0, top: 0 })
})

// 路由守卫
router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()
  
  // 设置页面标题
  document.title = to.meta?.title ? `${to.meta.title} - Nextera管理端` : 'Nextera管理端'
  
  // 处理根路径重定向
  if (to.path === '/' && userStore.token) {
    console.log('路由守卫: 根路径访问，重定向到默认页面')
    const defaultRoute = getDefaultRoute(userStore)
    console.log('路由守卫: 根路径重定向到:', defaultRoute)
    next(defaultRoute)
    return
  }
  
  // 检查是否需要登录
  if (to.meta?.requireAuth) {
    if (!userStore.token) {
      ElMessage.warning('请先登录')
      next('/login')
      return
    }
    
    // 检查用户信息
    if (!userStore.userInfo) {
      try {
        await userStore.fetchUserInfo()
      } catch (error) {
        ElMessage.error('获取用户信息失败')
        userStore.logout()
        next('/login')
        return
      }
    }
    
    // 确保权限已加载
    if (userStore.permissions.length === 0) {
      console.log('路由守卫: 权限为空，尝试加载权限')
      try {
        await userStore.loadUserPermissionsAndMenu()
        console.log('路由守卫: 权限加载成功，权限列表:', userStore.permissions)
      } catch (error) {
        console.error('路由守卫: 加载权限失败:', error)
        ElMessage.error('获取权限信息失败')
        next('/permission-debug')
        return
      }
    } else {
      console.log('路由守卫: 权限已存在:', userStore.permissions)
    }
    
    // 检查权限
    if (to.meta?.permissions && Array.isArray(to.meta.permissions)) {
      console.log('路由守卫: 检查页面权限', {
        targetPath: to.path,
        requiredPermissions: to.meta.permissions,
        userPermissions: userStore.permissions
      })
      
      const hasPermission = to.meta.permissions.some(permission => 
        userStore.hasPermission(permission as string)
      )
      
      console.log('路由守卫: 权限检查结果:', hasPermission)
      
      if (!hasPermission) {
        console.log('路由守卫: 权限不足，重定向到默认页面')
        // 重定向到用户有权限的默认页面
        const defaultRoute = getDefaultRoute(userStore)
        console.log('路由守卫: 重定向到:', defaultRoute)
        
        // 只有当没有任何可访问页面时才显示权限错误
        if (defaultRoute === '/permission-debug') {
          ElMessage.error('您没有访问该页面的权限')
        } else {
          // 对于正常的重定向，不显示错误消息
          console.log('路由守卫: 静默重定向到有权限的页面，不显示错误提示')
        }
        
        next(defaultRoute)
        return
      }
    } else {
      console.log('路由守卫: 该页面无需特殊权限检查，路径:', to.path)
    }
  }
  
  // 如果已登录用户访问登录页，重定向到用户有权限的默认页面
  if (to.path === '/login' && userStore.token) {
    const defaultRoute = getDefaultRoute(userStore)
    next(defaultRoute)
    return
  }
  
  next()
})

// 路由错误处理
router.onError((error) => {
  console.error('路由错误:', error)
  ElMessage.error('页面加载失败，请刷新重试')
})

export default router

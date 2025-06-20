import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import type { RouteRecordRaw } from 'vue-router'

// 路由配置
const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/dashboard'
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
          requireAuth: true
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
              permissions: ['system:admin:list']
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
              permissions: ['system:permission:list']
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
              permissions: ['system:log:list']
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
              permissions: ['user:list']
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
              permissions: ['user:analysis']
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
    path: '/quick-test',
    name: 'QuickTest',
    component: () => import('@/views/QuickTest.vue'),
    meta: {
      title: '快速测试',
      requireAuth: false,
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
    
    // 检查权限
    if (to.meta?.permissions && Array.isArray(to.meta.permissions)) {
      const hasPermission = to.meta.permissions.some(permission => 
        userStore.hasPermission(permission as string)
      )
      
      if (!hasPermission) {
        ElMessage.error('您没有访问该页面的权限')
        next('/dashboard')
        return
      }
    }
  }
  
  // 如果已登录用户访问登录页，重定向到仪表盘
  if (to.path === '/login' && userStore.token) {
    next('/dashboard')
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
